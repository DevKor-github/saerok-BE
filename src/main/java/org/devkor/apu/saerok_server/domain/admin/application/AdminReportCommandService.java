package org.devkor.apu.saerok_server.domain.admin.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditTargetType;
import org.devkor.apu.saerok_server.domain.admin.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentReport;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionReport;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.devkor.apu.saerok_server.global.shared.util.TransactionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminReportCommandService {

    private final CollectionReportRepository collectionReportRepository;
    private final CollectionCommentReportRepository commentReportRepository;

    private final CollectionRepository collectionRepository;
    private final CollectionCommentRepository commentRepository;
    private final CollectionImageRepository collectionImageRepository;

    private final ImageService imageService;

    // 감사/행위자 조회
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserRepository userRepository;

    /* ───────────── 신고 무시 ───────────── */

    public void ignoreCollectionReport(Long adminUserId, Long reportId) {
        UserBirdCollectionReport report = collectionReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 새록 신고가 없어요"));

        if (!collectionReportRepository.deleteById(reportId)) {
            throw new NotFoundException("해당 ID의 새록 신고가 없어요");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("collectionId", report.getCollection().getId());
        metadata.put("reportedUserId", report.getReportedUser().getId());
        metadata.put("reporterId", report.getReporter().getId());

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.REPORT_IGNORED,
                AdminAuditTargetType.REPORT_COLLECTION,
                reportId,
                reportId,
                metadata
        ));
    }

    public void ignoreCommentReport(Long adminUserId, Long reportId) {
        UserBirdCollectionCommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 댓글 신고가 없어요"));

        if (!commentReportRepository.deleteById(reportId)) {
            throw new NotFoundException("해당 ID의 댓글 신고가 없어요");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("commentId", report.getComment().getId());
        metadata.put("collectionId", report.getComment().getCollection().getId());
        metadata.put("reportedUserId", report.getReportedUser().getId());
        metadata.put("reporterId", report.getReporter().getId());
        metadata.put("commentContentSnapshot", report.getCommentContent());

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.REPORT_IGNORED,
                AdminAuditTargetType.REPORT_COMMENT,
                reportId,
                reportId,
                metadata
        ));
    }

    /* ───────────── 신고 대상 삭제 ───────────── */

    public void deleteCollectionByReport(Long adminUserId, Long reportId, String reason) {
        UserBirdCollectionReport report = collectionReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 새록 신고가 없어요"));

        Long collectionId = report.getCollection().getId();

        // 1) 관련 신고 정리
        collectionReportRepository.deleteByCollectionId(collectionId);
        commentReportRepository.deleteByCollectionId(collectionId);

        // 2) 이미지 삭제 준비
        List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(collectionId);

        // 3) 컬렉션 삭제 (노트 스냅샷 추출을 위해 먼저 로드)
        UserBirdCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 컬렉션이 존재하지 않아요"));
        String noteSnapshot = collection.getNote(); // 한 줄 평 스냅샷
        collectionImageRepository.removeByCollectionId(collection.getId());
        collectionRepository.remove(collection);

        // 4) 감사 기록
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reporterId", report.getReporter().getId());
        metadata.put("reportedUserId", report.getReportedUser().getId());
        metadata.put("deletedImageObjectKeyCount", objectKeys.size());
        metadata.put("reason", reason);
        metadata.put("collectionNoteSnapshot", noteSnapshot);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.COLLECTION_DELETED,
                AdminAuditTargetType.COLLECTION,
                collectionId,
                reportId,
                metadata
        ));

        // 5) 커밋 후 S3 삭제
        if (!objectKeys.isEmpty()) {
            TransactionUtils.runAfterCommitOrNow(() -> imageService.deleteAll(objectKeys));
        }
    }

    public void deleteCommentByReport(Long adminUserId, Long reportId, String reason) {
        UserBirdCollectionCommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 댓글 신고가 없어요"));

        Long commentId = report.getComment().getId();

        // 1) 관련 신고 정리
        commentReportRepository.deleteByCommentId(commentId);

        // 2) 댓글 삭제
        UserBirdCollectionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("해당 댓글이 존재하지 않아요"));
        commentRepository.remove(comment);

        // 3) 감사 기록
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("collectionId", report.getComment().getCollection().getId());
        metadata.put("reportedUserId", report.getReportedUser().getId());
        metadata.put("reporterId", report.getReporter().getId());
        metadata.put("commentContentSnapshot", report.getCommentContent());
        metadata.put("reason", reason);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.COMMENT_DELETED,
                AdminAuditTargetType.COMMENT,
                commentId,
                reportId,
                metadata
        ));
    }
}
