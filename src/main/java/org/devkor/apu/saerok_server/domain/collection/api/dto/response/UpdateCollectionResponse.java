package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import java.time.LocalDate;
import java.util.List;

public record UpdateCollectionResponse (
        Long collectionId,
        Long birdId,
        LocalDate discoveredDate,
        Double longitude,
        Double latitude,
        String locationAlias,
        String note,
        List<String> imageUrls
) {
}
