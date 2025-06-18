package org.devkor.apu.saerok_server.global.exception;

import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;

public class SocialAuthAlreadyExistsException extends RuntimeException {
    public SocialAuthAlreadyExistsException(SocialProviderType provider, String sub) {
        super("이미 등록된 소셜 계정입니다. provider: " + provider + ", sub: " + sub);
    }
}
