package org.devkor.apu.saerok_server.domain.admin.report.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditTargetType;
import org.devkor.apu.saerok_server.domain.admin.audit.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.domain.admin.notification.application.event.AdminNotificationEvent;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostCommentReport;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostReport;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentReportRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostReportRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminFreeBoardReportCommandService {

    private final FreeBoardPostReportRepository postReportRepository;
    private final FreeBoardPostCommentReportRepository commentReportRepository;

    private final FreeBoardPostRepository postRepository;
    private final FreeBoardPostCommentRepository commentRepository;

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /* ───────────── 신고 무시 ───────────── */

    public void ignorePostReport(Long adminUserId, Long reportId) {
        FreeBoardPostReport report = postReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 자유게시판 게시글 신고가 없어요"));

        if (!postReportRepository.deleteById(reportId)) {
            throw new NotFoundException("해당 ID의 자유게시판 게시글 신고가 없어요");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("postId", report.getPost().getId());
        metadata.put("reportedUserId", report.getReportedUser().getId());
        metadata.put("reporterId", report.getReporter().getId());

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.REPORT_IGNORED,
                AdminAuditTargetType.REPORT_FREEBOARD_POST,
                reportId,
                reportId,
                metadata
        ));
    }

    public void ignoreCommentReport(Long adminUserId, Long reportId) {
        FreeBoardPostCommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 자유게시판 댓글 신고가 없어요"));

        if (!commentReportRepository.deleteById(reportId)) {
            throw new NotFoundException("해당 ID의 자유게시판 댓글 신고가 없어요");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("commentId", report.getComment().getId());
        metadata.put("postId", report.getComment().getPost().getId());
        metadata.put("reportedUserId", report.getReportedUser().getId());
        metadata.put("reporterId", report.getReporter().getId());
        metadata.put("commentContentSnapshot", report.getCommentContent());

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.REPORT_IGNORED,
                AdminAuditTargetType.REPORT_FREEBOARD_COMMENT,
                reportId,
                reportId,
                metadata
        ));
    }

    /* ───────────── 신고 대상 삭제 ───────────── */

    public void deletePostByReport(Long adminUserId, Long reportId, String reason) {
        FreeBoardPostReport report = postReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 자유게시판 게시글 신고가 없어요"));

        Long postId = report.getPost().getId();

        // 1) 관련 신고 정리
        postReportRepository.deleteByPostId(postId);
        commentReportRepository.deleteByPostId(postId);

        // 2) 게시글 삭제 (게시글 내용 스냅샷 추출)
        FreeBoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("해당 게시글이 존재하지 않아요"));
        String contentSnapshot = post.getContent();
        postRepository.remove(post);

        // 3) 감사 기록
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reporterId", report.getReporter().getId());
        metadata.put("reportedUserId", report.getReportedUser().getId());
        metadata.put("reason", reason);
        metadata.put("postContentSnapshot", contentSnapshot);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.FREEBOARD_POST_DELETED,
                AdminAuditTargetType.FREEBOARD_POST,
                postId,
                reportId,
                metadata
        ));

        // 콘텐츠 삭제 알림
        eventPublisher.publishEvent(new AdminNotificationEvent.ContentDeletedByReport(
                report.getReportedUser().getId(), reason));
    }

    public void deleteCommentByReport(Long adminUserId, Long reportId, String reason) {
        FreeBoardPostCommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 자유게시판 댓글 신고가 없어요"));

        Long commentId = report.getComment().getId();

        // 1) 관련 신고 정리
        commentReportRepository.deleteByCommentId(commentId);

        // 2) 댓글 삭제 (대댓글이 있으면 ban, 없으면 hard delete)
        FreeBoardPostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("해당 댓글이 존재하지 않아요"));

        if (commentRepository.hasReplies(commentId)) {
            comment.ban();
        } else {
            commentRepository.remove(comment);
        }

        // 3) 감사 기록
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("postId", report.getComment().getPost().getId());
        metadata.put("reportedUserId", report.getReportedUser().getId());
        metadata.put("reporterId", report.getReporter().getId());
        metadata.put("commentContentSnapshot", report.getCommentContent());
        metadata.put("reason", reason);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.FREEBOARD_COMMENT_DELETED,
                AdminAuditTargetType.FREEBOARD_COMMENT,
                commentId,
                reportId,
                metadata
        ));

        // 콘텐츠 삭제 알림
        eventPublisher.publishEvent(new AdminNotificationEvent.ContentDeletedByReport(
                report.getReportedUser().getId(), reason));
    }
}
