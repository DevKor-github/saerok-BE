package org.devkor.apu.saerok_server.domain.notification.application.dto;

public record RegisterTokenCommand(
        Long userId,
        String deviceId,
        String token
) {}
