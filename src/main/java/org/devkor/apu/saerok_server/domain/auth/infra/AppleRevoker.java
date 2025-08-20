package org.devkor.apu.saerok_server.domain.auth.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.global.security.crypto.DataCryptoService;
import org.devkor.apu.saerok_server.global.security.crypto.EncryptedPayload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleRevoker implements SocialRevoker {

    private final AppleApiClient appleApiClient;
    private final DataCryptoService dataCryptoService;

    @Override
    public SocialProviderType provider() { return SocialProviderType.APPLE; }

    @Override
    public void revoke(SocialAuth link) {
        if (link.getRefreshToken() == null || link.getRefreshToken().getCiphertext() == null) {
            log.warn("[Apple] refresh token 없음: userId={}, providerUserId={}",
                    link.getUser().getId(), link.getProviderUserId());
            return; // 이미 없는 상태로 간주 (idempotent)
        }
        EncryptedPayload encryptedPayload = new EncryptedPayload(
                link.getRefreshToken().getCiphertext(),
                link.getRefreshToken().getKey(),
                link.getRefreshToken().getIv(),
                link.getRefreshToken().getTag()
        );
        byte[] refreshTokenBytes = dataCryptoService.decrypt(encryptedPayload);
        String refreshToken = new String(refreshTokenBytes, StandardCharsets.UTF_8);
        appleApiClient.revokeRefreshToken(refreshToken);
        log.info("[Apple] revoke 완료: userId={}, providerUserId={}", link.getUser().getId(), link.getProviderUserId());
    }
}