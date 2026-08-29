package org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.ConservationGrade;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.HabitatType;

public record AdminBirdListResponse(
        List<Item> birds,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record Item(
            Long id,
            String koreanName,
            String scientificName,
            ConservationGrade conservationGrade,
            Double bodyLengthCm,
            List<HabitatType> habitats,
            String thumbImageUrl,
            OffsetDateTime updatedAt
    ) {
    }
}
