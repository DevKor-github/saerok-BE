package org.devkor.apu.saerok_server.domain.collection.application.strategy;

import org.devkor.apu.saerok_server.domain.collection.application.dto.GetNearbyCollectionsCommand;
import org.devkor.apu.saerok_server.domain.collection.application.NearbyCollectionsMode;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface NearbyCollectionsStrategy {

    NearbyCollectionsMode getMode();

    List<UserBirdCollection> findCollections(GetNearbyCollectionsCommand command, Point refPoint);
}
