package org.devkor.apu.saerok_server.domain.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenProvider refreshTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserDeviceRepository userDeviceRepository;

    public void logout(Long userId, String refreshToken, String deviceId, DevicePlatform platform) {
        revokeRefreshToken(userId, refreshToken);
        deleteCurrentDevice(userId, deviceId, platform);
    }

    private void revokeRefreshToken(Long userId, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        userRefreshTokenRepository.findByRefreshTokenHash(refreshTokenProvider.hash(refreshToken))
                .ifPresent(token -> {
                    if (!token.getUser().getId().equals(userId)) {
                        log.warn("리프레시 토큰 소유주가 일치하지 않습니다: authenticatedUserId={}, tokenUserId={}",
                                userId, token.getUser().getId());
                        return;
                    }

                    if (token.getRevokedAt() == null) {
                        token.revoke();
                    }
                });
    }

    private void deleteCurrentDevice(Long userId, String deviceId, DevicePlatform platform) {
        if (deviceId == null || deviceId.isBlank()) {
            return;
        }

        DevicePlatform resolvedPlatform = platform != null ? platform : DevicePlatform.IOS;
        userDeviceRepository.deleteByUserIdAndDeviceIdAndPlatform(userId, deviceId, resolvedPlatform);
    }
}
