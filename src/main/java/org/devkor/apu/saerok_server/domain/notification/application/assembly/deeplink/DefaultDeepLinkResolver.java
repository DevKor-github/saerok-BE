package org.devkor.apu.saerok_server.domain.notification.application.assembly.deeplink;

import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.springframework.stereotype.Component;

@Component
public class DefaultDeepLinkResolver implements DeepLinkResolver {
    @Override
    public String resolve(Target target) {
        return switch (target.type()) {
            case COLLECTION -> "saerok://collection/" + target.id();
        };
    }
}
