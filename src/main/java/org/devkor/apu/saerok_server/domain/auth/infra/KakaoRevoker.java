package org.devkor.apu.saerok_server.domain.auth.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.global.core.properties.KakaoProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoRevoker implements SocialRevoker {

    private final KakaoAdminApiClient kakaoAdminApiClient;
    private final KakaoProperties kakaoProperties;

    @Override
    public SocialProviderType provider() { return SocialProviderType.KAKAO; }

    @Override
    public void revoke(SocialAuth link) {
        String providerUserId = link.getProviderUserId();
        kakaoAdminApiClient.unlinkUser(kakaoProperties.getAdminKey(), providerUserId);
        log.info("[Kakao] unlink 완료: userId={}, providerUserId={}", link.getUser().getId(), providerUserId);
    }
}