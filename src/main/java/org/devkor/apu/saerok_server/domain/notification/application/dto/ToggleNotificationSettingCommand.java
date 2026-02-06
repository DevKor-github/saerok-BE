package org.devkor.apu.saerok_server.domain.notification.application.dto;

import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

public record ToggleNotificationSettingCommand(
        Long userId,
        String deviceId,
        DevicePlatform platform,
        NotificationType type
) {}
