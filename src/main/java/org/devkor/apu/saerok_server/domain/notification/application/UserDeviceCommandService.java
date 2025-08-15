package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterUserDeviceResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.RegisterUserDeviceCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationSettingBackfillService;
import org.devkor.apu.saerok_server.domain.notification.mapper.UserDeviceWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDeviceCommandService {

    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;
    private final UserDeviceWebMapper userDeviceWebMapper;
    private final NotificationSettingBackfillService backfillService;

    public RegisterUserDeviceResponse registerUserDevice(RegisterUserDeviceCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        if (command.deviceId() == null || command.deviceId().isEmpty()
                || command.token() == null || command.token().isEmpty()) {
            throw new BadRequestException("deviceId와 token은 필수입니다");
        }

        UserDevice userDevice = userDeviceRepository
                .findByUserIdAndDeviceId(command.userId(), command.deviceId())
                .map(existing -> {
                    existing.updateToken(command.token());
                    return existing;
                })
                .orElseGet(() -> {
                    UserDevice newDevice = UserDevice.create(user, command.deviceId(), command.token());
                    userDeviceRepository.save(newDevice);
                    userDeviceRepository.flush();
                    return newDevice;
                });

        backfillService.ensureDefaults(userDevice);

        return userDeviceWebMapper.toRegisterUserDeviceResponse(command, true);
    }

    public void deleteDevice(Long userId, String deviceId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId).orElseThrow(() -> new NotFoundException("해당 디바이스를 찾을 수 없어요"));

        userDeviceRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    }

    public void deleteAllTokens(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        notificationSettingRepository.deleteByUserId(userId);
        userDeviceRepository.deleteAllByUserId(userId);
    }
}
