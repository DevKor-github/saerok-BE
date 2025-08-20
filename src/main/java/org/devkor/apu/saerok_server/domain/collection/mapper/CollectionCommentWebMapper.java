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

    /* 엔티티 목록 → 래핑된 응답 */
    default GetCollectionCommentsResponse toGetCollectionCommentsResponse(
            List<UserBirdCollectionComment> entities, 
            Map<Long, Long> likeCounts,
            Map<Long, Boolean> likeStatuses,
            Map<Long, Boolean> mineStatuses,
            Map<Long, String> profileImageUrls,
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
        }
        
        List<GetCollectionCommentsResponse.Item> items = entities.stream()
                .map(comment -> {
                    Long commentId = comment.getId();
                    Long userId = comment.getUser().getId();
                    int likeCount = likeCounts.get(commentId).intValue();
                    Boolean isLiked = likeStatuses.get(commentId);
                    Boolean isMine = mineStatuses.get(commentId);
                    String profileImageUrl = profileImageUrls.get(userId);
                    return toCommentItem(comment, likeCount, isLiked, isMine, profileImageUrl);
                })
                .toList();
        return new GetCollectionCommentsResponse(items, isMyCollection);
    }

    /* 단일 엔티티 → Item DTO */
    default GetCollectionCommentsResponse.Item toCommentItem(UserBirdCollectionComment c, int likeCount, Boolean isLiked, Boolean isMine, String profileImageUrl) {
        return new GetCollectionCommentsResponse.Item(
                c.getId(),
                c.getUser().getId(),
                c.getUser().getNickname(),
                profileImageUrl,
                c.getContent(),
                likeCount,
                isLiked,
                isMine,
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getCreatedAt()),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getUpdatedAt())
        );
    }
}