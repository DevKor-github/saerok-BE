package org.devkor.apu.saerok_server.domain.notification.application.adapter;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.TargetType;
import org.devkor.apu.saerok_server.domain.notification.application.port.TargetMetadataPort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TargetType.FREE_BOARD_POST 전용 메타데이터 어댑터.<br>
 * - extras.freeBoardPostId
 */
@Component
@RequiredArgsConstructor
public class FreeBoardPostTargetMetadataAdapter implements TargetMetadataPort {

    @Override
    public Map<String, Object> enrich(Target target, Map<String, Object> baseExtras) {
        if (target.type() != TargetType.FREE_BOARD_POST) {
            return baseExtras != null ? baseExtras : Map.of();
        }

        Map<String, Object> extras = baseExtras != null ? new HashMap<>(baseExtras) : new HashMap<>();
        extras.put("freeBoardPostId", target.id());
        return extras;
    }
}
