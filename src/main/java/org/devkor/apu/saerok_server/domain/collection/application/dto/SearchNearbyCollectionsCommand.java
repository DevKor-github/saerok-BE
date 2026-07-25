package org.devkor.apu.saerok_server.domain.collection.application.dto;

public record SearchNearbyCollectionsCommand(
        Long userId,
        Double latitude,
        Double longitude,
        Double initialRadiusMeters,
        String query,
        Integer limit
) {
}
