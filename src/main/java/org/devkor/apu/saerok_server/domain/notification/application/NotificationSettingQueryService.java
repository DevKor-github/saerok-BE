package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse.SubjectSettings;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationSettingBackfillService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationSettingQueryService {

    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationSettingBackfillService backfillService;

    public NotificationSettingsResponse getNotificationSettings(Long userId, String deviceId) {
        UserDevice userDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new NotFoundException("해당 디바이스를 찾을 수 없어요"));

        // 읽기 흐름 유지 + 누락 기본값은 독립 트랜잭션으로 보정
        backfillService.ensureDefaults(userDevice);

        List<NotificationSetting> list = notificationSettingRepository.findByUserDeviceId(userDevice.getId());

        // 1) subject 단위로 모은 뒤, groupEnabled / actions 맵 구성
        Map<NotificationSubject, SubjectAccumulator> accMap = new EnumMap<>(NotificationSubject.class);
        for (NotificationSetting ns : list) {
            SubjectAccumulator acc = accMap.computeIfAbsent(ns.getSubject(), s -> new SubjectAccumulator());
            if (ns.getAction() == null) {
                acc.groupEnabled = ns.enabled(); // 엔티티의 게터명이 enabled()인 점을 반영
            } else {
                acc.actions.put(ns.getAction(), ns.enabled());
            }
        }

        // 2) 불변 DTO로 변환
        Map<NotificationSubject, SubjectSettings> subjects = accMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new SubjectSettings(
                                e.getValue().groupEnabled,
                                Collections.unmodifiableMap(new EnumMap<>(e.getValue().actions))
                        ),
                        (a, b) -> a,
                        () -> new EnumMap<>(NotificationSubject.class)
                ));

        return new NotificationSettingsResponse(deviceId, Collections.unmodifiableMap(subjects));
    }

    /** 내부 가변 누적기 */
    private static class SubjectAccumulator {
        Boolean groupEnabled; // null일 수도 있음
        Map<NotificationAction, Boolean> actions = new EnumMap<>(NotificationAction.class);
    }
}
