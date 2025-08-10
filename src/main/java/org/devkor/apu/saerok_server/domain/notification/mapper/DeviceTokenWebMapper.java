package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.request.RegisterTokenRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterTokenResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.RegisterTokenCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface DeviceTokenWebMapper {

    @Mapping(target = "userId", source = "userId")
    RegisterTokenCommand toRegisterTokenCommand(RegisterTokenRequest request, Long userId);

    @Mapping(target = "success", source = "success")
    RegisterTokenResponse toRegisterTokenResponse(RegisterTokenCommand command, Boolean success);
}
