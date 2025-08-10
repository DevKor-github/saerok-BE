package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.ToggleNotificationSettingCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingsWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationSettingsCommandService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;
    private final NotificationSettingsWebMapper notificationSettingsWebMapper;

    public ToggleNotificationResponse toggleNotificationSetting(ToggleNotificationSettingCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        NotificationSettings settings = notificationSettingsRepository
                .findByUserIdAndDeviceIdAndType(command.userId(), command.deviceId(), command.notificationType())
                .orElseThrow(() -> new NotFoundException("해당 디바이스의 알림 설정을 찾을 수 없어요"));

        settings.toggleNotificationSetting();

        boolean isEnabled = settings.isNotificationEnabled();

        return notificationSettingsWebMapper.toToggleNotificationResponse(command, isEnabled);
    }
}
