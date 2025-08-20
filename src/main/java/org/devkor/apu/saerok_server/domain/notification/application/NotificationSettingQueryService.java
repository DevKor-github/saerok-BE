package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationSettingBackfillService;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingWebMapper;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationSettingQueryService {

    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationSettingBackfillService backfillService;
    private final NotificationSettingWebMapper mapper;

    public NotificationSettingsResponse getNotificationSettings(Long userId, String deviceId) {
        UserDevice userDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new NotFoundException("해당 디바이스를 찾을 수 없어요"));

        // 쓰기 가능한 독립 트랜잭션에서 처리
        backfillService.ensureDefaultsNewTx(userDevice);

        List<NotificationSetting> rows = notificationSettingRepository.findByUserDeviceId(userDevice.getId());
        return mapper.toNotificationSettingsResponse(deviceId, rows);
    }
}
