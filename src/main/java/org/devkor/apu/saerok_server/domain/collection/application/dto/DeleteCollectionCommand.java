package org.devkor.apu.saerok_server.domain.collection.application.dto;

public record DeleteCollectionCommand(
        Long userId,
        Long collectionId
) {
}
