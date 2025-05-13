package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "컬렉션(관찰 기록) 등록 요청 DTO")
public record CreateCollectionResponse (

        @Schema(description = "생성된 컬렉션의 ID", example = "42")
        Long collectionId
){
}
