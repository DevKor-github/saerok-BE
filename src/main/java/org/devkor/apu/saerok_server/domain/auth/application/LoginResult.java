package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;

/**
 * 소셜 인증 usecase의 비즈니스 결과.
 * 표현 계층에서 AccessTokenResponse 및 쿠키로 변환해 사용한다.
 */
public record LoginResult(
        String accessToken,
        String refreshToken,
        String signupStatus,
        User user
) {
}
