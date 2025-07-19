package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동정 의견 채택에 대한 응답")
public record AdoptSuggestionResponse(

        @Schema(description = "채택된 컬렉션 ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
        Long collectionId,

        @Schema(description = "채택된 새(bird)의 ID", example = "17", requiredMode = Schema.RequiredMode.REQUIRED)
        Long birdId,

        @Schema(description = "채택된 새의 이름 (한글)", example = "참새", requiredMode = Schema.RequiredMode.REQUIRED)
        String birdName

) {}
