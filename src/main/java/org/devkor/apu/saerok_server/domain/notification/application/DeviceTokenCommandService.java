package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterTokenResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.RegisterTokenCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DeviceToken;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.devkor.apu.saerok_server.domain.notification.mapper.DeviceTokenWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceTokenCommandService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;
    private final DeviceTokenWebMapper deviceTokenWebMapper;

    public RegisterTokenResponse registerToken(RegisterTokenCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        Optional<DeviceToken> existingToken = deviceTokenRepository
                .findByUserIdAndDeviceId(command.userId(), command.deviceId());

        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 갱신
            DeviceToken deviceToken = existingToken.get();
            deviceToken.updateToken(command.token());
        } else {
            // 새로운 토큰 등록
            DeviceToken newToken = DeviceToken.create(user, command.deviceId(), command.token());
            deviceTokenRepository.save(newToken);

            // 새 디바이스에 대한 기본 알림 설정 생성
            Optional<NotificationSettings> existingSettings = notificationSettingsRepository
                    .findByUserIdAndDeviceId(command.userId(), command.deviceId());
            
            if (existingSettings.isEmpty()) {
                NotificationSettings defaultSettings = NotificationSettings.createDefault(user, command.deviceId());
                notificationSettingsRepository.save(defaultSettings);
            }
        }

        return deviceTokenWebMapper.toRegisterTokenResponse(command, true);
    }

    public void deleteDevice(Long userId, String deviceId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        deviceTokenRepository.findByUserIdAndDeviceId(userId, deviceId).orElseThrow(() -> new NotFoundException("해당 디바이스를 찾을 수 없어요"));

        deviceTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
        notificationSettingsRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    }

    public void deleteAllTokens(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        deviceTokenRepository.deleteAllByUserId(userId);
        notificationSettingsRepository.deleteByUserId(userId);
    }
}
