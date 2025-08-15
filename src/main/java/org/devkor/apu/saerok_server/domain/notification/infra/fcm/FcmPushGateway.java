package org.devkor.apu.saerok_server.domain.notification.infra.fcm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeResolver;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile({"dev","prod"})
@RequiredArgsConstructor
public class FcmPushGateway implements PushGateway {

    private final NotificationSettingRepository settingRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FcmMessageClient fcmMessageClient;

    @Override
    public void sendToUser(Long userId, NotificationSubject subject, NotificationAction action, PushMessageCommand cmd) {
        NotificationType type = NotificationTypeResolver.from(subject, action);

        List<Long> deviceIds = settingRepository.findEnabledDeviceIdsByUserAndType(userId, type);
        if (deviceIds.isEmpty()) {
            log.debug("No enabled devices for user={}, type={}", userId, type);
            return;
        }

        List<String> tokens = userDeviceRepository.findTokensByUserDeviceIds(deviceIds);
        if (tokens.isEmpty()) return;
        fcmMessageClient.sendToDevices(tokens, cmd);
    }
}
