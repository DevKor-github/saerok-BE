package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

public record PresignResponse (
        String presignedUrl,
        String objectKey
) {
}
