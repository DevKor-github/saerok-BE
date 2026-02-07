package org.devkor.apu.saerok_server.domain.notification.application.dto;

import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;

public record RegisterUserDeviceCommand(
        Long userId,
        String deviceId,
        String token,
        DevicePlatform platform
) {}
