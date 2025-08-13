package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.request.ToggleNotificationRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.ToggleNotificationSettingCommand;
import org.springframework.stereotype.Component;

@Component
public class NotificationSettingWebMapper {

    public ToggleNotificationSettingCommand toToggleNotificationSettingCommand(Long userId, ToggleNotificationRequest request) {
        return new ToggleNotificationSettingCommand(
                userId,
                request.deviceId(),
                request.subject(),
                request.action()
        );
    }

    public ToggleNotificationResponse toToggleNotificationResponse(ToggleNotificationSettingCommand cmd, boolean enabled) {
        return new ToggleNotificationResponse(cmd.deviceId(), cmd.subject(), cmd.action(), enabled);
    }
}
