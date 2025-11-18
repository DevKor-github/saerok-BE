package org.devkor.apu.saerok_server.domain.collection.application.dto;

import org.devkor.apu.saerok_server.domain.collection.application.NearbyCollectionsMode;

public record GetNearbyCollectionsCommand(
        Long userId,
        Double latitude,
        Double longitude,
        Double radiusMeters,
        boolean isMineOnly,
        Integer limit,
        NearbyCollectionsMode mode
) {
}
