package org.devkor.apu.saerok_server.domain.auth.infra;

import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;

public interface SocialRevoker {
    SocialProviderType provider();
    void revoke(SocialAuth link);
}