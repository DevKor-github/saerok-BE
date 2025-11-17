package org.devkor.apu.saerok_server.domain.auth.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenService;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialAuthClient;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
import org.devkor.apu.saerok_server.global.security.crypto.DataCryptoService;
import org.devkor.apu.saerok_server.global.security.crypto.EncryptedPayload;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Transactional
public abstract class AbstractSocialLoginService {

    private final SocialAuthRepository socialAuthRepository;
    private final AuthTokenService authTokenService;
    private final UserProvisioningService userProvisioningService;
    private final DataCryptoService dataCryptoService;

    protected abstract SocialAuthClient client();

    /** 기존 기본 흐름(redirect_uri 고정) */
    public LoginResult authenticate(String authorizationCode, String accessToken, ClientInfo ci) {
        SocialUserInfo userInfo = client().fetch(authorizationCode, accessToken);
        return authenticateWithUserInfo(userInfo, ci);
    }

    /** 공급자별 커스텀 흐름(예: redirect_uri 동적 선택)에서도 재사용 가능한 공통 후처리 */
    protected LoginResult authenticateWithUserInfo(SocialUserInfo userInfo, ClientInfo ci) {
        SocialAuth socialAuth = authenticateSocialUser(userInfo, client().provider());

        return authTokenService.issueTokens(
                socialAuth.getUser(),
                ci
        );
    }

    private SocialAuth authenticateSocialUser(SocialUserInfo userInfo, SocialProviderType provider) {
        SocialAuth socialAuth = socialAuthRepository
                .findByProviderAndProviderUserId(provider, userInfo.sub())
                .map(link -> {
                    // 탈퇴했던 유저면 재프로비저닝
                    if (link.getUser().getSignupStatus() == SignupStatusType.WITHDRAWN) {
                        userProvisioningService.provisionRejoinedUser(link.getUser(), userInfo.email());
                    }
                    return link;
                })
                .orElseGet(() -> userProvisioningService.provisionNewUser(provider, userInfo));

        if (userInfo.refreshToken() != null) {
            EncryptedPayload payload = dataCryptoService.encrypt(userInfo.refreshToken().getBytes(StandardCharsets.UTF_8));
            socialAuth.setRefreshToken(payload);
        }

        return socialAuth;
    }
}
