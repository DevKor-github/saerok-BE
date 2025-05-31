package org.devkor.apu.saerok_server.domain.user.auth.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.auth.api.dto.response.JwtResponse;
import org.devkor.apu.saerok_server.domain.user.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.user.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.user.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.user.auth.infra.AppleApiClient;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AppleAuthService {

    private final AppleApiClient appleApiClient;
    private final SocialAuthRepository socialAuthRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public JwtResponse authenticate(String authorizationCode) {

        String idToken = appleApiClient.requestIdToken(authorizationCode);

        DecodedJWT jwt = JWT.decode(idToken);
        String sub = jwt.getClaim("sub").asString();
        String email = jwt.getClaim("email").asString();

        SocialAuth socialAuth = socialAuthRepository
                .findByProviderAndProviderUserId(SocialProviderType.APPLE, sub)
                .orElseGet(() -> {
                    User user = userRepository.save(User.createUser(email));
                    return socialAuthRepository.save(
                            SocialAuth.createSocialAuth(user, SocialProviderType.APPLE, sub)
                    );
                });

        socialAuth.setLastLoginAt(OffsetDateTime.now());

        String accessToken = jwtProvider.createAccessToken(socialAuth.getUser().getId());
        return new JwtResponse(accessToken);

    }
}
