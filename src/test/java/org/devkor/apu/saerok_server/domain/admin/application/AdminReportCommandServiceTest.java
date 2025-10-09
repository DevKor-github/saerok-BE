package org.devkor.apu.saerok_server.domain.admin.application;

import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentReport;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionReport;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportCommandServiceTest {

    @InjectMocks
    AdminReportCommandService sut;

    /* repos */
    @Mock CollectionReportRepository         collectionReportRepository;
    @Mock CollectionCommentReportRepository  commentReportRepository;
    @Mock CollectionRepository               collectionRepository;
    @Mock CollectionCommentRepository        commentRepository;
    @Mock CollectionImageRepository          collectionImageRepository;

    @Mock ImageService imageService;

    private static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    private static UserBirdCollection collection(long id) {
        UserBirdCollection c = new UserBirdCollection();
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    @Test
    @DisplayName("ignoreCollectionReport: 성공")
    void ignoreCollectionReport_success() {
        when(collectionReportRepository.deleteById(1L)).thenReturn(true);
        sut.ignoreCollectionReport(1L);
        verify(collectionReportRepository).deleteById(1L);
    }

    @Test
    @DisplayName("ignoreCollectionReport: 없음 → 404")
    void ignoreCollectionReport_notFound() {
        when(collectionReportRepository.deleteById(9L)).thenReturn(false);
        assertThatThrownBy(() -> sut.ignoreCollectionReport(9L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("ignoreCommentReport: 성공")
    void ignoreCommentReport_success() {
        when(commentReportRepository.deleteById(2L)).thenReturn(true);
        sut.ignoreCommentReport(2L);
        verify(commentReportRepository).deleteById(2L);
    }

    @Test
    @DisplayName("ignoreCommentReport: 없음 → 404")
    void ignoreCommentReport_notFound() {
        when(commentReportRepository.deleteById(8L)).thenReturn(false);
        assertThatThrownBy(() -> sut.ignoreCommentReport(8L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("deleteCollectionByReport: 신고된 새록 삭제 + 신고 정리 + S3 삭제")
    void deleteCollectionByReport_success() {
        UserBirdCollection col = collection(100L);

        UserBirdCollectionReport rep = newInstance(UserBirdCollectionReport.class);
        ReflectionTestUtils.setField(rep, "id", 50L);
        ReflectionTestUtils.setField(rep, "collection", col);

        when(collectionReportRepository.findById(50L)).thenReturn(Optional.of(rep));
        when(collectionImageRepository.findObjectKeysByCollectionId(100L))
                .thenReturn(List.of("k1", "k2"));
        when(collectionRepository.findById(100L)).thenReturn(Optional.of(col));

        sut.deleteCollectionByReport(50L);

        verify(collectionReportRepository).deleteByCollectionId(100L);
        verify(commentReportRepository).deleteByCollectionId(100L);
        verify(collectionRepository).remove(col);
        verify(imageService).deleteAll(List.of("k1", "k2"));
    }

    @Test
    @DisplayName("deleteCollectionByReport: 신고 없음 → 404")
    void deleteCollectionByReport_notFound() {
        when(collectionReportRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.deleteCollectionByReport(404L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("deleteCommentByReport: 신고된 댓글 삭제 + 신고 정리")
    void deleteCommentByReport_success() {
        UserBirdCollectionComment cm = UserBirdCollectionComment.of(null, collection(10L), "x");
        ReflectionTestUtils.setField(cm, "id", 900L);

        UserBirdCollectionCommentReport rep = newInstance(UserBirdCollectionCommentReport.class);
        ReflectionTestUtils.setField(rep, "id", 70L);
        ReflectionTestUtils.setField(rep, "comment", cm);

        when(commentReportRepository.findById(70L)).thenReturn(Optional.of(rep));
        when(commentRepository.findById(900L)).thenReturn(Optional.of(cm));

        sut.deleteCommentByReport(70L);

        verify(commentReportRepository).deleteByCommentId(900L);
        verify(commentRepository).remove(cm);
    }

    @Test
    @DisplayName("deleteCommentByReport: 신고 없음 → 404")
    void deleteCommentByReport_notFound() {
        when(commentReportRepository.findById(71L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.deleteCommentByReport(71L))
                .isInstanceOf(NotFoundException.class);
    }
}
