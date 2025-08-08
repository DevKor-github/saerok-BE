package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.request.ToggleNotificationRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.ToggleNotificationSettingCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface NotificationSettingsWebMapper {

    NotificationSettingsResponse toNotificationSettingsResponse(NotificationSettings settings);

    @Mapping(target = "userId", source = "userId")
    ToggleNotificationSettingCommand toToggleNotificationSettingCommand(ToggleNotificationRequest request, Long userId);

    @Mapping(target = "enabled", source = "isEnabled")
    ToggleNotificationResponse toToggleNotificationResponse(ToggleNotificationSettingCommand command, boolean isEnabled);
}