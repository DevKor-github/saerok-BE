package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import java.util.List;

public record GetBirdIdSuggestionsResponse(List<Item> items) {
    public record Item(Long    birdId,
                       String  birdKoreanName,
                       String  birdScientificName,
                       String  birdImageUrl,
                       Long    agreeCount,
                       Boolean isAgreedByMe) {}
}