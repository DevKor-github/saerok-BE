package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.util.RegexMatchUtils;
import org.springframework.stereotype.Service;

/**
 * 사용자 데이터를 조사하고, 규칙을 만족할 경우 signup_status를 업데이트할 책임을 지는 도메인 서비스.
 */
@Service
@RequiredArgsConstructor
public class UserSignupStatusService {

    private final NicknamePolicy nicknamePolicy;

    public void tryCompleteSignup(User user) {

        if (user.getSignupStatus() == SignupStatusType.COMPLETED) return;

        if (nicknamePolicy.isNicknameValid(user.getNickname())
                && RegexMatchUtils.isEmailValid(user.getEmail())
        ) {
            user.setSignupStatus(SignupStatusType.COMPLETED);
        }
    }
}
