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
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoAuthClient;
import org.devkor.apu.saerok_server.domain.auth.support.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.util.dto.ClientInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class KakaoAuthService {

    private final SocialAuthRepository socialAuthRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthTokenFacade authTokenFacade;
    private final KakaoAuthClient kakaoAuthClient;

    public ResponseEntity<AccessTokenResponse> authenticate(String authorizationCode, String accessToken, ClientInfo clientInfo) {
        SocialUserInfo userInfo = kakaoAuthClient.fetch(authorizationCode, accessToken);

        log.info("카카오 서버로부터 받은 사용자 정보 [sub: {}, email: {}]", userInfo.sub(), userInfo.email());

        SocialAuth socialAuth = socialAuthRepository
                .findByProviderAndProviderUserId(SocialProviderType.KAKAO, userInfo.sub())
                .orElseGet(() -> {
                    User user = userRepository.save(User.createUser(userInfo.email()));
                    userRoleRepository.save(UserRole.createUserRole(user, UserRoleType.USER));

                    return socialAuthRepository.save(
                            SocialAuth.createSocialAuth(user, SocialProviderType.KAKAO, userInfo.sub())
                    );
                });

        User user = socialAuth.getUser();
        AuthBundle bundle = authTokenFacade.issueTokens(user, clientInfo);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, bundle.cookie().toString())
                .body(bundle.body());
    }
}
