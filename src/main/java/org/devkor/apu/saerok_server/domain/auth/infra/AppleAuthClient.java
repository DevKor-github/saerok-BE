package org.devkor.apu.saerok_server.domain.auth.infra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.AppleTokenResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppleAuthClient implements SocialAuthClient {

    private final AppleApiClient appleApiClient;

    @Override
    public SocialProviderType provider() {
        return SocialProviderType.APPLE;
    }

    @Override
    public SocialUserInfo fetch(String authorizationCode, String accessToken) {
        AppleTokenResponse response = appleApiClient.requestToken(authorizationCode);
        String idToken = response.getIdToken();
        DecodedJWT jwt = JWT.decode(idToken);
        return new SocialUserInfo(
                jwt.getClaim("sub").asString(),
                jwt.getClaim("email").asString(),
                response.getRefreshToken()
        );
    }
}
