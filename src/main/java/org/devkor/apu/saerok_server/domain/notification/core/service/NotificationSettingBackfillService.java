package org.devkor.apu.saerok_server.domain.notification.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationSettingBackfillService {

    private final NotificationTypeSchema schema;
    private final NotificationSettingRepository settingRepository;

    /** 읽기 흐름 보존: 별도 트랜잭션으로 누락값 보정 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureDefaultsNewTx(UserDevice device) {
        ensureDefaults(device);
    }

    /** 동일 트랜잭션 내 보정 */
    @Transactional
    public void ensureDefaults(UserDevice device) {
        List<NotificationSetting> existing = settingRepository.findByUserDeviceId(device.getId());

        Set<NotificationType> have = new HashSet<>();
        for (NotificationSetting ns : existing) have.add(ns.getType());

        Set<NotificationType> need = EnumSet.copyOf(schema.requiredTypes());
        need.removeAll(have);

        if (!need.isEmpty()) {
            for (NotificationType t : need) {
                // 디폴트 on/off 정책: 기존 로직이 없으므로 기본 true로 시작(필요시 정책 변경)
                settingRepository.save(NotificationSetting.of(device, t, true));
            }
        }
    }
}
