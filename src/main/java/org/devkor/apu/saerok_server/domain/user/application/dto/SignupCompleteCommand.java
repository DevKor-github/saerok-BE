package org.devkor.apu.saerok_server.domain.user.application.dto;

import org.devkor.apu.saerok_server.domain.user.core.entity.SignupSourceType;

public record SignupCompleteCommand(
        Long userId,
        String nickname,
        SignupSourceType signupSource
) {
}
