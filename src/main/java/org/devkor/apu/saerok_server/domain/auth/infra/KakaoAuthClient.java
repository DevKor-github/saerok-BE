package org.devkor.apu.saerok_server.domain.auth.infra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.KakaoUserInfoResponse;
import org.devkor.apu.saerok_server.domain.auth.support.SocialAuthClient;
import org.devkor.apu.saerok_server.domain.auth.support.SocialUserInfo;
import org.devkor.apu.saerok_server.global.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAuthClient implements SocialAuthClient {

    private final KakaoApiClient kakaoApiClient;

    @Override
    public SocialProviderType provider() {
        return SocialProviderType.KAKAO;
    }

    @Override
    public SocialUserInfo fetch(String authorizationCode, String accessToken) {

        if (authorizationCode != null) {
            String idToken = kakaoApiClient.requestIdToken(authorizationCode);
            DecodedJWT jwt = JWT.decode(idToken);
            return new SocialUserInfo(
                    jwt.getClaim("sub").asString(),
                    jwt.getClaim("email").asString()
            );
        }

        if (accessToken != null) {
            KakaoUserInfoResponse userInfo = kakaoApiClient.fetchUserInfo(accessToken);
            if (userInfo.getKakaoAccount().getIsEmailValid() && userInfo.getKakaoAccount().getIsEmailVerified()) {
                return new SocialUserInfo(
                        userInfo.getId().toString(),
                        userInfo.getKakaoAccount().getEmail()
                );
            }
            throw new UnauthorizedException("해당 카카오 계정의 이메일이 유효하지 않거나 인증되지 않아 사용할 수 없어요");
        }

        throw new UnauthorizedException("로그인하려면 인가 코드 또는 액세스 토큰이 필요해요");
    }
}
