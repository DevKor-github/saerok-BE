package org.devkor.apu.saerok_server.domain.collection.application.dto;

public record GetNearbyCollectionsCommand(
        Long userId,
        Double latitude,
        Double longitude,
        Double radiusMeters
) {
}
