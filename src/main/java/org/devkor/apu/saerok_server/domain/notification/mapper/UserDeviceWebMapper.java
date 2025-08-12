package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.request.RegisterTokenRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterUserDeviceResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.RegisterUserDeviceCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface UserDeviceWebMapper {

    @Mapping(target = "userId", source = "userId")
    RegisterUserDeviceCommand toRegisterUserDeviceCommand(RegisterTokenRequest request, Long userId);

    @Mapping(target = "success", source = "success")
    RegisterUserDeviceResponse toRegisterUserDeviceResponse(RegisterUserDeviceCommand command, Boolean success);
}
