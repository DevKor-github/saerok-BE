package org.devkor.apu.saerok_server.domain.community.core.repository.dto;

import java.time.OffsetDateTime;

public record TrendingCollectionCandidate(
        Long collectionId,
        OffsetDateTime createdAt
) {
}
