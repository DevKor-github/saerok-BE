package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.core.entity.UserRefreshToken;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.domain.notification.application.UserDeviceCommandService;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock RefreshTokenProvider refreshTokenProvider;
    @Mock UserRefreshTokenRepository userRefreshTokenRepository;
    @Mock UserDeviceCommandService userDeviceCommandService;

    @InjectMocks LogoutService logoutService;

    @Test
    @DisplayName("로그아웃 시 현재 유저 refresh token을 revoke하고 현재 디바이스 토큰을 비활성화한다")
    void logout_revokesRefreshTokenAndDeactivatesDevice() {
        UserRefreshToken token = refreshTokenFor(user(42L), "refresh-hash");
        given(refreshTokenProvider.hash("raw-refresh")).willReturn("refresh-hash");
        given(userRefreshTokenRepository.findByRefreshTokenHash("refresh-hash")).willReturn(Optional.of(token));

        logoutService.logout(42L, "raw-refresh", "device-1", DevicePlatform.IOS);

        assertThat(token.getRevokedAt()).isNotNull();
        verify(userDeviceCommandService).deactivateDeviceIfPresent(42L, "device-1", DevicePlatform.IOS);
    }

    @Test
    @DisplayName("refresh token이 이미 revoke되어 있으면 다시 revoke하지 않고 디바이스만 정리한다")
    void logout_alreadyRevokedRefreshToken() {
        UserRefreshToken token = refreshTokenFor(user(42L), "refresh-hash");
        token.revoke();
        var revokedAt = token.getRevokedAt();
        given(refreshTokenProvider.hash("raw-refresh")).willReturn("refresh-hash");
        given(userRefreshTokenRepository.findByRefreshTokenHash("refresh-hash")).willReturn(Optional.of(token));

        logoutService.logout(42L, "raw-refresh", "device-1", null);

        assertThat(token.getRevokedAt()).isEqualTo(revokedAt);
        verify(userDeviceCommandService).deactivateDeviceIfPresent(42L, "device-1", null);
    }

    @Test
    @DisplayName("refresh token의 유저가 달라도 해당 토큰은 무시하고 디바이스 정리는 계속한다")
    void logout_refreshTokenOwnerMismatch() {
        UserRefreshToken token = refreshTokenFor(user(99L), "refresh-hash");
        given(refreshTokenProvider.hash("raw-refresh")).willReturn("refresh-hash");
        given(userRefreshTokenRepository.findByRefreshTokenHash("refresh-hash")).willReturn(Optional.of(token));

        logoutService.logout(42L, "raw-refresh", "device-1", DevicePlatform.IOS);

        assertThat(token.getRevokedAt()).isNull();
        verify(userDeviceCommandService).deactivateDeviceIfPresent(42L, "device-1", DevicePlatform.IOS);
    }

    @Test
    @DisplayName("refresh token과 deviceId가 없으면 idempotent하게 아무 작업도 하지 않는다")
    void logout_withoutRefreshTokenAndDevice() {
        logoutService.logout(42L, null, null, null);

        verifyNoInteractions(refreshTokenProvider, userRefreshTokenRepository, userDeviceCommandService);
    }

    @Test
    @DisplayName("존재하지 않는 refresh token은 무시하고 디바이스 정리는 수행한다")
    void logout_missingRefreshTokenRow() {
        given(refreshTokenProvider.hash("raw-refresh")).willReturn("refresh-hash");
        given(userRefreshTokenRepository.findByRefreshTokenHash("refresh-hash")).willReturn(Optional.empty());

        logoutService.logout(42L, "raw-refresh", "device-1", DevicePlatform.ANDROID);

        verify(userDeviceCommandService).deactivateDeviceIfPresent(42L, "device-1", DevicePlatform.ANDROID);
        verifyNoMoreInteractions(userDeviceCommandService);
    }

    private User user(Long id) {
        User user = User.createUser("user" + id + "@example.com");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UserRefreshToken refreshTokenFor(User user, String hash) {
        return UserRefreshToken.create(
                user,
                hash,
                "Mozilla/5.0",
                "127.0.0.1",
                Duration.ofDays(30)
        );
    }
}
