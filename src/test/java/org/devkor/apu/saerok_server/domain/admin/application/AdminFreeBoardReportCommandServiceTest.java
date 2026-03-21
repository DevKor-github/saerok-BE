package org.devkor.apu.saerok_server.domain.admin.application;

import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.audit.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.domain.admin.report.application.AdminFreeBoardReportCommandService;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.*;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.*;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFreeBoardReportCommandServiceTest {

    private static final long ADMIN_ID = 999L;
    private static final String REASON = "운영정책 위반(욕설)";

    @InjectMocks AdminFreeBoardReportCommandService sut;

    @Mock FreeBoardPostReportRepository postReportRepository;
    @Mock FreeBoardPostCommentReportRepository commentReportRepository;
    @Mock FreeBoardPostRepository postRepository;
    @Mock FreeBoardPostCommentRepository commentRepository;
    @Mock AdminAuditLogRepository adminAuditLogRepository;
    @Mock UserRepository userRepository;

    private static User user(long id) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private static FreeBoardPost post(long id, User owner) {
        FreeBoardPost p = FreeBoardPost.of(owner, "게시글 내용");
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private static FreeBoardPostReport postReport(long reportId, long postId, long reporterId, long reportedId) {
        User reporter = user(reporterId);
        User reported = user(reportedId);
        FreeBoardPost p = post(postId, reported);
        FreeBoardPostReport r = FreeBoardPostReport.of(reporter, reported, p);
        ReflectionTestUtils.setField(r, "id", reportId);
        return r;
    }

    private static FreeBoardPostCommentReport commentReport(long reportId, long commentId, long postId, long reporterId, long reportedId, String content) {
        User reporter = user(reporterId);
        User reported = user(reportedId);
        FreeBoardPost p = post(postId, reported);
        FreeBoardPostComment c = FreeBoardPostComment.of(reported, p, content);
        ReflectionTestUtils.setField(c, "id", commentId);
        FreeBoardPostCommentReport r = FreeBoardPostCommentReport.of(reporter, reported, c);
        ReflectionTestUtils.setField(r, "id", reportId);
        return r;
    }

    private void stubAdminUser() {
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(user(ADMIN_ID)));
    }

    /* ───────────── ignorePostReport ───────────── */

    @Test @DisplayName("ignorePostReport: 성공 → 신고 삭제 + 감사 로그")
    void ignorePostReport_success() {
        long reportId = 1L;
        var rep = postReport(reportId, 100L, 10L, 20L);
        when(postReportRepository.findById(reportId)).thenReturn(Optional.of(rep));
        when(postReportRepository.deleteById(reportId)).thenReturn(true);
        stubAdminUser();

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);

        sut.ignorePostReport(ADMIN_ID, reportId);

        verify(postReportRepository).deleteById(reportId);
        verify(adminAuditLogRepository).save(cap.capture());

        AdminAuditLog log = cap.getValue();
        assertThat(log.getAction()).isEqualTo(AdminAuditAction.REPORT_IGNORED);
        Map<String, Object> md = log.getMetadata();
        assertThat(md.get("postId")).isEqualTo(100L);
    }

    @Test @DisplayName("ignorePostReport: 없음 → 404")
    void ignorePostReport_notFound() {
        when(postReportRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.ignorePostReport(ADMIN_ID, 9L)).isInstanceOf(NotFoundException.class);
    }

    @Test @DisplayName("ignorePostReport: deleteById false → 404")
    void ignorePostReport_deleteFailed() {
        long reportId = 9L;
        when(postReportRepository.findById(reportId)).thenReturn(Optional.of(postReport(reportId, 1L, 2L, 3L)));
        when(postReportRepository.deleteById(reportId)).thenReturn(false);
        assertThatThrownBy(() -> sut.ignorePostReport(ADMIN_ID, reportId)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(userRepository, adminAuditLogRepository);
    }

    /* ───────────── ignoreCommentReport ───────────── */

    @Test @DisplayName("ignoreCommentReport: 성공 → 신고 삭제 + 감사 로그")
    void ignoreCommentReport_success() {
        long reportId = 2L;
        var rep = commentReport(reportId, 200L, 10L, 11L, 21L, "악성 댓글");
        when(commentReportRepository.findById(reportId)).thenReturn(Optional.of(rep));
        when(commentReportRepository.deleteById(reportId)).thenReturn(true);
        stubAdminUser();

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);

        sut.ignoreCommentReport(ADMIN_ID, reportId);

        verify(commentReportRepository).deleteById(reportId);
        verify(adminAuditLogRepository).save(cap.capture());

        AdminAuditLog log = cap.getValue();
        assertThat(log.getAction()).isEqualTo(AdminAuditAction.REPORT_IGNORED);
        Map<String, Object> md = log.getMetadata();
        assertThat(md.get("commentId")).isEqualTo(200L);
        assertThat(md.get("commentContentSnapshot")).isEqualTo("악성 댓글");
    }

    @Test @DisplayName("ignoreCommentReport: 없음 → 404")
    void ignoreCommentReport_notFound() {
        when(commentReportRepository.findById(8L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.ignoreCommentReport(ADMIN_ID, 8L)).isInstanceOf(NotFoundException.class);
    }

    /* ───────────── deletePostByReport ───────────── */

    @Test @DisplayName("deletePostByReport: 삭제 + 신고 정리 + 감사 로그(reason, content 스냅샷)")
    void deletePostByReport_success() {
        long reportId = 50L; long postId = 100L;
        var rep = postReport(reportId, postId, 1L, 2L);
        when(postReportRepository.findById(reportId)).thenReturn(Optional.of(rep));

        FreeBoardPost p = post(postId, user(2L));
        when(postRepository.findById(postId)).thenReturn(Optional.of(p));
        stubAdminUser();

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);

        sut.deletePostByReport(ADMIN_ID, reportId, REASON);

        verify(postReportRepository).deleteByPostId(postId);
        verify(commentReportRepository).deleteByPostId(postId);
        verify(postRepository).remove(p);
        verify(adminAuditLogRepository).save(cap.capture());

        AdminAuditLog log = cap.getValue();
        assertThat(log.getAction()).isEqualTo(AdminAuditAction.FREEBOARD_POST_DELETED);
        Map<String, Object> md = log.getMetadata();
        assertThat(md.get("reason")).isEqualTo(REASON);
        assertThat(md.get("postContentSnapshot")).isEqualTo("게시글 내용");
    }

    @Test @DisplayName("deletePostByReport: 신고 없음 → 404")
    void deletePostByReport_notFound() {
        when(postReportRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.deletePostByReport(ADMIN_ID, 404L, REASON)).isInstanceOf(NotFoundException.class);
    }

    /* ───────────── deleteCommentByReport ───────────── */

    @Test @DisplayName("deleteCommentByReport: 대댓글 없는 경우 → hard delete")
    void deleteCommentByReport_noReplies_hardDelete() {
        long reportId = 70L; long cmId = 900L; long postId = 10L;
        var rep = commentReport(reportId, cmId, postId, 11L, 21L, "bye");
        when(commentReportRepository.findById(reportId)).thenReturn(Optional.of(rep));

        FreeBoardPostComment cm = FreeBoardPostComment.of(user(21L), post(postId, user(21L)), "bye");
        ReflectionTestUtils.setField(cm, "id", cmId);
        when(commentRepository.findById(cmId)).thenReturn(Optional.of(cm));
        when(commentRepository.hasReplies(cmId)).thenReturn(false);
        stubAdminUser();

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);

        sut.deleteCommentByReport(ADMIN_ID, reportId, REASON);

        verify(commentReportRepository).deleteByCommentId(cmId);
        verify(commentRepository).remove(cm);
        assertThat(cm.getStatus()).isEqualTo(FreeBoardCommentStatus.ACTIVE); // not banned
        verify(adminAuditLogRepository).save(cap.capture());

        AdminAuditLog log = cap.getValue();
        assertThat(log.getAction()).isEqualTo(AdminAuditAction.FREEBOARD_COMMENT_DELETED);
        Map<String, Object> md = log.getMetadata();
        assertThat(md.get("reason")).isEqualTo(REASON);
        assertThat(md.get("commentContentSnapshot")).isEqualTo("bye");
    }

    @Test @DisplayName("deleteCommentByReport: 대댓글 있는 경우 → ban (soft delete)")
    void deleteCommentByReport_hasReplies_softDelete() {
        long reportId = 71L; long cmId = 901L; long postId = 11L;
        var rep = commentReport(reportId, cmId, postId, 12L, 22L, "parent");
        when(commentReportRepository.findById(reportId)).thenReturn(Optional.of(rep));

        FreeBoardPostComment cm = FreeBoardPostComment.of(user(22L), post(postId, user(22L)), "parent");
        ReflectionTestUtils.setField(cm, "id", cmId);
        when(commentRepository.findById(cmId)).thenReturn(Optional.of(cm));
        when(commentRepository.hasReplies(cmId)).thenReturn(true);
        stubAdminUser();

        sut.deleteCommentByReport(ADMIN_ID, reportId, REASON);

        verify(commentReportRepository).deleteByCommentId(cmId);
        verify(commentRepository, never()).remove(any());
        assertThat(cm.getStatus()).isEqualTo(FreeBoardCommentStatus.BANNED);
    }

    @Test @DisplayName("deleteCommentByReport: 신고 없음 → 404")
    void deleteCommentByReport_notFound() {
        when(commentReportRepository.findById(71L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.deleteCommentByReport(ADMIN_ID, 71L, REASON)).isInstanceOf(NotFoundException.class);
    }
}
