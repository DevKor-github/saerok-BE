package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileUpdateService;
import org.devkor.apu.saerok_server.domain.user.core.service.UserSignupStatusService;
import org.devkor.apu.saerok_server.global.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserProfileUpdateService userProfileUpdateService;
    private final UserSignupStatusService userSignupStatusService;

    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileCommand command) {

        User user = userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        try {
            if (command.nickname() != null) userProfileUpdateService.changeNickname(user, command.nickname());

        } catch (IllegalArgumentException e) {
            throw new BadRequestException("사용자 정보 수정이 거부되었습니다: " + e.getMessage());
        }

        userSignupStatusService.tryCompleteSignup(user);

        return new UpdateUserProfileResponse(
                user.getNickname(),
                user.getEmail()
        );
    }
}
