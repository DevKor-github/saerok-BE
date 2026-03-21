package org.devkor.apu.saerok_server.domain.admin.application;

import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.admin.report.application.AdminFreeBoardReportQueryService;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentsResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostDetailResponse;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostCommentQueryService;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostQueryService;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardCommentQueryCommand;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.*;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentReportRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostReportRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFreeBoardReportQueryServiceTest {

    @InjectMocks AdminFreeBoardReportQueryService sut;

    @Mock FreeBoardPostReportRepository postReportRepository;
    @Mock FreeBoardPostCommentReportRepository commentReportRepository;
    @Mock FreeBoardPostQueryService postQueryService;
    @Mock FreeBoardPostCommentQueryService commentQueryService;

    /* ---------------- helpers ---------------- */

    private static User user(long id, String nickname) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        u.setNickname(nickname);
        return u;
    }

    private static FreeBoardPost post(long id, User owner) {
        FreeBoardPost p = FreeBoardPost.of(owner, "게시글 내용");
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private static FreeBoardPostComment comment(long id, User author, FreeBoardPost post, String content) {
        FreeBoardPostComment cm = FreeBoardPostComment.of(author, post, content);
        ReflectionTestUtils.setField(cm, "id", id);
        ReflectionTestUtils.setField(cm, "createdAt", OffsetDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(cm, "updatedAt", OffsetDateTime.now());
        return cm;
    }

    /* -------------------- tests -------------------- */

    @Test @DisplayName("listPostReports: 신고 리스트가 매핑되어 반환된다")
    void listPostReports_success() {
        User reporter = user(1L, "rep");
        User reported = user(2L, "reported");
        FreeBoardPost p = post(10L, reported);

        FreeBoardPostReport r = FreeBoardPostReport.of(reporter, reported, p);
        ReflectionTestUtils.setField(r, "id", 100L);
        ReflectionTestUtils.setField(r, "createdAt", OffsetDateTime.now());

        when(postReportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(r));

        ReportedFreeBoardPostListResponse res = sut.listPostReports();

        assertThat(res.items()).hasSize(1);
        var item = res.items().getFirst();
        assertThat(item.reportId()).isEqualTo(100L);
        assertThat(item.postId()).isEqualTo(10L);
        assertThat(item.contentPreview()).isEqualTo("게시글 내용");
        assertThat(item.reporter().nickname()).isEqualTo("rep");
        assertThat(item.reportedUser().nickname()).isEqualTo("reported");
        assertThat(item.reportedAt()).isNotNull();
    }

    @Test @DisplayName("listCommentReports: 신고 리스트가 매핑되어 반환된다")
    void listCommentReports_success() {
        User reporter = user(1L, "rep");
        User reported = user(2L, "reported");
        FreeBoardPost p = post(10L, reported);
        FreeBoardPostComment cm = comment(200L, reported, p, "악성 댓글");

        FreeBoardPostCommentReport cr = FreeBoardPostCommentReport.of(reporter, reported, cm);
        ReflectionTestUtils.setField(cr, "id", 300L);
        ReflectionTestUtils.setField(cr, "createdAt", OffsetDateTime.now());

        when(commentReportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(cr));

        ReportedFreeBoardCommentListResponse res = sut.listCommentReports();

        assertThat(res.items()).hasSize(1);
        var item = res.items().getFirst();
        assertThat(item.reportId()).isEqualTo(300L);
        assertThat(item.commentId()).isEqualTo(200L);
        assertThat(item.postId()).isEqualTo(10L);
        assertThat(item.contentPreview()).isEqualTo("악성 댓글");
        assertThat(item.reporter().nickname()).isEqualTo("rep");
        assertThat(item.reportedUser().nickname()).isEqualTo("reported");
    }

    @Test @DisplayName("getReportedPostDetail: 게시글 상세 + 댓글 목록 조립")
    void getReportedPostDetail_success() {
        User owner = user(2L, "owner");
        User reporter = user(1L, "rep");
        FreeBoardPost p = post(10L, owner);

        FreeBoardPostReport rep = FreeBoardPostReport.of(reporter, owner, p);
        ReflectionTestUtils.setField(rep, "id", 999L);

        GetFreeBoardPostDetailResponse detail = new GetFreeBoardPostDetailResponse(
                10L, 2L, "owner", "profile", "thumb", "내용", 0L, false,
                LocalDateTime.now(), LocalDateTime.now()
        );
        GetFreeBoardPostCommentsResponse comments = new GetFreeBoardPostCommentsResponse(List.of(), false, null);

        when(postReportRepository.findById(999L)).thenReturn(Optional.of(rep));
        when(postQueryService.getPostDetail(10L, null)).thenReturn(detail);
        when(commentQueryService.getComments(eq(10L), isNull(), any(FreeBoardCommentQueryCommand.class)))
                .thenReturn(comments);

        ReportedFreeBoardPostDetailResponse res = sut.getReportedPostDetail(999L);

        assertThat(res.reportId()).isEqualTo(999L);
        assertThat(res.post()).isSameAs(detail);
        assertThat(res.comments()).isSameAs(comments);
    }

    @Test @DisplayName("getReportedPostDetail: 신고 없음 → 404")
    void getReportedPostDetail_notFound() {
        when(postReportRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.getReportedPostDetail(404L)).isInstanceOf(NotFoundException.class);
    }

    @Test @DisplayName("getReportedCommentDetail: 댓글 상세 + 부모 게시글 + 댓글목록 조립")
    void getReportedCommentDetail_success() {
        User owner = user(10L, "owner");
        User cmAuthor = user(11L, "cmAuthor");
        FreeBoardPost p = post(100L, owner);
        FreeBoardPostComment cm = comment(1000L, cmAuthor, p, "불건전 댓글");

        FreeBoardPostCommentReport rep = FreeBoardPostCommentReport.of(user(1L, "rep"), cmAuthor, cm);
        ReflectionTestUtils.setField(rep, "id", 777L);

        GetFreeBoardPostDetailResponse detail = new GetFreeBoardPostDetailResponse(
                100L, 10L, "owner", "p", "pt", "게시글", 1L, false,
                LocalDateTime.now(), LocalDateTime.now()
        );
        GetFreeBoardPostCommentsResponse comments = new GetFreeBoardPostCommentsResponse(List.of(), false, null);

        when(commentReportRepository.findById(777L)).thenReturn(Optional.of(rep));
        when(postQueryService.getPostDetail(100L, null)).thenReturn(detail);
        when(commentQueryService.getComments(eq(100L), isNull(), any(FreeBoardCommentQueryCommand.class)))
                .thenReturn(comments);

        ReportedFreeBoardCommentDetailResponse res = sut.getReportedCommentDetail(777L);

        assertThat(res.reportId()).isEqualTo(777L);
        assertThat(res.post()).isSameAs(detail);
        assertThat(res.comments()).isSameAs(comments);

        var reported = res.comment();
        assertThat(reported.commentId()).isEqualTo(1000L);
        assertThat(reported.userId()).isEqualTo(11L);
        assertThat(reported.nickname()).isEqualTo("cmAuthor");
        assertThat(reported.content()).isEqualTo("불건전 댓글");
        assertThat(reported.createdAt()).isNotNull();
        assertThat(reported.updatedAt()).isNotNull();
    }

    @Test @DisplayName("getReportedCommentDetail: 신고 없음 → 404")
    void getReportedCommentDetail_notFound() {
        when(commentReportRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.getReportedCommentDetail(123L)).isInstanceOf(NotFoundException.class);
    }
}
