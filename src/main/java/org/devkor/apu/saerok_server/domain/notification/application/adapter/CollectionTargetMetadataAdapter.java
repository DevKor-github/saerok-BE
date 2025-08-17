package org.devkor.apu.saerok_server.domain.notification.application.adapter;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.TargetType;
import org.devkor.apu.saerok_server.domain.notification.application.port.TargetMetadataPort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TargetType.COLLECTION 전용 메타데이터 어댑터.<br>
 * - extras.collectionId<br>
 * - extras.collectionImageUrl (없으면 null 넣어 키 유지)
 */
@Component
@RequiredArgsConstructor
public class CollectionTargetMetadataAdapter implements TargetMetadataPort {

    private final CollectionRepository collectionRepository;
    private final CollectionImageUrlService collectionImageUrlService;

    @Override
    public Map<String, Object> enrich(Target target, Map<String, Object> baseExtras) {
        if (target.type() != TargetType.COLLECTION) {
            return baseExtras != null ? baseExtras : Map.of();
        }

        Map<String, Object> extras = baseExtras != null ? new HashMap<>(baseExtras) : new HashMap<>();
        extras.put("collectionId", target.id());

        String imageUrl = collectionRepository.findById(target.id())
                .flatMap(collectionImageUrlService::getPrimaryImageUrlFor)
                .orElse(null);
        extras.put("collectionImageUrl", imageUrl);

        return extras;
    }
}
