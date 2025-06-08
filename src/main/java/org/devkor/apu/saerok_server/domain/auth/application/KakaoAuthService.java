package org.devkor.apu.saerok_server.domain.auth.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade.AuthBundle;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoApiClient;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.KakaoUserInfoResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.exception.UnauthorizedException;
import org.devkor.apu.saerok_server.global.util.dto.ClientInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoApiClient kakaoApiClient;
    private final SocialAuthRepository socialAuthRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthTokenFacade authTokenFacade;

    public ResponseEntity<AccessTokenResponse> authenticate(String authorizationCode, String accessToken, ClientInfo clientInfo) {

        String sub;
        String email;

        if (authorizationCode != null) {
            String idToken = kakaoApiClient.requestIdToken(authorizationCode);
            DecodedJWT jwt = JWT.decode(idToken);
            sub = jwt.getClaim("sub").asString();
            email = jwt.getClaim("email").asString();
        } else if (accessToken != null) {
            KakaoUserInfoResponse userInfo = kakaoApiClient.fetchUserInfo(accessToken);
            sub = userInfo.getId().toString();
            if (userInfo.getKakaoAccount().getIsEmailValid() && userInfo.getKakaoAccount().getIsEmailVerified()) {
                email = userInfo.getKakaoAccount().getEmail();
            } else {
                throw new UnauthorizedException("해당 카카오 계정의 이메일이 유효하지 않거나 인증되지 않아 사용할 수 없어요");
            }
        } else {
            throw new UnauthorizedException("로그인하려면 인가 코드 또는 액세스 토큰이 필요해요");
        }

        log.info("카카오 서버로부터 받은 사용자 정보 [sub: {}, email: {}]", sub, email);

        SocialAuth socialAuth = socialAuthRepository
                .findByProviderAndProviderUserId(SocialProviderType.KAKAO, sub)
                .orElseGet(() -> {
                    User user = userRepository.save(User.createUser(email));
                    userRoleRepository.save(UserRole.createUserRole(user, UserRoleType.USER));

                    return socialAuthRepository.save(
                            SocialAuth.createSocialAuth(user, SocialProviderType.KAKAO, sub)
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
