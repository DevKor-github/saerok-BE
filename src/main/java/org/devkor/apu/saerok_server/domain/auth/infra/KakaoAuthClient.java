package org.devkor.apu.saerok_server.domain.auth.infra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.KakaoUserInfoResponse;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.global.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthClient implements SocialAuthClient {

    private final KakaoApiClient kakaoApiClient;

    @Override
    public SocialProviderType provider() {
        return SocialProviderType.KAKAO;
    }

    /** 기존: 고정 redirect_uri 경로 */
    @Override
    public SocialUserInfo fetch(String authorizationCode, String accessToken) {

        if (authorizationCode != null) {
            String idToken = kakaoApiClient.requestIdToken(authorizationCode);
            DecodedJWT jwt = JWT.decode(idToken);
            return new SocialUserInfo(
                    jwt.getClaim("sub").asString(),
                    jwt.getClaim("email").asString(),
                    null
            );
        }

        if (accessToken != null) {

            KakaoUserInfoResponse userInfo = kakaoApiClient.fetchUserInfo(accessToken);
            log.info("[fixlog] sub: {}, email: {}", userInfo.getId(), userInfo.getKakaoAccount().getEmail());

            // ✅ 앱 키 섞임 방지: access_token_info 로 appId 검증
            kakaoApiClient.validateAccessTokenAppId(accessToken);
            // TODO: validateAccessTokenAppId가 fetchUserInfo보다 먼저 오는 게 맞는데,
            //  개발 서버 DB의 잘못된 sub 값 수정을 위해 임시 조치함. 롤백하기

            if (userInfo.getKakaoAccount().getIsEmailValid() && userInfo.getKakaoAccount().getIsEmailVerified()) {
                return new SocialUserInfo(
                        userInfo.getId().toString(),
                        userInfo.getKakaoAccount().getEmail(),
                        null
                );
            }
            throw new UnauthorizedException("해당 카카오 계정의 이메일이 유효하지 않거나 인증되지 않아 사용할 수 없어요");
        }

        throw new UnauthorizedException("로그인하려면 인가 코드 또는 액세스 토큰이 필요해요");
    }

    /** 신규: 호출 시점에 redirect_uri를 오버라이드하는 경로 */
    public SocialUserInfo fetchWithRedirect(String authorizationCode, String accessToken, String redirectUriOverride) {

        if (authorizationCode != null) {
            String idToken = kakaoApiClient.requestIdToken(authorizationCode, redirectUriOverride);
            DecodedJWT jwt = JWT.decode(idToken);
            return new SocialUserInfo(
                    jwt.getClaim("sub").asString(),
                    jwt.getClaim("email").asString(),
                    null
            );
        }

        // accessToken 경로는 redirect_uri가 관여하지 않으므로 기존 처리 재사용
        return fetch(null, accessToken);
    }
}
