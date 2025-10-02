package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoAuthClient;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoRedirectUriResolver;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialAuthClient;
import org.devkor.apu.saerok_server.global.security.crypto.DataCryptoService;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class KakaoAuthService extends AbstractSocialAuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoRedirectUriResolver redirectUriResolver;

    public KakaoAuthService(
            SocialAuthRepository socialAuthRepository,
            AuthTokenFacade authTokenFacade,
            UserProvisioningService userProvisioningService,
            DataCryptoService dataCryptoService,
            KakaoAuthClient kakaoAuthClient,
            KakaoRedirectUriResolver redirectUriResolver
    ) {
        super(socialAuthRepository, authTokenFacade, userProvisioningService, dataCryptoService);
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
     */
    public ResponseEntity<AccessTokenResponse> authenticate(
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
        return authenticateWithUserInfo(userInfo, ci);
    }
}
