package org.devkor.apu.saerok_server.domain.collection.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동정 의견 제안")
public record SuggestBirdIdRequest(
        @Schema(description = "제안할 birdId", example = "123")
        Long birdId
) {}
