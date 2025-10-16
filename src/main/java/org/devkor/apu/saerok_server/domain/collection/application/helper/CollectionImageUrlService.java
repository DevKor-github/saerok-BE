package org.devkor.apu.saerok_server.domain.collection.application.helper;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionImageUrlService {

    private final CollectionImageSelector collectionImageSelector;
    private final ImageDomainService imageDomainService;

    public Optional<String> getPrimaryImageUrlFor(UserBirdCollection collection) {
        return collectionImageSelector.selectPrimaryImageKey(collection)
                .map(imageDomainService::toUploadImageUrl);
    }

    public Map<Long, String> getPrimaryImageUrlsFor(List<UserBirdCollection> collections) {
        Map<Long, String> result = new LinkedHashMap<>();
        Map<Long, String> objectKeyMap = collectionImageSelector.selectPrimaryImageKeyMap(collections);

        for (Map.Entry<Long, String> entry : objectKeyMap.entrySet()) {
            String objectKey = entry.getValue();
            String imageUrl = objectKey != null ? imageDomainService.toUploadImageUrl(objectKey) : null;
            result.put(entry.getKey(), imageUrl);
        }

        return result;
    }

    public Map<Long, String> getPrimaryImageThumbnailUrlsFor(List<UserBirdCollection> collections) {
        Map<Long, String> result = new LinkedHashMap<>();
        Map<Long, String> objectKeyMap = collectionImageSelector.selectPrimaryImageKeyMap(collections);

        for (Map.Entry<Long, String> entry : objectKeyMap.entrySet()) {
            String objectKey = entry.getValue();
            String thumbnailUrl = objectKey != null ? imageDomainService.toThumbnailUrl(objectKey) : null;
            result.put(entry.getKey(), thumbnailUrl);
        }

        return result;
    }

}
