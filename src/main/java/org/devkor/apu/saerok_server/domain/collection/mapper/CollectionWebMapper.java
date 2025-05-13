package org.devkor.apu.saerok_server.domain.collection.mapper;

import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.CreateCollectionResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.CreateCollectionCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CollectionWebMapper {

    @Mapping(target = "userId", source = "userId")
    CreateCollectionCommand toCreateCollectionCommand(CreateCollectionRequest request, Long userId);

    @Mapping(target = "collectionId", source = "collectionId")
    CreateCollectionResponse toCreateCollectionResponse(Long collectionId);
}
