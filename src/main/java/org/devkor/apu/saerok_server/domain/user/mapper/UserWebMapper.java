package org.devkor.apu.saerok_server.domain.user.mapper;

import org.devkor.apu.saerok_server.domain.user.api.dto.request.SignupCompleteRequest;
import org.devkor.apu.saerok_server.domain.user.api.dto.request.UpdateUserProfileRequest;
import org.devkor.apu.saerok_server.domain.user.application.dto.SignupCompleteCommand;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface UserWebMapper {

    UpdateUserProfileCommand toUpdateUserProfileCommand(UpdateUserProfileRequest request, Long userId);

    SignupCompleteCommand toSignupCompleteCommand(SignupCompleteRequest request, Long userId);
}
