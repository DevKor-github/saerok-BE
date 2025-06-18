package org.devkor.apu.saerok_server.domain.auth.core.dto;

public record SocialUserInfo(
        String sub,
        String email,
        String refreshToken
) {
}
