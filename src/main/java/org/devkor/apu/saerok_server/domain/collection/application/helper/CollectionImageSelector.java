package org.devkor.apu.saerok_server.domain.collection.application.helper;

import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CollectionImageSelector {

    Optional<String> selectPrimaryImageKey(UserBirdCollection collection);

    Map<Long, String> selectPrimaryImageKeyMap(List<UserBirdCollection> collections);
}
