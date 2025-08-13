package org.devkor.apu.saerok_server.domain.notification.infra.fcm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushGateway implements PushGateway {

    private final NotificationSettingRepository settingRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FcmMessageClient fcmMessageClient;

    @Override
    public void sendToUser(Long userId, NotificationSubject subject, NotificationAction action, PushMessageCommand cmd) {
        // (1) 설정 로우 일괄 조회 (그룹 + 해당 action)
        List<NotificationSetting> rows = settingRepository.findForSubjectAndAction(userId, subject, action);
        if (rows.isEmpty()) {
            log.debug("No NotificationSetting rows for user={}, subject={}, action={}", userId, subject, action);
            return;
        }

        // (2) 디바이스별 유효 여부 계산
        Map<Long, DeviceState> byDevice = new HashMap<>();
        for (NotificationSetting ns : rows) {
            Long deviceId = ns.getUserDevice().getId();
            DeviceState st = byDevice.computeIfAbsent(deviceId, k -> new DeviceState());
            if (ns.getAction() == null) st.group = ns.enabled();
            else st.detail = ns.enabled();
        }

        List<Long> enabledDeviceIds = byDevice.entrySet().stream()
                .filter(e -> isEnabled(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (enabledDeviceIds.isEmpty()) {
            log.debug("No enabled devices for user={}, subject={}, action={}", userId, subject, action);
            return;
        }

        // (3) 토큰 조회 후 전송
        List<String> tokens = userDeviceRepository.findTokensByUserDeviceIds(enabledDeviceIds);
        if (CollectionUtils.isEmpty(tokens)) return;
        fcmMessageClient.sendToDevices(tokens, cmd);
    }

    private boolean isEnabled(DeviceState s) {
        // 그룹 OFF가 우선 차단
        if (s.group != null && !s.group) return false;
        // 세부 설정이 있으면 그것을 우선 사용
        if (s.detail != null) return s.detail;
        // 세부 설정 없으면 그룹(없으면 디폴트 true)
        return true;
    }

    private static class DeviceState {
        Boolean group;   // action == null
        Boolean detail;  // action == specific
    }
}
