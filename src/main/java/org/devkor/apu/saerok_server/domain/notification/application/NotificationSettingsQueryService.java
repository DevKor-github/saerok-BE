package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.*;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingsWebMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationSettingsQueryService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;
    private final NotificationSettingsWebMapper notificationSettingsWebMapper;

    /**
     * 사용자의 디바이스별 알림 설정 조회
     */
    public NotificationSettingsResponse getNotificationSettings(GetNotificationSettingsCommand command) {
        userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("유효하지 않은 사용자 id예요"));

        NotificationSettings settings = notificationSettingsRepository
                .findByUserIdAndDeviceId(command.userId(), command.deviceId())
                .orElseThrow(() -> new NotFoundException("해당 디바이스의 알림 설정을 찾을 수 없어요"));

        return notificationSettingsWebMapper.toNotificationSettingsResponse(settings);
    }

    /**
     * 사용자의 모든 디바이스 알림 설정 조회
     */
    public List<NotificationSettingsResponse> getAllNotificationSettings(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유효하지 않은 사용자 id예요"));

        List<NotificationSettings> settingsList = notificationSettingsRepository.findByUserId(userId);

        return settingsList.stream()
                .map(notificationSettingsWebMapper::toNotificationSettingsResponse)
                .toList();
    }
}
