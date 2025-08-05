package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.request.DeviceIdRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.request.RegisterTokenRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterTokenResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import org.devkor.apu.saerok_server.domain.notification.api.dto.response.LocalDeviceTokenResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DeviceToken;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface DeviceTokenWebMapper {

    @Mapping(target = "userId", source = "userId")
    RegisterTokenCommand toRegisterTokenCommand(RegisterTokenRequest request, Long userId);

    @Mapping(target = "userId", source = "userId")
    DeviceTokenDeleteCommand toDeviceTokenDeleteCommand(DeviceIdRequest request, Long userId);

    @Mapping(target = "userId", source = "userId")
    DeleteAllTokensCommand toDeleteAllTokensCommand(Long userId);

    RegisterTokenResponse toRegisterTokenResponse(RegisterTokenCommand command);

    @Mapping(source = "user.id", target = "userId")
    LocalDeviceTokenResponse toLocalDeviceTokenResponse(DeviceToken deviceToken);
}
