package org.devkor.apu.saerok_server.domain.auth.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade.AuthBundle;
import org.devkor.apu.saerok_server.domain.auth.core.entity.UserRefreshToken;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.global.exception.UnauthorizedException;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenProvider;
import org.devkor.apu.saerok_server.global.util.dto.ClientInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
     * 액세스 토큰은 응답 body에, 리프레시 토큰은 HttpOnly 쿠키로 포함됩니다.
     * @param refreshTokenCookie 클라이언트가 보낸 리프레시 토큰
     * @return
     */
    public ResponseEntity<AccessTokenResponse> refresh(String refreshTokenCookie, ClientInfo clientInfo) {

        log.info("refreshTokenCookie: {}", refreshTokenCookie);

        UserRefreshToken userRefreshToken = userRefreshTokenRepository
                .findByRefreshTokenHash(refreshTokenProvider.hash(refreshTokenCookie))
                .orElseThrow(() -> new UnauthorizedException("리프레시 토큰이 유효하지 않아요 (not found)"));

        if (!userRefreshToken.isUsable()) {
            throw new UnauthorizedException("리프레시 토큰이 유효하지 않아요 (not usable)");
        }

        AuthBundle bundle = authTokenFacade.rotateTokens(userRefreshToken, clientInfo);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, bundle.cookie().toString())
                .body(bundle.body());
    }
}
