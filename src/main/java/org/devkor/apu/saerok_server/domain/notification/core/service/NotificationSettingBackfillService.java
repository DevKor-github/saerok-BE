package org.devkor.apu.saerok_server.domain.notification.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationSettingSchema.SubjectActionKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 각 UserDevice에 필요한 알림 설정이 없으면 기본값으로 채워 넣는다. */
@Service
@RequiredArgsConstructor
public class NotificationSettingBackfillService {

    private final NotificationSettingSchema schema;
    private final NotificationSettingRepository notificationSettingRepository;

    /**
     * 독립 트랜잭션으로 백필.
     * - 조회 중에도 안전하게 쓰기 가능
     * - 동시성: UNIQUE(user_device_id, subject, action)에 의해 보호됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureDefaultsNewTx(UserDevice userDevice) {
        ensureDefaultsCore(userDevice);
    }

    /** 같은 트랜잭션에서 수행: UserDevice 등록 직후 호출용 */
    @Transactional
    public void ensureDefaultsSameTx(UserDevice userDevice) {
        ensureDefaultsCore(userDevice);
    }

    private void ensureDefaultsCore(UserDevice userDevice) {
        List<NotificationSetting> existing = notificationSettingRepository.findByUserDeviceId(userDevice.getId());

        // 이미 있는 (subject, action) 키 집합
        Set<SubjectActionKey> present = new HashSet<>();
        for (NotificationSetting ns : existing) {
            present.add(new SubjectActionKey(ns.getSubject(), ns.getAction()));
        }

        // 필요한 키 중 빠진 것만 생성
        Set<SubjectActionKey> required = schema.requiredKeys();
        Set<NotificationSetting> toCreate = new HashSet<>();
        for (SubjectActionKey k : required) {
            if (!present.contains(k)) {
                toCreate.add(NotificationSetting.builder()
                        .userDevice(userDevice)
                        .subject(k.subject())
                        .action(k.action())
                        .enabled(true) // 기본 ON
                        .build());
            }
        }

        if (toCreate.isEmpty()) return;

        notificationSettingRepository.saveAll(toCreate.stream().toList());
    }
}