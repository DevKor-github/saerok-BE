package org.devkor.apu.saerok_server.domain.notification.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationSettingInitService {

    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public void createDefaultSettingsForDevice(Long userDeviceId) {
        UserDevice userDevice = userDeviceRepository.findById(userDeviceId)
                .orElseThrow(() -> new NotFoundException("해당 디바이스를 찾을 수 없어요"));

        // 기존 설정이 있는지 확인 (중복 생성 방지)
        List<NotificationSetting> existingSettings = notificationSettingRepository.findByUserDeviceId(userDeviceId);

        if (existingSettings.isEmpty()) {
            List<NotificationSetting> defaultSettings = NotificationSetting.createDefaultSetting(userDevice);
            notificationSettingRepository.saveAll(defaultSettings);
        }
    }
}
