package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.ToggleNotificationSettingCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationSettingCommandService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;
    private final NotificationSettingWebMapper notificationSettingWebMapper;

    public ToggleNotificationResponse toggleNotificationSetting(ToggleNotificationSettingCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        NotificationSetting settings = notificationSettingRepository
                .findByUserIdAndDeviceIdAndType(command.userId(), command.deviceId(), command.notificationType())
                .orElseThrow(() -> new NotFoundException("해당 디바이스의 알림 설정을 찾을 수 없어요"));

        settings.toggleNotificationSetting();

        boolean isEnabled = settings.isNotificationEnabled();

        return notificationSettingWebMapper.toToggleNotificationResponse(command, isEnabled);
    }
}
