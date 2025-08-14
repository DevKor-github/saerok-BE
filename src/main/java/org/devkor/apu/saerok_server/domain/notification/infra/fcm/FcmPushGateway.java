// ===== ./src/main/java/org/devkor/apu/saerok_server/domain/notification/infra/gateway/FcmPushGateway.java =====
package org.devkor.apu.saerok_server.domain.notification.infra.gateway;

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
import org.devkor.apu.saerok_server.domain.notification.infra.fcm.FcmMessageClient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushGateway implements PushGateway {

    private final NotificationSettingRepository settingRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FcmMessageClient fcmMessageClient;

    @Override
    public void sendToUser(Long userId, NotificationSubject subject, NotificationAction action, PushMessageCommand cmd) {
        // subject/action -> type 매핑
        NotificationType type = NotificationTypeResolver.from(subject, action);

        // type 기준으로 enabled 디바이스 id 조회
        List<Long> deviceIds = settingRepository.findEnabledDeviceIdsByUserAndType(userId, type);
        if (deviceIds.isEmpty()) {
            log.debug("No enabled devices for user={}, type={}", userId, type);
            return;
        }

        // 토큰 조회 후 전송
        List<String> tokens = userDeviceRepository.findTokensByUserDeviceIds(deviceIds);
        if (CollectionUtils.isEmpty(tokens)) return;
        fcmMessageClient.sendToDevices(tokens, cmd);
    }
}
