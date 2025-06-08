package org.devkor.apu.saerok_server.domain.auth.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade.AuthBundle;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.support.SocialAuthClient;
import org.devkor.apu.saerok_server.domain.auth.support.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.util.dto.ClientInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSocialAuthService {

    private final SocialAuthRepository socialAuthRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthTokenFacade authTokenFacade;

    protected abstract SocialAuthClient client();

    @Transactional
    public ResponseEntity<AccessTokenResponse> authenticate(String authorizationCode, String accessToken, ClientInfo ci) {

        SocialUserInfo userInfo = client().fetch(authorizationCode, accessToken);
        SocialProviderType provider = client().provider();
        log.info("사용자 인증 - provider: {}, sub: {}, email: {}", provider, userInfo.sub(), userInfo.email());

        SocialAuth socialAuth = socialAuthRepository
                .findByProviderAndProviderUserId(provider, userInfo.sub())
                .orElseGet(() -> provisionNewUser(provider, userInfo));

        AuthBundle bundle = authTokenFacade.issueTokens(
                socialAuth.getUser(),
                ci
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, bundle.cookie().toString())
                .body(bundle.body());
    }

    private SocialAuth provisionNewUser(SocialProviderType provider, SocialUserInfo userInfo) {
        User user = userRepository.save(User.createUser(userInfo.email()));
        userRoleRepository.save(UserRole.createUserRole(user, UserRoleType.USER));

        return socialAuthRepository.save(
                SocialAuth.createSocialAuth(user, provider, userInfo.sub())
        );
    }
}
