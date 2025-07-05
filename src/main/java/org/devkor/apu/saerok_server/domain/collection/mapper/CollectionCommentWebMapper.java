package org.devkor.apu.saerok_server.domain.collection.mapper;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CollectionCommentWebMapper {

    /* 엔티티 목록 → 래핑된 응답 */
    default GetCollectionCommentsResponse toGetCollectionCommentsResponse(List<UserBirdCollectionComment> entities) {
        if (entities == null || entities.isEmpty()) {
            return new GetCollectionCommentsResponse(List.of());
        }
        List<GetCollectionCommentsResponse.Item> items = entities.stream()
                .map(this::toCommentItem)
                .toList();
        return new GetCollectionCommentsResponse(items);
    }

    /* 단일 엔티티 → Item DTO */
    default GetCollectionCommentsResponse.Item toCommentItem(UserBirdCollectionComment c) {
        return new GetCollectionCommentsResponse.Item(
                c.getId(),
                c.getUser().getId(),
                c.getContent(),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getCreatedAt()),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getUpdatedAt())
        );
    }
}