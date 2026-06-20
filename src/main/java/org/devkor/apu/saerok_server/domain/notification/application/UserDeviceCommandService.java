package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterUserDeviceResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.RegisterUserDeviceCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationSettingBackfillService;
import org.devkor.apu.saerok_server.domain.notification.mapper.UserDeviceWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDeviceCommandService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;
    private final UserDeviceWebMapper userDeviceWebMapper;
    private final NotificationSettingBackfillService backfillService;

    public RegisterUserDeviceResponse registerUserDevice(RegisterUserDeviceCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        if (command.deviceId() == null || command.deviceId().isBlank()
                || command.token() == null || command.token().isBlank()) {
            throw new BadRequestException("deviceId, token은 필수입니다");
        }

        DevicePlatform platform = command.platform() != null ? command.platform() : DevicePlatform.IOS;

        userDeviceRepository.deactivateConflictingTokensForRegistration(
                command.userId(),
                command.deviceId(),
                platform,
                command.token()
        );

        UserDevice userDevice = userDeviceRepository
                .findByUserIdAndDeviceIdAndPlatform(command.userId(), command.deviceId(), platform)
                .map(existing -> {
                    existing.activateToken(command.token());
                    return existing;
                })
                .orElseGet(() -> {
                    UserDevice newDevice = UserDevice.create(user, command.deviceId(), command.token(), platform);
                    userDeviceRepository.save(newDevice);
                    userDeviceRepository.flush();
                    return newDevice;
                });

        backfillService.ensureDefaults(userDevice);

        return userDeviceWebMapper.toRegisterUserDeviceResponse(command, true);
    }

    public void deactivateDeviceIfPresent(Long userId, String deviceId, DevicePlatform platform) {
        if (deviceId == null || deviceId.isBlank()) {
            return;
        }

        DevicePlatform resolvedPlatform = platform != null ? platform : DevicePlatform.IOS;
        userDeviceRepository.findByUserIdAndDeviceIdAndPlatform(userId, deviceId, resolvedPlatform)
                .ifPresent(UserDevice::deactivateToken);
    }

    public void deactivateInvalidTokens(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        List<String> validTokens = tokens.stream()
                .filter(token -> token != null && !token.isBlank())
                .distinct()
                .toList();

        userDeviceRepository.deactivateByTokens(validTokens);
    }
}
