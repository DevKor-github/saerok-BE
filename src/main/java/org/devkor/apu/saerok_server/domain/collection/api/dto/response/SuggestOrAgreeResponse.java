package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "bird_id 제안 또는 동의 요청에 대한 응답")
public record SuggestOrAgreeResponse(

        @Schema(description = "생성된 동정 의견 ID", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long suggestionId

) {}
