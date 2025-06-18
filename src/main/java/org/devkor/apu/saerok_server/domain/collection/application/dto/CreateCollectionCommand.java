package org.devkor.apu.saerok_server.domain.collection.application.dto;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;

import java.time.LocalDate;

public record CreateCollectionCommand (
        Long userId,
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
