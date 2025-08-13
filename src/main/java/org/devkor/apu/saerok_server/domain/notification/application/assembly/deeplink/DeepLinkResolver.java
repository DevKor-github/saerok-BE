package org.devkor.apu.saerok_server.domain.notification.application.assembly.deeplink;

import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;

public interface DeepLinkResolver {
    String resolve(Target target);
}
