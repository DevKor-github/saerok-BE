package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.ToggleNotificationSettingCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationSettingBackfillService;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingWebMapper;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationSettingCommandService {

    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationSettingBackfillService backfillService;
    private final NotificationSettingWebMapper notificationSettingWebMapper;

    public ToggleNotificationResponse toggleNotificationSetting(ToggleNotificationSettingCommand command) {
        UserDevice device = userDeviceRepository.findByUserIdAndDeviceId(command.userId(), command.deviceId())
                .orElseThrow(() -> new NotFoundException("해당 디바이스를 찾을 수 없어요"));

        backfillService.ensureDefaults(device);

        NotificationSetting setting = notificationSettingRepository
                .findByUserDeviceIdAndType(device.getId(), command.type())
                .orElseThrow(() -> new IllegalStateException("서버 오류: 해당 알림 설정 없음"));

        setting.toggle();
        return notificationSettingWebMapper.toToggleNotificationResponse(command, setting.getEnabled());
    }
}
