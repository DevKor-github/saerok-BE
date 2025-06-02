package org.devkor.apu.saerok_server.domain.auth.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.JwtResponse;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.infra.AppleApiClient;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AppleAuthService {

    private final AppleApiClient appleApiClient;
    private final JwtProvider jwtProvider;
    private final SocialAuthRepository socialAuthRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public JwtResponse authenticate(String authorizationCode) {

        String idToken = appleApiClient.requestIdToken(authorizationCode);

        DecodedJWT jwt = JWT.decode(idToken);
        String sub = jwt.getClaim("sub").asString();
        String email = jwt.getClaim("email").asString();

        log.info("애플 서버로부터 받은 ID TOKEN 정보 [sub: {}, email: {}]", sub, email);

        SocialAuth socialAuth = socialAuthRepository
                .findByProviderAndProviderUserId(SocialProviderType.APPLE, sub)
                .orElseGet(() -> {
                    User user = userRepository.save(User.createUser(email));
                    userRoleRepository.save(UserRole.createUserRole(user, UserRoleType.USER));

                    return socialAuthRepository.save(
                            SocialAuth.createSocialAuth(user, SocialProviderType.APPLE, sub)
                    );
                });

        socialAuth.setLastLoginAt(OffsetDateTime.now());

        User user = socialAuth.getUser();
        List<String> roles = userRoleRepository.findByUser(user).stream()
                .map(ur -> ur.getRole().name())
                .toList();

        String accessToken = jwtProvider.createAccessToken(user.getId(), roles);
        return new JwtResponse(accessToken, user.getSignupStatus().name());

    }
}
