package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.FcmMessageService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushDispatchService {
    
    private final FcmMessageService fcmMessageService;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserDeviceRepository userDeviceRepository;

    @Async("pushNotificationExecutor")
    @Transactional(readOnly = true)
    public void sendToUser(Long userId, NotificationType notificationType, PushMessageCommand message) {
        List<NotificationSetting> settings = notificationSettingRepository
                .findByUserIdAndTypeAndEnabledTrue(userId, notificationType);

        if (settings.isEmpty()) {
            return;
        }

        List<Long> userDeviceIds = settings.stream()
                .map(setting -> setting.getUserDevice().getId())
                .toList();

        List<String> fcmTokens = userDeviceRepository.findTokensByUserDeviceIds(userDeviceIds);

        if (fcmTokens.isEmpty()) {
            return;
        }

        fcmMessageService.sendToDevices(fcmTokens, message);
    }
}
