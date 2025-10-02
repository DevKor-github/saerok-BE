package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoAuthClient;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoRedirectUriResolver;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialAuthClient;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.security.crypto.DataCryptoService;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class KakaoAuthService extends AbstractSocialAuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoRedirectUriResolver redirectUriResolver;

    // admin 채널 권한 확인을 위해 필요
    private final SocialAuthRepository socialAuthRepository;
    private final UserRoleRepository userRoleRepository;

    public KakaoAuthService(
            SocialAuthRepository socialAuthRepository,
            AuthTokenFacade authTokenFacade,
            UserProvisioningService userProvisioningService,
            DataCryptoService dataCryptoService,
            KakaoAuthClient kakaoAuthClient,
            KakaoRedirectUriResolver redirectUriResolver,
            UserRoleRepository userRoleRepository
    ) {
        super(socialAuthRepository, authTokenFacade, userProvisioningService, dataCryptoService);
        this.kakaoAuthClient = kakaoAuthClient;
        this.redirectUriResolver = redirectUriResolver;
        this.socialAuthRepository = socialAuthRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    protected SocialAuthClient client() {
        return kakaoAuthClient;
    }

    /**
     * channel(요청 주체)에 따라 허용된 redirect_uri를 선택해 Kakao 토큰 교환을 수행.
     * - authorizationCode 경로에서만 redirect_uri가 사용됨
     * - accessToken 경로는 기존 처리와 동일
     * - channel이 "admin"이면 해당 계정이 ADMIN 권한을 가지고 있는지 선확인(없으면 403)
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

        // ===== ADMIN 채널 권한 검사 =====
        if ("admin".equalsIgnoreCase(channel)) {
            // 기존 소셜 링크가 없으면 관리자 로그인 불가
            var link = socialAuthRepository
                    .findByProviderAndProviderUserId(kakaoAuthClient.provider(), userInfo.sub())
                    .orElseThrow(() ->
                            new ForbiddenException("관리자 권한이 없는 계정입니다 (미등록 계정)"));

            var hasAdminRole = userRoleRepository.findByUser(link.getUser()).stream()
                    .anyMatch(ur -> ur.getRole() == UserRoleType.ADMIN);

            if (!hasAdminRole) {
                throw new ForbiddenException("관리자 권한이 없는 계정입니다");
            }
        }
        // =============================

        return authenticateWithUserInfo(userInfo, ci);
    }
}
