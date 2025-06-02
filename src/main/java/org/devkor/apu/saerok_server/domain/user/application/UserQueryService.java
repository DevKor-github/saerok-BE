package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.response.CheckNicknameResponse;
import org.devkor.apu.saerok_server.domain.user.api.response.GetMyUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public GetMyUserProfileResponse getMyUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("해당 id의 사용자가 존재하지 않아요"));

        return new GetMyUserProfileResponse(
                user.getNickname(),
                user.getEmail()
        );
    }

    public CheckNicknameResponse checkNickname(String nickname) {

        return new CheckNicknameResponse(
                userRepository.findByNickname(nickname).isPresent()
        );
    }
}
