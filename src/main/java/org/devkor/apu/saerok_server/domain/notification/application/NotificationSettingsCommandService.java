package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.*;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingsWebMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationSettingsCommandService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;
    private final NotificationSettingsWebMapper notificationSettingsWebMapper;

    /**
     * 특정 알림 유형 토글
     */
    public ToggleNotificationResponse toggleNotificationSetting(ToggleNotificationSettingCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("유효하지 않은 사용자 id예요"));

        NotificationSettings settings = notificationSettingsRepository
                .findByUserIdAndDeviceId(command.userId(), command.deviceId())
                .orElseThrow(() -> new NotFoundException("해당 디바이스의 알림 설정을 찾을 수 없어요"));

        settings.toggleNotification(command.notificationType());

        boolean isEnabled = settings.isNotificationEnabled(command.notificationType());

        return notificationSettingsWebMapper.toToggleNotificationResponse(command, isEnabled);
    }
}
