// ===== ./src/main/java/org/devkor/apu/saerok_server/domain/notification/mapper/NotificationSettingWebMapper.java =====
package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.request.ToggleNotificationRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.ToggleNotificationSettingCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationSettingWebMapper {

    public ToggleNotificationSettingCommand toToggleNotificationSettingCommand(Long userId, ToggleNotificationRequest req) {
        return new ToggleNotificationSettingCommand(userId, req.deviceId(), req.type());
    }

    public ToggleNotificationResponse toToggleNotificationResponse(ToggleNotificationSettingCommand cmd, boolean enabled) {
        return new ToggleNotificationResponse(cmd.deviceId(), cmd.type(), enabled);
    }

    public NotificationSettingsResponse toNotificationSettingsResponse(String deviceId, List<NotificationSetting> rows) {
        List<NotificationSettingsResponse.Item> items = rows.stream()
                .map(ns -> new NotificationSettingsResponse.Item(ns.getType(), ns.getEnabled()))
                .toList();
        return new NotificationSettingsResponse(deviceId, items);
    }
}
