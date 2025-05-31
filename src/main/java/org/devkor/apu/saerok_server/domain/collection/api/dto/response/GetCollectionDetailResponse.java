package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import lombok.Builder;
import lombok.Data;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;

import java.time.LocalDate;

@Data
@Builder
public class GetCollectionDetailResponse {

    private Long collectionId;
    private String imageUrl;
    private LocalDate discoveredDate;
    private Double latitude;
    private Double longitude;
    private String locationAlias;
    private String address;
    private String note;
    private AccessLevelType accessLevel;
    
    private BirdInfo bird;
    private UserInfo user;

    @Data
    @Builder
    public static class BirdInfo {
        private Long birdId;
        private String koreanName;
        private String scientificName;
    }

    @Data
    @Builder
    public static class UserInfo {
        private Long userId;
        private String nickname;
    }
}
