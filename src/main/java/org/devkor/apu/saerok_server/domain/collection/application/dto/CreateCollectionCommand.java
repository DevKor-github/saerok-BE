package org.devkor.apu.saerok_server.domain.collection.application.dto;

import java.time.LocalDate;

public record CreateCollectionCommand (
        Long userId,
        Long birdId,
        String tempBirdName,
        LocalDate discoveredDate,
        Double latitude,
        Double longitude,
        String locationAlias,
        String note
){
}
