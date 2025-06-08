package org.devkor.apu.saerok_server.domain.user.api.response;

public record CheckNicknameResponse(
        boolean isAvailable,
        String reason
) {
}
