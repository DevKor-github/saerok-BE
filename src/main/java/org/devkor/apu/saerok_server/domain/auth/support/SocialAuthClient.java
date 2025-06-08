package org.devkor.apu.saerok_server.domain.auth.support;

import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;

public interface SocialAuthClient {

    SocialProviderType provider();
    SocialUserInfo fetch(String authorizationCode, String accessToken);
}
