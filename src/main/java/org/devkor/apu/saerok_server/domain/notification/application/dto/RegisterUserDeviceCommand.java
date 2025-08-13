package org.devkor.apu.saerok_server.domain.notification.application.dto;

public record RegisterUserDeviceCommand(
        Long userId,
        String deviceId,
        String token
) {}
