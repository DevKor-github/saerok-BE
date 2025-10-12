package org.devkor.apu.saerok_server.domain.admin.application;

import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditLog;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportCommandServiceTest {

    private static final long ADMIN_ID = 999L;
    private static final String REASON = "운영정책 위반(욕설)";

    @InjectMocks
    AdminReportCommandService sut;

    @Mock CollectionReportRepository         collectionReportRepository;
    @Mock CollectionCommentReportRepository  commentReportRepository;
    @Mock CollectionRepository               collectionRepository;
    @Mock CollectionCommentRepository        commentRepository;
    @Mock CollectionImageRepository          collectionImageRepository;
    @Mock ImageService imageService;

    @Mock AdminAuditLogRepository adminAuditLogRepository;
    @Mock UserRepository          userRepository;

    private static User user(long id) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private static UserBirdCollection collection(long id, String note) {
        UserBirdCollection c = new UserBirdCollection();
        ReflectionTestUtils.setField(c, "id", id);
        c.setNote(note);
        return c;
    }

    private static UserBirdCollection collection(long id) { return collection(id, null); }

    private static UserBirdCollectionReport collectionReport(long reportId, long collectionId, long reporterId, long reportedId) {
        User reporter = user(reporterId);
        User reported = user(reportedId);
        UserBirdCollection col = collection(collectionId);
        UserBirdCollectionReport rep = UserBirdCollectionReport.of(reporter, reported, col);
        ReflectionTestUtils.setField(rep, "id", reportId);
        return rep;
    }

    private static UserBirdCollectionCommentReport commentReport(long reportId, long commentId, long collectionId, long reporterId, long reportedId, String content) {
        User reporter = user(reporterId);
        User reported = user(reportedId);
        UserBirdCollection col = collection(collectionId);
        UserBirdCollectionComment cm = UserBirdCollectionComment.of(reported, col, content);
        ReflectionTestUtils.setField(cm, "id", commentId);
        UserBirdCollectionCommentReport rep = UserBirdCollectionCommentReport.of(reporter, reported, cm);
        ReflectionTestUtils.setField(rep, "id", reportId);
        return rep;
    }

    private void stubAdminUser() {
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(user(ADMIN_ID)));
    }

    @Test
    @DisplayName("ignoreCollectionReport: 성공 → 신고 삭제 + 감사 로그 기록")
    void ignoreCollectionReport_success() {
        long reportId = 1L;
        var rep = collectionReport(reportId, 100L, 10L, 20L);
        when(collectionReportRepository.findById(reportId)).thenReturn(Optional.of(rep));
        when(collectionReportRepository.deleteById(reportId)).thenReturn(true);
        stubAdminUser();

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);

        sut.ignoreCollectionReport(ADMIN_ID, reportId);

        verify(collectionReportRepository).deleteById(reportId);
        verify(adminAuditLogRepository).save(cap.capture());

        AdminAuditLog log = cap.getValue();
        assertThat(log.getAction()).isEqualTo(AdminAuditAction.REPORT_IGNORED);
        Map<String, Object> md = log.getMetadata();
        assertThat(md.get("collectionId")).isEqualTo(100L);
    }

    @Test
    @DisplayName("ignoreCollectionReport: 없음 → 404")
    void ignoreCollectionReport_notFound() {
        long reportId = 9L;
        when(collectionReportRepository.findById(reportId)).thenReturn(Optional.of(collectionReport(reportId, 1L, 2L, 3L)));
        when(collectionReportRepository.deleteById(reportId)).thenReturn(false);
        assertThatThrownBy(() -> sut.ignoreCollectionReport(ADMIN_ID, reportId)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(userRepository, adminAuditLogRepository);
    }

    @Test
    @DisplayName("ignoreCommentReport: 성공 → 신고 삭제 + 감사 로그 기록")
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

    @Test
    @DisplayName("ignoreCommentReport: 없음 → 404")
    void ignoreCommentReport_notFound() {
        long reportId = 8L;
        when(commentReportRepository.findById(reportId)).thenReturn(Optional.of(commentReport(reportId, 1L, 1L, 1L, 1L, "x")));
        when(commentReportRepository.deleteById(reportId)).thenReturn(false);
        assertThatThrownBy(() -> sut.ignoreCommentReport(ADMIN_ID, reportId)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(userRepository, adminAuditLogRepository);
    }

    @Test
    @DisplayName("deleteCollectionByReport: 삭제 + 신고 정리 + S3 삭제 + 감사 로그(reason, note 스냅샷)")
    void deleteCollectionByReport_success() {
        long reportId = 50L; long colId = 100L;
        var rep = collectionReport(reportId, colId, 1L, 2L);
        when(collectionReportRepository.findById(reportId)).thenReturn(Optional.of(rep));
        when(collectionImageRepository.findObjectKeysByCollectionId(colId)).thenReturn(List.of("k1", "k2"));

        UserBirdCollection col = collection(colId, "노트 스냅샷");
        when(collectionRepository.findById(colId)).thenReturn(Optional.of(col));
        stubAdminUser();

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);

        sut.deleteCollectionByReport(ADMIN_ID, reportId, REASON);

        verify(collectionReportRepository).deleteByCollectionId(colId);
        verify(commentReportRepository).deleteByCollectionId(colId);
        verify(collectionRepository).remove(col);
        verify(imageService).deleteAll(List.of("k1", "k2"));
        verify(adminAuditLogRepository).save(cap.capture());

        AdminAuditLog log = cap.getValue();
        assertThat(log.getAction()).isEqualTo(AdminAuditAction.COLLECTION_DELETED);
        Map<String, Object> md = log.getMetadata();
        assertThat(md.get("reason")).isEqualTo(REASON);
        assertThat(md.get("collectionNoteSnapshot")).isEqualTo("노트 스냅샷");
        assertThat(md.get("deletedImageObjectKeyCount")).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteCollectionByReport: 신고 없음 → 404")
    void deleteCollectionByReport_notFound() {
        when(collectionReportRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.deleteCollectionByReport(ADMIN_ID, 404L, REASON)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(userRepository, adminAuditLogRepository, imageService);
    }

    @Test
    @DisplayName("deleteCommentByReport: 삭제 + 신고 정리 + 감사 로그(reason 포함)")
    void deleteCommentByReport_success() {
        long reportId = 70L; long cmId = 900L; long colId = 10L;
        var rep = commentReport(reportId, cmId, colId, 11L, 21L, "bye");
        when(commentReportRepository.findById(reportId)).thenReturn(Optional.of(rep));
        UserBirdCollectionComment cm = UserBirdCollectionComment.of(user(21L), collection(colId), "bye");
        ReflectionTestUtils.setField(cm, "id", cmId);
        when(commentRepository.findById(cmId)).thenReturn(Optional.of(cm));
        stubAdminUser();

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);

        sut.deleteCommentByReport(ADMIN_ID, reportId, REASON);

        verify(commentReportRepository).deleteByCommentId(cmId);
        verify(commentRepository).remove(cm);
        verify(adminAuditLogRepository).save(cap.capture());

        AdminAuditLog log = cap.getValue();
        assertThat(log.getAction()).isEqualTo(AdminAuditAction.COMMENT_DELETED);
        Map<String, Object> md = log.getMetadata();
        assertThat(md.get("reason")).isEqualTo(REASON);
        assertThat(md.get("commentContentSnapshot")).isEqualTo("bye");
    }

    @Test
    @DisplayName("deleteCommentByReport: 신고 없음 → 404")
    void deleteCommentByReport_notFound() {
        when(commentReportRepository.findById(71L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.deleteCommentByReport(ADMIN_ID, 71L, REASON)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(userRepository, adminAuditLogRepository);
    }
}
