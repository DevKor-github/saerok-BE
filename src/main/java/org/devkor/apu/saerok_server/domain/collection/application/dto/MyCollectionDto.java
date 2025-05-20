package org.devkor.apu.saerok_server.domain.collection.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyCollectionDto {
    private Long collectionId;
    private String imageUrl;
    private String birdName;
}
