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
        List<GetCollectionCommentsResponse.Item> items = entities.stream()
                .map(comment -> {
                    int likeCount = likeCounts.getOrDefault(comment.getId(), 0L).intValue();
                    Boolean isLiked = likeStatuses.getOrDefault(comment.getId(), false);
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
                c.getContent(),
                likeCount,
                isLiked,
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getCreatedAt()),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getUpdatedAt())
        );
    }
}