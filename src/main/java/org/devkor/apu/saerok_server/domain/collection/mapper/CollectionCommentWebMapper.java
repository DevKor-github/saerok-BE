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
            Map<Long, Boolean> likeStatuses) {
        if (entities == null || entities.isEmpty()) {
            return new GetCollectionCommentsResponse(List.of());
        }

        for (UserBirdCollectionComment entity : entities) {
            Long commentId = entity.getId();
            if (commentId == null) {
                throw new IllegalStateException("댓글 ID가 null입니다.");
            }
            if (!likeCounts.containsKey(commentId)) {
                throw new IllegalStateException("likeCounts에 댓글 ID " + commentId + "가 없습니다.");
            }
            if (!likeStatuses.containsKey(commentId)) {
                throw new IllegalStateException("likeStatuses에 댓글 ID " + commentId + "가 없습니다.");
            }
        }
        
        List<GetCollectionCommentsResponse.Item> items = entities.stream()
                .map(comment -> {
                    int likeCount = likeCounts.get(comment.getId()).intValue();
                    Boolean isLiked = likeStatuses.get(comment.getId());
                    return toCommentItem(comment, likeCount, isLiked);
                })
                .toList();
        return new GetCollectionCommentsResponse(items);
    }

    /* 단일 엔티티 → Item DTO */
    default GetCollectionCommentsResponse.Item toCommentItem(UserBirdCollectionComment c, int likeCount, Boolean isLiked) {
        return new GetCollectionCommentsResponse.Item(
                c.getId(),
                c.getUser().getId(),
                c.getUser().getNickname(),
                c.getContent(),
                likeCount,
                isLiked,
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getCreatedAt()),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getUpdatedAt())
        );
    }
}