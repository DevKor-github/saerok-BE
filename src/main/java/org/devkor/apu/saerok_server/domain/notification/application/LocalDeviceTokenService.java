package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.LocalDeviceTokenResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DeviceToken;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.devkor.apu.saerok_server.domain.notification.mapper.DeviceTokenWebMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LocalDeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final DeviceTokenWebMapper deviceTokenWebMapper;

    public LocalDeviceTokenResponse createLocalDummyDeviceToken() {
        User dummyUser = userRepository.findById(99999L)
                .orElseThrow(() -> new IllegalStateException("로컬 더미 유저가 존재하지 않습니다"));

        // 더미 디바이스 정보 생성
        String dummyDeviceId = "dummy_device_99999";
        String dummyFcmToken = "dummy_fcm_token_99999";

        // 기존 더미 디바이스 토큰 확인 및 처리
        DeviceToken deviceToken = deviceTokenRepository
                .findByUserIdAndDeviceId(dummyUser.getId(), dummyDeviceId)
                .map(token -> {
                    // 기존 토큰이 있으면 갱신
                    token.updateToken(dummyFcmToken);
                    return token;
                })
                .orElseGet(() -> {
                    // 새로운 더미 토큰 생성
                    DeviceToken newDeviceToken = DeviceToken.create(
                            dummyUser,
                            dummyDeviceId,
                            dummyFcmToken
                    );
                    deviceTokenRepository.save(newDeviceToken);
                    return newDeviceToken;
                });

        // 알림 설정 확인 및 생성
        notificationSettingsRepository
                .findByUserIdAndDeviceId(dummyUser.getId(), dummyDeviceId)
                .orElseGet(() -> {
                    // 기존 알림 설정이 없으면 기본 설정으로 생성
                    NotificationSettings newSettings = NotificationSettings.createDefault(
                            dummyUser,
                            dummyDeviceId
                    );
                    notificationSettingsRepository.save(newSettings);
                    return newSettings;
                });

        return deviceTokenWebMapper.toLocalDeviceTokenResponse(deviceToken);
    }
}
