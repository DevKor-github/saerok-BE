package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import java.util.List;

public record GetPendingCollectionsResponse(List<Item> items) {
    public record Item(Long   collectionId,
                       String imageUrl,
                       String note,
                       String nickname) {}
}