package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동정 의견 제안에 대한 응답")
public record SuggestBirdIdResponse(

        @Schema(description = "생성된 동정 의견 ID", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long suggestionId

) {}
