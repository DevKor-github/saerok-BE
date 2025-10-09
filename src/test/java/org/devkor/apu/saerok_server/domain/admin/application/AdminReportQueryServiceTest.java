package org.devkor.apu.saerok_server.domain.admin.application;

import org.devkor.apu.saerok_server.domain.admin.api.dto.response.ReportedCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.api.dto.response.ReportedCollectionListResponse;
import org.devkor.apu.saerok_server.domain.admin.api.dto.response.ReportedCommentDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.api.dto.response.ReportedCommentListResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionCommentQueryService;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentReport;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionReport;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportQueryServiceTest {

    @InjectMocks
    AdminReportQueryService sut;

    /* Repositories */
    @Mock CollectionReportRepository         collectionReportRepository;
    @Mock CollectionCommentReportRepository  commentReportRepository;
    @Mock CollectionLikeRepository           collectionLikeRepository;
    @Mock CollectionCommentRepository        collectionCommentRepository; // count용

    /* Collaborators */
    @Mock CollectionWebMapper                collectionWebMapper;
    @Mock CollectionCommentQueryService      commentQueryService;
    @Mock CollectionImageUrlService          collectionImageUrlService;
    @Mock UserProfileImageUrlService         userProfileImageUrlService;

    /* ---------------- helpers ---------------- */

    private static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    private static User user(long id, String nickname) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        u.setNickname(nickname);
        return u;
    }

    private static UserBirdCollection collection(long id, User owner) {
        UserBirdCollection c = new UserBirdCollection();
        ReflectionTestUtils.setField(c, "id", id);
        ReflectionTestUtils.setField(c, "user", owner);
        return c;
    }

    private static UserBirdCollectionComment comment(long id, User author, UserBirdCollection col, String content) {
        UserBirdCollectionComment cm = UserBirdCollectionComment.of(author, col, content);
        ReflectionTestUtils.setField(cm, "id", id);
        ReflectionTestUtils.setField(cm, "createdAt", OffsetDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(cm, "updatedAt", OffsetDateTime.now());
        return cm;
    }

    /* -------------------- tests -------------------- */

    @Test
    @DisplayName("listCollectionReports: 신고 리스트가 매핑되어 반환된다")
    void listCollectionReports_success() {
        User reporter = user(1L, "rep");
        User reported = user(2L, "reported");
        UserBirdCollection col = collection(10L, reported);

        UserBirdCollectionReport r1 = newInstance(UserBirdCollectionReport.class);
        ReflectionTestUtils.setField(r1, "id", 100L);
        ReflectionTestUtils.setField(r1, "reporter", reporter);
        ReflectionTestUtils.setField(r1, "reportedUser", reported);
        ReflectionTestUtils.setField(r1, "collection", col);
        ReflectionTestUtils.setField(r1, "createdAt", OffsetDateTime.now().minusHours(2));

        UserBirdCollectionReport r2 = newInstance(UserBirdCollectionReport.class);
        ReflectionTestUtils.setField(r2, "id", 101L);
        ReflectionTestUtils.setField(r2, "reporter", reporter);
        ReflectionTestUtils.setField(r2, "reportedUser", reported);
        ReflectionTestUtils.setField(r2, "collection", col);
        ReflectionTestUtils.setField(r2, "createdAt", OffsetDateTime.now());

        when(collectionReportRepository.findAllOrderByCreatedAtDesc())
                .thenReturn(List.of(r2, r1)); // repo 정렬 결과를 그대로 사용

        ReportedCollectionListResponse res = sut.listCollectionReports();

        assertThat(res.items()).hasSize(2);
        assertThat(res.items().getFirst().reportId()).isEqualTo(101L);
        assertThat(res.items().getFirst().collectionId()).isEqualTo(10L);
        assertThat(res.items().getFirst().reporter().nickname()).isEqualTo("rep");
        assertThat(res.items().getFirst().reportedUser().nickname()).isEqualTo("reported");
        assertThat(res.items().getFirst().reportedAt()).isNotNull();
    }

    @Test
    @DisplayName("listCommentReports: 신고 리스트가 매핑되어 반환된다")
    void listCommentReports_success() {
        User reporter = user(1L, "rep");
        User reported = user(2L, "reported");
        UserBirdCollection col = collection(10L, reported);
        UserBirdCollectionComment cm = comment(200L, reported, col, "악성 댓글");

        UserBirdCollectionCommentReport cr = newInstance(UserBirdCollectionCommentReport.class);
        ReflectionTestUtils.setField(cr, "id", 300L);
        ReflectionTestUtils.setField(cr, "reporter", reporter);
        ReflectionTestUtils.setField(cr, "reportedUser", reported);
        ReflectionTestUtils.setField(cr, "comment", cm);
        ReflectionTestUtils.setField(cr, "createdAt", OffsetDateTime.now());

        when(commentReportRepository.findAllOrderByCreatedAtDesc())
                .thenReturn(List.of(cr));

        ReportedCommentListResponse res = sut.listCommentReports();

        assertThat(res.items()).hasSize(1);
        var item = res.items().getFirst();
        assertThat(item.reportId()).isEqualTo(300L);
        assertThat(item.commentId()).isEqualTo(200L);
        assertThat(item.collectionId()).isEqualTo(10L);
        assertThat(item.contentPreview()).isEqualTo("악성 댓글");
        assertThat(item.reportedAt()).isNotNull();
        assertThat(item.reporter().nickname()).isEqualTo("rep");
        assertThat(item.reportedUser().nickname()).isEqualTo("reported");
    }

    @Test
    @DisplayName("getReportedCollectionDetail: 새록 상세+댓글 목록 조립")
    void getReportedCollectionDetail_success() {
        User owner = user(2L, "owner");
        User reporter = user(1L, "rep");
        UserBirdCollection col = collection(10L, owner);

        UserBirdCollectionReport rep = newInstance(UserBirdCollectionReport.class);
        ReflectionTestUtils.setField(rep, "id", 999L);
        ReflectionTestUtils.setField(rep, "reporter", reporter);
        ReflectionTestUtils.setField(rep, "reportedUser", owner);
        ReflectionTestUtils.setField(rep, "collection", col);

        GetCollectionDetailResponse detail = new GetCollectionDetailResponse();
        GetCollectionCommentsResponse comments = new GetCollectionCommentsResponse(List.of(), false);

        when(collectionReportRepository.findById(999L)).thenReturn(Optional.of(rep));
        when(collectionImageUrlService.getPrimaryImageUrlFor(col)).thenReturn(Optional.of("img"));
        when(collectionLikeRepository.countByCollectionId(10L)).thenReturn(10L);
        when(collectionCommentRepository.countByCollectionId(10L)).thenReturn(2L);
        when(userProfileImageUrlService.getProfileImageUrlFor(owner)).thenReturn("profile");
        when(collectionWebMapper.toGetCollectionDetailResponse(col, "img", "profile", 10L, 2L, false, false))
                .thenReturn(detail);
        when(commentQueryService.getComments(10L, null)).thenReturn(comments);

        ReportedCollectionDetailResponse res = sut.getReportedCollectionDetail(999L);

        assertThat(res.reportId()).isEqualTo(999L);
        assertThat(res.collection()).isSameAs(detail);
        assertThat(res.comments()).isSameAs(comments);
    }

    @Test
    @DisplayName("getReportedCollectionDetail: 신고 없음 → 404")
    void getReportedCollectionDetail_notFound() {
        when(collectionReportRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.getReportedCollectionDetail(404L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getReportedCommentDetail: 신고 댓글 + 부모 새록 + 댓글목록 조립")
    void getReportedCommentDetail_success() {
        User owner = user(10L, "owner");
        User cmAuthor = user(11L, "cmAuthor");
        UserBirdCollection col = collection(100L, owner);
        UserBirdCollectionComment cm = comment(1000L, cmAuthor, col, "불건전 댓글");

        UserBirdCollectionCommentReport rep = newInstance(UserBirdCollectionCommentReport.class);
        ReflectionTestUtils.setField(rep, "id", 777L);
        ReflectionTestUtils.setField(rep, "reporter", user(1L,"rep"));
        ReflectionTestUtils.setField(rep, "reportedUser", cmAuthor);
        ReflectionTestUtils.setField(rep, "comment", cm);

        GetCollectionDetailResponse detail = new GetCollectionDetailResponse();
        GetCollectionCommentsResponse comments = new GetCollectionCommentsResponse(List.of(), false);

        when(commentReportRepository.findById(777L)).thenReturn(Optional.of(rep));
        when(collectionImageUrlService.getPrimaryImageUrlFor(col)).thenReturn(Optional.empty());
        when(collectionLikeRepository.countByCollectionId(100L)).thenReturn(5L);
        when(collectionCommentRepository.countByCollectionId(100L)).thenReturn(1L);
        when(userProfileImageUrlService.getProfileImageUrlFor(owner)).thenReturn("p");
        when(collectionWebMapper.toGetCollectionDetailResponse(col, null, "p", 5L, 1L, false, false))
                .thenReturn(detail);
        when(commentQueryService.getComments(100L, null)).thenReturn(comments);

        ReportedCommentDetailResponse res = sut.getReportedCommentDetail(777L);

        assertThat(res.reportId()).isEqualTo(777L);
        assertThat(res.collection()).isSameAs(detail);
        assertThat(res.comments()).isSameAs(comments);

        var reported = res.comment();
        assertThat(reported.commentId()).isEqualTo(1000L);
        assertThat(reported.userId()).isEqualTo(11L);
        assertThat(reported.nickname()).isEqualTo("cmAuthor");
        assertThat(reported.content()).isEqualTo("불건전 댓글");
        assertThat(reported.createdAt()).isNotNull();
        assertThat(reported.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("getReportedCommentDetail: 신고 없음 → 404")
    void getReportedCommentDetail_notFound() {
        when(commentReportRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.getReportedCommentDetail(123L))
                .isInstanceOf(NotFoundException.class);
    }
}
