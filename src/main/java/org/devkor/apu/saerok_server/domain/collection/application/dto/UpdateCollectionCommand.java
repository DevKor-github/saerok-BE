package org.devkor.apu.saerok_server.domain.collection.application.dto;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;

import java.time.LocalDate;

public record UpdateCollectionCommand (
        Long userId,
        Long collectionId,
        Boolean isBirdIdUpdated,
        Long birdId,
        LocalDate discoveredDate,
        Double latitude,
        Double longitude,
        String locationAlias,
        String address,
        String note,
        AccessLevelType accessLevel
){
}
