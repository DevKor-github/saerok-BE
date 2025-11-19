package org.devkor.apu.saerok_server.domain.collection.application.strategy;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.application.dto.GetNearbyCollectionsCommand;
import org.devkor.apu.saerok_server.domain.collection.application.NearbyCollectionsMode;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DistancePriorityNearbyCollectionsStrategy implements NearbyCollectionsStrategy {

    private final CollectionRepository collectionRepository;

    @Override
    public NearbyCollectionsMode getMode() {
        return NearbyCollectionsMode.DIST;
    }

    @Override
    public List<UserBirdCollection> findCollections(GetNearbyCollectionsCommand command, Point refPoint) {
        return collectionRepository.findNearby(
                refPoint,
                command.radiusMeters(),
                command.userId(),
                command.isMineOnly(),
                command.limit()
        );
    }
}
