package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.request.ToggleNotificationRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.ToggleNotificationSettingCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface NotificationSettingsWebMapper {


    @Mapping(target = "userId", source = "userId")
    ToggleNotificationSettingCommand toToggleNotificationSettingCommand(ToggleNotificationRequest request, Long userId);

    @Mapping(target = "enabled", source = "isEnabled")
    ToggleNotificationResponse toToggleNotificationResponse(ToggleNotificationSettingCommand command, boolean isEnabled);
}