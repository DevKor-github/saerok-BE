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
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialAuthClient;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.global.security.crypto.DataCryptoService;
import org.devkor.apu.saerok_server.global.security.crypto.EncryptedPayload;
import org.devkor.apu.saerok_server.global.util.dto.ClientInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Transactional
public abstract class AbstractSocialAuthService {

    private final SocialAuthRepository socialAuthRepository;
    private final AuthTokenFacade authTokenFacade;
    private final UserProvisioningService userProvisioningService;
    private final DataCryptoService dataCryptoService;

    protected abstract SocialAuthClient client();

    public ResponseEntity<AccessTokenResponse> authenticate(String authorizationCode, String accessToken, ClientInfo ci) {

        SocialUserInfo userInfo = client().fetch(authorizationCode, accessToken);
        SocialProviderType provider = client().provider();
        log.info("사용자 인증 - provider: {}, sub: {}, email: {}", provider, userInfo.sub(), userInfo.email());

        SocialAuth socialAuth = socialAuthRepository
                .findByProviderAndProviderUserId(provider, userInfo.sub())
                .orElseGet(() -> userProvisioningService.provisionNewUser(provider, userInfo));

        if (userInfo.refreshToken() != null) {
            log.info("소셜 공급자로부터 받은 리프레시 토큰: {}", userInfo.refreshToken());
            EncryptedPayload payload = dataCryptoService.encrypt(userInfo.refreshToken().getBytes(StandardCharsets.UTF_8));
            socialAuth.setRefreshToken(payload);
        }

        AuthBundle bundle = authTokenFacade.issueTokens(
                socialAuth.getUser(),
                ci
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, bundle.cookie().toString())
                .body(bundle.body());
    }
}
