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
public class EvenDistributionNearbyCollectionsStrategy implements NearbyCollectionsStrategy {

    private static final double CELL_STABILITY_COEFFICIENT = 1.5;
    private static final double CELL_SIZE_MIN = 80.0;

    private final CollectionRepository collectionRepository;

    @Override
    public NearbyCollectionsMode getMode() {
        return NearbyCollectionsMode.EVEN;
    }

    @Override
    public List<UserBirdCollection> findCollections(GetNearbyCollectionsCommand command, Point refPoint) {
        long candidateCount = collectionRepository.countNearbyCandidates(
                refPoint,
                command.radiusMeters(),
                command.userId(),
                command.isMineOnly()
        );

        if (candidateCount == 0) {
            return List.of();
        }


        long finalCount = command.limit() != null ? Math.min(command.limit(), candidateCount) : candidateCount;
        if (finalCount <= 0) {
            return List.of();
        }

        double cellSize = calculateCellSize(command.radiusMeters(), finalCount);
        return collectionRepository.findNearbyEven(
                refPoint,
                command.radiusMeters(),
                command.userId(),
                command.isMineOnly(),
                finalCount,
                cellSize
        );
    }

    private double calculateCellSize(double radiusMeters, long count) {
        double cellCount = Math.max(count * CELL_STABILITY_COEFFICIENT, 1.0);
        double cellArea = (Math.PI * Math.pow(radiusMeters, 2)) / cellCount;
        double rawCellSize = Math.sqrt(cellArea);
        double cellSizeMax = radiusMeters / 2.0;
        return clamp(rawCellSize, CELL_SIZE_MIN, cellSizeMax);
    }

    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

}
