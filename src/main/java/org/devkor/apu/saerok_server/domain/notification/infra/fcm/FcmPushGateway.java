package org.devkor.apu.saerok_server.domain.notification.infra.fcm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushGateway implements PushGateway {
    
    private final FcmMessageClient fcmMessageClient;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserDeviceRepository userDeviceRepository;

    // TODO: 실제 푸시 알림 전송은 메인 트랜잭션 커밋 이후에 이뤄지도록 변경해야 함
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

        fcmMessageClient.sendToDevices(fcmTokens, message);
    }
}
