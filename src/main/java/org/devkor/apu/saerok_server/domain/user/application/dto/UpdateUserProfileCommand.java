package org.devkor.apu.saerok_server.domain.user.application.dto;

public record UpdateUserProfileCommand(
        Long userId,
        String nickname
) {
}
