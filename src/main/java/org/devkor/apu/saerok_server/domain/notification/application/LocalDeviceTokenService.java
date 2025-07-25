package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.LocalDeviceTokenResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DeviceToken;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LocalDeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    public LocalDeviceTokenResponse createLocalDummyDeviceToken() {
        User dummyUser = userRepository.findById(99999L)
                .orElseThrow(() -> new IllegalStateException("로컬 더미 유저가 존재하지 않습니다"));

        // 더미 디바이스 정보 생성
        String dummyDeviceId = "dummy_device_99999";
        String dummyFcmToken = "dummy_fcm_token_99999";

        // 기존 더미 디바이스 토큰 확인
        Optional<DeviceToken> existingToken = deviceTokenRepository
                .findByUserIdAndDeviceId(dummyUser.getId(), dummyDeviceId);

        DeviceToken deviceToken;
        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 갱신
            deviceToken = existingToken.get();
            deviceToken.updateToken(dummyFcmToken);
            deviceToken.activate();
        } else {
            // 새로운 더미 토큰 생성
            deviceToken = DeviceToken.createActiveToken(
                    dummyUser,
                    dummyDeviceId,
                    dummyFcmToken
            );
            deviceTokenRepository.save(deviceToken);
        }

        return new LocalDeviceTokenResponse(
                deviceToken.getDeviceId(),
                deviceToken.getToken(),
                deviceToken.getIsActive(),
                dummyUser.getId()
        );
    }
}
