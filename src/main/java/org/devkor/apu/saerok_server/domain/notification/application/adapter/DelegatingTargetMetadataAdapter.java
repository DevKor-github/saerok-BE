package org.devkor.apu.saerok_server.domain.notification.application.adapter;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.port.TargetMetadataPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Target 타입에 따라 적절한 어댑터에게 위임하는 Delegating 어댑터.
 */
@Primary
@Component
@RequiredArgsConstructor
public class DelegatingTargetMetadataAdapter implements TargetMetadataPort {

    private final CollectionTargetMetadataAdapter collectionAdapter;
    private final CommentTargetMetadataAdapter commentAdapter;

    @Override
    public Map<String, Object> enrich(Target target, Map<String, Object> baseExtras) {
        return switch (target.type()) {
            case COLLECTION -> collectionAdapter.enrich(target, baseExtras);
            case COMMENT -> commentAdapter.enrich(target, baseExtras);
        };
    }
}
