package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
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
     * channel == "admin" 인 경우, 해당 카카오 계정이 ADMIN_VIEWER 또는 ADMIN_EDITOR 권한을
     * 이미 보유하고 있는지 선확인하고, 없으면 403으로 거부한다.
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

        // admin 채널 선권한 검증 (미보유시 관리자 시스템 로그인 불가)
        if ("admin".equalsIgnoreCase(channel)) {
            SocialProviderType provider = kakaoAuthClient.provider();

            var link = socialAuthRepository
                    .findByProviderAndProviderUserId(provider, userInfo.sub())
                    .orElseThrow(() -> new ForbiddenException("관리자 권한이 없는 계정입니다."));

            boolean hasAdminRole = userRoleRepository.findByUser(link.getUser()).stream()
                    .anyMatch(ur ->
                            ur.getRole() == UserRoleType.ADMIN_VIEWER
                                    || ur.getRole() == UserRoleType.ADMIN_EDITOR
                    );

            if (!hasAdminRole) {
                throw new ForbiddenException("관리자 권한이 없는 계정입니다. (ADMIN_VIEWER/ADMIN_EDITOR 필요)");
            }
        }

        // 이후 공통 후처리(토큰 발급, 쿠키 설정 등)는 상위 클래스에서 처리
        return authenticateWithUserInfo(userInfo, ci);
    }
}
