package org.devkor.apu.saerok_server.domain.collection.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동정 의견 제안/동의")
public record SuggestOrAgreeRequest(
        @Schema(description = "제안 또는 동의할 birdId", example = "123")
        Long birdId
) {}
