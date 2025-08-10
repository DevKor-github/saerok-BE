package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingsWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationSettingsQueryService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;
    private final NotificationSettingsWebMapper notificationSettingsWebMapper;

    public NotificationSettingsResponse getNotificationSettings(Long userId, String deviceId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        List<NotificationSettings> settings = notificationSettingsRepository.findByUserIdAndDeviceId(userId, deviceId);
        if (settings.isEmpty()) {
            throw new NotFoundException("해당 디바이스의 알림 설정을 찾을 수 없어요");
        }

        Map<NotificationType, Boolean> settingsMap = settings.stream()
                .collect(Collectors.toMap(
                        NotificationSettings::getType,
                        NotificationSettings::getEnabled
                ));

        return new NotificationSettingsResponse(
                deviceId,
                settingsMap.get(NotificationType.LIKE),
                settingsMap.get(NotificationType.COMMENT),
                settingsMap.get(NotificationType.BIRD_ID_SUGGESTION),
                settingsMap.get(NotificationType.SYSTEM)
        );
    }
}
