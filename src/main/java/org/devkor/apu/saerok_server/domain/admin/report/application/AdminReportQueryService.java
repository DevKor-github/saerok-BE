package org.devkor.apu.saerok_server.domain.admin.report.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedCollectionListResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedCommentDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedCommentListResponse;
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
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminReportQueryService {

    private final CollectionReportRepository collectionReportRepository;
    private final CollectionCommentReportRepository commentReportRepository;

    private final CollectionLikeRepository collectionLikeRepository;
    private final CollectionCommentRepository collectionCommentRepository;

    private final CollectionWebMapper collectionWebMapper;
    private final CollectionCommentQueryService commentQueryService;
    private final CollectionImageUrlService collectionImageUrlService;
    private final UserProfileImageUrlService userProfileImageUrlService;

    /* ───────────── 목록 조회 ───────────── */

    public ReportedCollectionListResponse listCollectionReports() {
        List<UserBirdCollectionReport> reports = collectionReportRepository.findAllOrderByCreatedAtDesc();

        List<ReportedCollectionListResponse.Item> items = reports.stream()
                .map(r -> new ReportedCollectionListResponse.Item(
                        r.getId(),
                        OffsetDateTimeLocalizer.toSeoulLocalDateTime(r.getCreatedAt()),
                        r.getCollection().getId(),
                        new ReportedCollectionListResponse.UserMini(r.getReporter().getId(), r.getReporter().getNickname()),
                        new ReportedCollectionListResponse.UserMini(r.getReportedUser().getId(), r.getReportedUser().getNickname())
                ))
                .toList();

        return new ReportedCollectionListResponse(items);
    }

    public ReportedCommentListResponse listCommentReports() {
        List<UserBirdCollectionCommentReport> reports = commentReportRepository.findAllOrderByCreatedAtDesc();

        List<ReportedCommentListResponse.Item> items = reports.stream()
                .map(r -> {
                    UserBirdCollectionComment c = r.getComment();
                    return new ReportedCommentListResponse.Item(
                            r.getId(),
                            OffsetDateTimeLocalizer.toSeoulLocalDateTime(r.getCreatedAt()),
                            c.getId(),
                            c.getCollection().getId(),
                            c.getContent(),
                            new ReportedCommentListResponse.UserMini(r.getReporter().getId(), r.getReporter().getNickname()),
                            new ReportedCommentListResponse.UserMini(r.getReportedUser().getId(), r.getReportedUser().getNickname())
                    );
                })
                .toList();

        return new ReportedCommentListResponse(items);
    }

    /* ───────────── 상세(새록 + 댓글목록) ───────────── */

    public ReportedCollectionDetailResponse getReportedCollectionDetail(Long reportId) {
        UserBirdCollectionReport report = collectionReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 새록 신고가 없어요"));

        UserBirdCollection collection = report.getCollection();

        // GetCollectionDetailResponse 직접 조립 (접근 제한 무시)
        String imageUrl = collectionImageUrlService.getPrimaryImageUrlFor(collection).orElse(null);
        long likeCount = collectionLikeRepository.countByCollectionId(collection.getId());
        long commentCount = collectionCommentRepository.countByCollectionId(collection.getId());
        String authorProfileImage = userProfileImageUrlService.getProfileImageUrlFor(collection.getUser());
        String thumbnailProfileImage = userProfileImageUrlService.getProfileThumbnailImageUrlFor(collection.getUser());

        GetCollectionDetailResponse collectionDetail =
                collectionWebMapper.toGetCollectionDetailResponse(
                        collection, imageUrl, authorProfileImage, thumbnailProfileImage, likeCount, commentCount, false, false
                );

        // 댓글 목록 (관리자 기준 isLiked/isMine 계산 불필요)
        GetCollectionCommentsResponse comments = commentQueryService.getComments(collection.getId(), null);

        return new ReportedCollectionDetailResponse(report.getId(), collectionDetail, comments);
    }

    /* ───────────── 상세(댓글 + 부모 새록 + 댓글목록) ───────────── */

    public ReportedCommentDetailResponse getReportedCommentDetail(Long reportId) {
        UserBirdCollectionCommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 댓글 신고가 없어요"));

        UserBirdCollectionComment reportedComment = report.getComment();
        UserBirdCollection parentCollection = reportedComment.getCollection();

        // 부모 새록 상세 (접근 제한 무시)
        String imageUrl = collectionImageUrlService.getPrimaryImageUrlFor(parentCollection).orElse(null);
        long likeCount = collectionLikeRepository.countByCollectionId(parentCollection.getId());
        long commentCount = collectionCommentRepository.countByCollectionId(parentCollection.getId());
        String authorProfileImage = userProfileImageUrlService.getProfileImageUrlFor(parentCollection.getUser());
        String thumbnailProfileImage = userProfileImageUrlService.getProfileThumbnailImageUrlFor(parentCollection.getUser());

        GetCollectionDetailResponse collectionDetail =
                collectionWebMapper.toGetCollectionDetailResponse(
                        parentCollection, imageUrl, authorProfileImage, thumbnailProfileImage, likeCount, commentCount, false, false
                );

        // 댓글 목록
        GetCollectionCommentsResponse comments = commentQueryService.getComments(parentCollection.getId(), null);

        // 신고된 댓글 정보
        ReportedCommentDetailResponse.ReportedComment commentDto =
                new ReportedCommentDetailResponse.ReportedComment(
                        reportedComment.getId(),
                        reportedComment.getUser().getId(),
                        reportedComment.getUser().getNickname(),
                        reportedComment.getContent(),
                        OffsetDateTimeLocalizer.toSeoulLocalDateTime(reportedComment.getCreatedAt()),
                        OffsetDateTimeLocalizer.toSeoulLocalDateTime(reportedComment.getUpdatedAt())
                );

        return new ReportedCommentDetailResponse(report.getId(), commentDto, collectionDetail, comments);
    }
}
