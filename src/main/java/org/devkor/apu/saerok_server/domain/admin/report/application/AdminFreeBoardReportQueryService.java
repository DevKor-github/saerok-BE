package org.devkor.apu.saerok_server.domain.admin.report.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedFreeBoardCommentDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedFreeBoardCommentListResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedFreeBoardPostDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedFreeBoardPostListResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentsResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostDetailResponse;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostCommentQueryService;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostQueryService;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardCommentQueryCommand;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostCommentReport;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostReport;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentReportRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostReportRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminFreeBoardReportQueryService {

    private final FreeBoardPostReportRepository postReportRepository;
    private final FreeBoardPostCommentReportRepository commentReportRepository;

    private final FreeBoardPostQueryService postQueryService;
    private final FreeBoardPostCommentQueryService commentQueryService;

    /* ───────────── 목록 조회 ───────────── */

    public ReportedFreeBoardPostListResponse listPostReports() {
        List<FreeBoardPostReport> reports = postReportRepository.findAllOrderByCreatedAtDesc();

        List<ReportedFreeBoardPostListResponse.Item> items = reports.stream()
                .map(r -> {
                    FreeBoardPost post = r.getPost();
                    return new ReportedFreeBoardPostListResponse.Item(
                            r.getId(),
                            OffsetDateTimeLocalizer.toSeoulLocalDateTime(r.getCreatedAt()),
                            post.getId(),
                            post.getContent(),
                            new ReportedFreeBoardPostListResponse.UserMini(r.getReporter().getId(), r.getReporter().getNickname()),
                            new ReportedFreeBoardPostListResponse.UserMini(r.getReportedUser().getId(), r.getReportedUser().getNickname())
                    );
                })
                .toList();

        return new ReportedFreeBoardPostListResponse(items);
    }

    public ReportedFreeBoardCommentListResponse listCommentReports() {
        List<FreeBoardPostCommentReport> reports = commentReportRepository.findAllOrderByCreatedAtDesc();

        List<ReportedFreeBoardCommentListResponse.Item> items = reports.stream()
                .map(r -> {
                    FreeBoardPostComment c = r.getComment();
                    return new ReportedFreeBoardCommentListResponse.Item(
                            r.getId(),
                            OffsetDateTimeLocalizer.toSeoulLocalDateTime(r.getCreatedAt()),
                            c.getId(),
                            c.getPost().getId(),
                            r.getCommentContent(),
                            new ReportedFreeBoardCommentListResponse.UserMini(r.getReporter().getId(), r.getReporter().getNickname()),
                            new ReportedFreeBoardCommentListResponse.UserMini(r.getReportedUser().getId(), r.getReportedUser().getNickname())
                    );
                })
                .toList();

        return new ReportedFreeBoardCommentListResponse(items);
    }

    /* ───────────── 상세(게시글 + 댓글목록) ───────────── */

    public ReportedFreeBoardPostDetailResponse getReportedPostDetail(Long reportId) {
        FreeBoardPostReport report = postReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 자유게시판 게시글 신고가 없어요"));

        Long postId = report.getPost().getId();

        GetFreeBoardPostDetailResponse postDetail = postQueryService.getPostDetail(postId, null);
        GetFreeBoardPostCommentsResponse comments = commentQueryService.getComments(postId, null, new FreeBoardCommentQueryCommand(null, null));

        return new ReportedFreeBoardPostDetailResponse(report.getId(), postDetail, comments);
    }

    /* ───────────── 상세(댓글 + 부모 게시글 + 댓글목록) ───────────── */

    public ReportedFreeBoardCommentDetailResponse getReportedCommentDetail(Long reportId) {
        FreeBoardPostCommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 자유게시판 댓글 신고가 없어요"));

        FreeBoardPostComment reportedComment = report.getComment();
        Long postId = reportedComment.getPost().getId();

        GetFreeBoardPostDetailResponse postDetail = postQueryService.getPostDetail(postId, null);
        GetFreeBoardPostCommentsResponse comments = commentQueryService.getComments(postId, null, new FreeBoardCommentQueryCommand(null, null));

        ReportedFreeBoardCommentDetailResponse.ReportedComment commentDto =
                new ReportedFreeBoardCommentDetailResponse.ReportedComment(
                        reportedComment.getId(),
                        reportedComment.getUser().getId(),
                        reportedComment.getUser().getNickname(),
                        reportedComment.getContent(),
                        OffsetDateTimeLocalizer.toSeoulLocalDateTime(reportedComment.getCreatedAt()),
                        OffsetDateTimeLocalizer.toSeoulLocalDateTime(reportedComment.getUpdatedAt())
                );

        return new ReportedFreeBoardCommentDetailResponse(report.getId(), commentDto, postDetail, comments);
    }
}
