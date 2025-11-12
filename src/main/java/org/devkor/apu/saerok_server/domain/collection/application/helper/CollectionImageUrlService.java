package org.devkor.apu.saerok_server.domain.collection.application.helper;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainRouter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionImageUrlService {

    private final CollectionImageSelector collectionImageSelector;
    private final ImageVariantService imageVariantService;
    private final ImageDomainRouter imageDomainRouter;

    public Optional<String> getPrimaryImageUrlFor(UserBirdCollection collection) {
        return collectionImageSelector.selectPrimaryImageKey(collection)
                .map(key -> imageDomainRouter.toUrlFor(ImageKind.USER_COLLECTION_IMAGE, key));
    }

    /**
     * 썸네일 미지원/실패 시 원본 이미지 URL로 fallback 합니다.
     */
    public Optional<String> getPrimaryImageThumbnailUrlFor(UserBirdCollection collection) {
        return collectionImageSelector.selectPrimaryImageKey(collection)
                .map(originalKey ->
                        imageVariantService.thumbnailKey(ImageKind.USER_COLLECTION_IMAGE, originalKey)
                                .map(thumbKey -> imageDomainRouter.toUrlFor(ImageKind.USER_COLLECTION_IMAGE, thumbKey))
                                .orElseGet(() -> imageDomainRouter.toUrlFor(ImageKind.USER_COLLECTION_IMAGE, originalKey))
                );
    }

    public Map<Long, String> getPrimaryImageUrlsFor(List<UserBirdCollection> collections) {
        Map<Long, String> result = new LinkedHashMap<>();
        Map<Long, String> objectKeyMap = collectionImageSelector.selectPrimaryImageKeyMap(collections);

        for (Map.Entry<Long, String> entry : objectKeyMap.entrySet()) {
            String objectKey = entry.getValue();
            String imageUrl = (objectKey != null)
                    ? imageDomainRouter.toUrlFor(ImageKind.USER_COLLECTION_IMAGE, objectKey)
                    : null;
            result.put(entry.getKey(), imageUrl);
        }

        return result;
    }

    /**
     * 썸네일 미지원/실패 시 각 항목은 원본 이미지 URL로 fallback 합니다.
     */
    public Map<Long, String> getPrimaryImageThumbnailUrlsFor(List<UserBirdCollection> collections) {
        Map<Long, String> result = new LinkedHashMap<>();
        Map<Long, String> objectKeyMap = collectionImageSelector.selectPrimaryImageKeyMap(collections);

        for (Map.Entry<Long, String> entry : objectKeyMap.entrySet()) {
            String objectKey = entry.getValue();
            String url = null;
            if (objectKey != null) {
                url = imageVariantService.thumbnailKey(ImageKind.USER_COLLECTION_IMAGE, objectKey)
                        .map(thumbKey -> imageDomainRouter.toUrlFor(ImageKind.USER_COLLECTION_IMAGE, thumbKey))
                        .orElseGet(() -> imageDomainRouter.toUrlFor(ImageKind.USER_COLLECTION_IMAGE, objectKey));
            }
            result.put(entry.getKey(), url);
        }

        return result;
    }
}
