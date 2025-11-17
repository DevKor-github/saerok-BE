package org.devkor.apu.saerok_server.domain.auth.application.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.application.AuthResult;
import org.devkor.apu.saerok_server.domain.auth.core.entity.UserRefreshToken;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.security.token.AccessTokenProvider;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenPair;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenProvider;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthTokenFacade {

    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final UserRoleRepository userRoleRepository;
    private final UserRefreshTokenRepository refreshTokenRepo;

    /**
     * User를 넘기면 액세스 토큰과 리프레시 토큰을 발급하고
     * 비즈니스 결과(AuthResult)로 반환함.
     */
    @Transactional
    public AuthResult issueTokens(User user, ClientInfo ci) {

        // 1) roles → access token
        List<String> roles = userRoleRepository.findByUser(user)
                .stream()
                .map(ur -> ur.getRole().getCode())
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

        // 3) 비즈니스 결과 레코드로 반환 (표현 계층은 별도)
        return new AuthResult(
                access,
                pair.raw(),
                user.getSignupStatus().name(),
                user
        );
    }

    /**
     * refresh 토큰을 한 번 쓰고 → 새로 교체하는 시나리오
     */
    @Transactional
    public AuthResult rotateTokens(UserRefreshToken oldToken, ClientInfo ci) {
        oldToken.revoke();
        return issueTokens(oldToken.getUser(), ci);
    }
}
