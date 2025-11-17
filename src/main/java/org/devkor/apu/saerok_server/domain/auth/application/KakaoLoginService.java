package org.devkor.apu.saerok_server.domain.auth.application;

import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenService;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoAuthClient;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoRedirectUriResolver;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialAuthClient;
import org.devkor.apu.saerok_server.global.security.crypto.DataCryptoService;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KakaoLoginService extends AbstractSocialLoginService {

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoRedirectUriResolver redirectUriResolver;

    public KakaoLoginService(
            SocialAuthRepository socialAuthRepository,
            AuthTokenService authTokenService,
            UserProvisioningService userProvisioningService,
            DataCryptoService dataCryptoService,
            KakaoAuthClient kakaoAuthClient,
            KakaoRedirectUriResolver redirectUriResolver
    ) {
        super(socialAuthRepository, authTokenService, userProvisioningService, dataCryptoService);
        this.kakaoAuthClient = kakaoAuthClient;
        this.redirectUriResolver = redirectUriResolver;
    }

    @Override
    protected SocialAuthClient client() {
        return kakaoAuthClient;
    }

    /**
     * channel(요청 주체)에 따라 허용된 redirect_uri를 선택해 Kakao 토큰 교환을 수행.
     * - authorizationCode 경로에서만 redirect_uri가 사용됨
     * - accessToken 경로는 기존 처리와 동일
     * channel == "admin" 인 경우, 해당 카카오 계정이 ADMIN_VIEWER 또는 ADMIN_EDITOR 권한을
     * 이미 보유하고 있는지 선확인하고, 없으면 403으로 거부한다.
     */
    public LoginResult authenticate(
            String authorizationCode,
            String accessToken,
            String channel,
            ClientInfo ci
    ) {
        SocialUserInfo userInfo;
        if (authorizationCode != null) {
            String redirectUri = redirectUriResolver.resolve(channel);
            userInfo = kakaoAuthClient.fetchWithRedirect(authorizationCode, null, redirectUri);
        } else {
            userInfo = kakaoAuthClient.fetch(null, accessToken);
        }

        log.info("[fixlog] sub: {}, email: {}, channel: {}", userInfo.sub(), userInfo.email(), channel);

        return authenticateWithUserInfo(userInfo, ci);
    }
}
