package org.devkor.apu.saerok_server.domain.collection.application.dto;

public record GetCollectionEditDataCommand(
        Long userId,
        Long collectionId
) {
}
