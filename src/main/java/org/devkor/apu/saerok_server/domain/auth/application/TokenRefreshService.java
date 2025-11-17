package org.devkor.apu.saerok_server.domain.auth.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.core.entity.UserRefreshToken;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.global.shared.exception.UnauthorizedException;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenProvider;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TokenRefreshService {

    private final RefreshTokenProvider refreshTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final AuthTokenFacade authTokenFacade;

    /**
     * 액세스 토큰과 리프레시 토큰을 갱신하여 돌려줍니다.
     * 액세스 토큰은 AuthResult.accessToken(), 리프레시 토큰은 AuthResult.refreshToken()에 담겨
     * 표현 계층(AuthController)에서 HttpOnly 쿠키와 JSON 바디로 조립됩니다.
     *
     * @param refreshTokenCookie 클라이언트가 보낸 리프레시 토큰
     */
    public AuthResult refresh(String refreshTokenCookie, ClientInfo clientInfo) {

        log.info("refreshTokenCookie: {}", refreshTokenCookie);

        UserRefreshToken userRefreshToken = userRefreshTokenRepository
                .findByRefreshTokenHash(refreshTokenProvider.hash(refreshTokenCookie))
                .orElseThrow(() -> new UnauthorizedException("리프레시 토큰이 유효하지 않아요 (not found)"));

        if (!userRefreshToken.isUsable()) {
            throw new UnauthorizedException("리프레시 토큰이 유효하지 않아요 (not usable)");
        }

        return authTokenFacade.rotateTokens(userRefreshToken, clientInfo);
    }
}
