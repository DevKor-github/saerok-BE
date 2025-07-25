package org.devkor.apu.saerok_server.domain.notification.application.dto;

public record DeviceTokenToggleCommand(
        Long userId,
        String deviceId
) {}
