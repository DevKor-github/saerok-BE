package org.devkor.apu.saerok_server.domain.notification.application.deeplink;

import org.devkor.apu.saerok_server.domain.notification.application.dsl.Target;

public interface DeepLinkResolver {
    String resolve(Target target);
}
