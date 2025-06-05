package org.devkor.apu.saerok_server.global.security.token;

public record RefreshTokenPair(
        String raw,
        String hashed
) {
}
