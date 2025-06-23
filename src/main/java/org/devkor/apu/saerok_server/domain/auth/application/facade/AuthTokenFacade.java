package org.devkor.apu.saerok_server.domain.auth.application.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.core.entity.UserRefreshToken;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.security.token.AccessTokenProvider;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenPair;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenProvider;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthTokenFacade {

    @Value("${app.cookie.secure}")
    private boolean isCookieSecure;

    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final UserRoleRepository userRoleRepository;
    private final UserRefreshTokenRepository refreshTokenRepo;

    /** User를 넘기면 액세스 토큰과 리프레시 토큰을 발급하고 쿠키와 바디로 제공함 */
    @Transactional
    public AuthBundle issueTokens(User user, ClientInfo ci) {

        // 1) roles → access token
        List<String> roles = userRoleRepository.findByUser(user)
                .stream()
                .map(ur -> ur.getRole().name())
                .toList();
        String access = accessTokenProvider.createAccessToken(user.getId(), roles);

        // 2) refresh token pair
        RefreshTokenPair pair = refreshTokenProvider.generateRefreshTokenPair();
        refreshTokenRepo.save(
                UserRefreshToken.create(
                        user,
                        pair.hashed(),
                        ci.userAgent(),
                        ci.ipAddress(),
                        RefreshTokenProvider.validDuration
                )
        );

        // 3) cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", pair.raw())
                .httpOnly(true)
                .secure(isCookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth/refresh")
                .maxAge(RefreshTokenProvider.validDuration)
                .build();

        // 4) 바디
        AccessTokenResponse body = new AccessTokenResponse(access, user.getSignupStatus().name());

        return new AuthBundle(cookie, body);
    }

    /** refresh 토큰을 한 번 쓰고 → 새로 교체하는 시나리오 */
    @Transactional
    public AuthBundle rotateTokens(UserRefreshToken oldToken, ClientInfo ci) {
        oldToken.revoke();
        return issueTokens(oldToken.getUser(), ci);
    }

    public record AuthBundle(ResponseCookie cookie,
                             AccessTokenResponse body) {}
}
