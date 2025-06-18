package org.devkor.apu.saerok_server.domain.user.mapper;

import org.devkor.apu.saerok_server.domain.user.api.dto.request.UpdateUserProfileRequest;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface UserWebMapper {

    UpdateUserProfileCommand toUpdateUserProfileCommand(UpdateUserProfileRequest request, Long userId);
}
