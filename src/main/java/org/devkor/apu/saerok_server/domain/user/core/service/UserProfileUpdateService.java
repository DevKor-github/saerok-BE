package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 사용자 프로필 수정을 전담하는 Domain Service.
 * 각 프로필 항목의 규칙에 따라 수정을 허용하거나 반려합니다. (ex: 닉네임은 0글자일 수 없음)
 */
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {

    private final UserProfilePolicy userProfilePolicy;
    private final UserRepository userRepository;

    public void changeNickname(User user, String nickname) {

        if (user.getNickname() != null && user.getNickname().equals(nickname)) return;

        if (!userProfilePolicy.isNicknameValid(nickname)) {
            throw new IllegalArgumentException("해당 닉네임은 정책상 사용할 수 없습니다.");
        }

        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("해당 닉네임은 다른 사용자가 사용 중입니다.");
        }

        user.setNickname(nickname);
    }
}
