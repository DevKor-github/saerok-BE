package org.devkor.apu.saerok_server.domain.collection.application.dto;

public record CreateCollectionImageCommand(
        String objectKey,
        String contentType
) {
}
