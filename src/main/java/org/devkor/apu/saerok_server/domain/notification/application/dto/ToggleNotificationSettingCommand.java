package org.devkor.apu.saerok_server.domain.notification.application.dto;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

public record ToggleNotificationSettingCommand(
        Long userId,
        String deviceId,
        NotificationSubject subject,
        NotificationAction action // nullable 허용: 그룹 토글일 때 null
) {}
