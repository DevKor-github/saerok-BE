package org.devkor.apu.saerok_server.domain.notification.application.dto;

public record DeviceTokenDeleteCommand(
        Long userId,
        String deviceId
) {}
