package org.devkor.apu.saerok_server.domain.collection.application.helper;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SingleImageSelector implements CollectionImageSelector {

    private final CollectionImageRepository collectionImageRepository;

    @Override
    public Optional<String> selectPrimaryImageKey(UserBirdCollection collection) {
        return collectionImageRepository.findObjectKeysByCollectionId(collection.getId()).stream()
                .findFirst();
    }

    @Override
    public Map<Long, String> selectPrimaryImageKeyMap(List<UserBirdCollection> collections) {
        List<Long> ids = collections.stream()
                .map(UserBirdCollection::getId)
                .toList();

        return collectionImageRepository.findThumbKeysByCollectionIds(ids);
    }
}
