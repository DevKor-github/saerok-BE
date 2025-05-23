package org.devkor.apu.saerok_server.domain.collection.application.dto;

import lombok.Builder;
import lombok.Data;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;

import java.time.LocalDate;

@Data
@Builder
public class GetCollectionDetail {
    
    private Long collectionId;
    private String imageUrl;
    private LocalDate discoveredDate;
    private Double latitude;
    private Double longitude;
    private String locationAlias;
    private String note;
    private AccessLevelType accessLevel;
    
    private Long birdId;
    private String birdKoreanName;
    private Long userId;
    private String userNickname;

    public static GetCollectionDetail from(UserBirdCollection collection, String imageUrl) {
        return GetCollectionDetail.builder()
                .collectionId(collection.getId())
                .imageUrl(imageUrl)
                .discoveredDate(collection.getDiscoveredDate())
                .latitude(collection.getLatitude())
                .longitude(collection.getLongitude())
                .locationAlias(collection.getLocationAlias())
                .note(collection.getNote())
                .accessLevel(collection.getAccessLevel())
                .birdId(collection.getBirdIdOrNull())
                .birdKoreanName(collection.getBirdKoreanName())
                .userId(collection.getUser().getId())
                .userNickname(collection.getUser().getNickname())
                .build();
    }
}
