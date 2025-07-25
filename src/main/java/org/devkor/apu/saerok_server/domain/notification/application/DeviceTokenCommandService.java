package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.DeviceTokenToggleResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterTokenResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.*;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DeviceToken;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceTokenCommandService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    public RegisterTokenResponse registerToken(RegisterTokenCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        Optional<DeviceToken> existingToken = deviceTokenRepository
                .findByUserIdAndDeviceId(command.userId(), command.deviceId());

        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 갱신
            DeviceToken deviceToken = existingToken.get();
            deviceToken.updateToken(command.token());
            deviceToken.activate();
        } else {
            // 새로운 토큰 등록
            DeviceToken newToken = DeviceToken.createActiveToken(
                    user, 
                    command.deviceId(), 
                    command.token()
            );
            deviceTokenRepository.save(newToken);
        }

        return new RegisterTokenResponse(command.deviceId(), true);
    }

    public DeviceTokenToggleResponse toggleDevicePushNotification(DeviceTokenToggleCommand command) {
        userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        DeviceToken deviceToken = deviceTokenRepository
                .findByUserIdAndDeviceId(command.userId(), command.deviceId())
                .orElseThrow(() -> new NotFoundException("해당 디바이스를 찾을 수 없어요"));

        if (deviceToken.getIsActive()) {
            deviceToken.deactivate();
        } else {
            deviceToken.activate();
        }

        return new DeviceTokenToggleResponse(command.deviceId(), deviceToken.getIsActive());
    }

    public void deleteDevice(DeviceTokenDeleteCommand command) {
        userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        deviceTokenRepository.findByUserIdAndDeviceId(command.userId(), command.deviceId())
                .orElseThrow(() -> new NotFoundException("해당 디바이스를 찾을 수 없어요"));

        deviceTokenRepository.deleteByUserIdAndDeviceId(command.userId(), command.deviceId());
    }

    public void deleteAllTokens(DeleteAllTokensCommand command) {
        userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        deviceTokenRepository.deleteAllByUserId(command.userId());
    }
}
