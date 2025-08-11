package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterTokenResponse;
import org.devkor.apu.saerok_server.domain.notification.application.dto.RegisterTokenCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.mapper.UserDeviceWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDeviceCommandService {

    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;
    private final UserDeviceWebMapper userDeviceWebMapper;

    public RegisterTokenResponse registerToken(RegisterTokenCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        Optional<UserDevice> existingToken = userDeviceRepository
                .findByUserIdAndDeviceId(command.userId(), command.deviceId());

        UserDevice userDevice;
        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 갱신
            userDevice = existingToken.get();
            userDevice.updateToken(command.token());
        } else {
            // 새로운 토큰 등록
            userDevice = UserDevice.create(user, command.deviceId(), command.token());
            userDeviceRepository.save(userDevice);

            // 새 디바이스에 대한 기본 알림 설정 생성
            List<NotificationSetting> existingSettings = notificationSettingRepository
                    .findByUserDeviceId(userDevice.getId());
            
            if (existingSettings.isEmpty()) {
                List<NotificationSetting> defaultSettings = NotificationSetting.createDefaultSetting(userDevice);
                notificationSettingRepository.saveAll(defaultSettings);
            }
        }

        return userDeviceWebMapper.toRegisterTokenResponse(command, true);
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
