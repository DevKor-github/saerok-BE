package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GetNearbyCollectionsResponse {

    private List<Item> items;

    @Data
    public static class Item {
        private Long collectionId;
        private String imageUrl;
        private String koreanName;
        private String note;
    }
}
