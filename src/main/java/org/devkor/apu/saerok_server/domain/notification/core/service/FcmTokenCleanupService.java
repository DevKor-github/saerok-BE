package org.devkor.apu.saerok_server.domain.notification.core.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenCleanupService {

    private final DeviceTokenRepository deviceTokenRepository;

    public void cleanupInvalidToken(String token) {
        try {
            deviceTokenRepository.deleteByToken(token);
        } catch (Exception e) {
            log.error("FCM 토큰 삭제 중 오류 발생: {}", e.getMessage());
        }
    }

    public boolean isInvalidToken(FirebaseMessagingException exception) {
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.INVALID_ARGUMENT || errorCode == MessagingErrorCode.UNREGISTERED;
    }
}
