package org.devkor.apu.saerok_server.domain.collection.mapper;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CollectionCommentWebMapper {

    /* 엔티티 목록 → 래핑된 응답 (원댓글 + 대댓글 트리 구조) */
    default GetCollectionCommentsResponse toGetCollectionCommentsResponse(
            List<UserBirdCollectionComment> entities,
            Map<Long, Long> likeCounts,
            Map<Long, Boolean> likeStatuses,
            Map<Long, Boolean> mineStatuses,
            Map<Long, String> profileImageUrls,
            Map<Long, String> thumbnailProfileImageUrls,
            Boolean isMyCollection) {
        if (entities == null || entities.isEmpty()) {
            return new GetCollectionCommentsResponse(List.of(), isMyCollection);
        }

        for (UserBirdCollectionComment entity : entities) {
            Long commentId = entity.getId();
            Long userId = entity.getUser().getId();
            if (commentId == null) {
                throw new IllegalStateException("댓글 ID가 null입니다.");
            }
            if (!likeCounts.containsKey(commentId)) {
                throw new IllegalStateException("likeCounts에 댓글 ID " + commentId + "가 없습니다.");
            }
            if (!likeStatuses.containsKey(commentId)) {
                throw new IllegalStateException("likeStatuses에 댓글 ID " + commentId + "가 없습니다.");
            }
            if (!mineStatuses.containsKey(commentId)) {
                throw new IllegalStateException("mineStatuses에 댓글 ID " + commentId + "가 없습니다.");
            }
            if (!profileImageUrls.containsKey(userId)) {
                throw new IllegalStateException("profileImageUrls에 사용자 ID " + userId + "가 없습니다.");
            }
            if (!thumbnailProfileImageUrls.containsKey(userId)) {
                throw new IllegalStateException("thumbnailProfileImageUrls에 사용자 ID " + userId + "가 없습니다.");
            }
        }

        // 1. 원댓글과 대댓글 분리
        List<UserBirdCollectionComment> rootComments = entities.stream()
                .filter(c -> c.getParent() == null)
                .toList();

        Map<Long, List<UserBirdCollectionComment>> repliesByParentId = entities.stream()
                .filter(c -> c.getParent() != null)
                .collect(java.util.stream.Collectors.groupingBy(c -> c.getParent().getId()));

        // 2. 원댓글을 Item으로 변환하고 대댓글 추가
        List<GetCollectionCommentsResponse.Item> items = rootComments.stream()
                .map(comment -> {
                    // 대댓글 목록 생성
                    List<GetCollectionCommentsResponse.Item> replies = repliesByParentId.getOrDefault(comment.getId(), List.of())
                            .stream()
                            .sorted(java.util.Comparator.comparing(UserBirdCollectionComment::getCreatedAt))
                            .map(reply -> buildCommentItem(reply, likeCounts, likeStatuses, mineStatuses, profileImageUrls, thumbnailProfileImageUrls, List.of()))
                            .toList();

                    return buildCommentItem(comment, likeCounts, likeStatuses, mineStatuses, profileImageUrls, thumbnailProfileImageUrls, replies);
                })
                .toList();
        return new GetCollectionCommentsResponse(items, isMyCollection);
    }

    /* 댓글 엔티티 → Item DTO (공통 매핑 로직) */
    private GetCollectionCommentsResponse.Item buildCommentItem(
            UserBirdCollectionComment c,
            Map<Long, Long> likeCounts,
            Map<Long, Boolean> likeStatuses,
            Map<Long, Boolean> mineStatuses,
            Map<Long, String> profileImageUrls,
            Map<Long, String> thumbnailProfileImageUrls,
            List<GetCollectionCommentsResponse.Item> replies) {
        Long commentId = c.getId();
        Long userId = c.getUser().getId();
        int likeCount = likeCounts.get(commentId).intValue();
        Boolean isLiked = likeStatuses.get(commentId);
        Boolean isMine = mineStatuses.get(commentId);
        String profileImageUrl = profileImageUrls.get(userId);
        String thumbnailProfileImageUrl = thumbnailProfileImageUrls.get(userId);

        return new GetCollectionCommentsResponse.Item(
                commentId,
                userId,
                c.getUser().getNickname(),
                profileImageUrl,
                thumbnailProfileImageUrl,
                c.getContent(),
                c.getStatus().name(),
                c.getParent() != null ? c.getParent().getId() : null,
                likeCount,
                isLiked,
                isMine,
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getCreatedAt()),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getUpdatedAt()),
                replies
        );
    }
}