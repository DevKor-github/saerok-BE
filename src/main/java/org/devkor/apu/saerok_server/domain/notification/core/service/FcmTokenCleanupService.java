package org.devkor.apu.saerok_server.domain.notification.core.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenCleanupService {

    private final DeviceTokenRepository deviceTokenRepository;

    // FCM 전송 결과를 분석하여 무효한 토큰들을 정리합니다.
    public void handleFailedTokens(List<String> originalTokens, BatchResponse response) {
        if (response.getFailureCount() == 0) {
            return;
        }

        List<String> invalidTokens = new ArrayList<>();

        for (int i = 0; i < response.getResponses().size(); i++) {
            SendResponse sendResponse = response.getResponses().get(i);
            if (!sendResponse.isSuccessful()) {
                FirebaseMessagingException exception = sendResponse.getException();
                if (exception != null && isInvalidToken(exception)) {
                    invalidTokens.add(originalTokens.get(i));
                    log.debug("무효한 토큰 발견: {}, 오류: {}",
                            originalTokens.get(i).substring(0, Math.min(10, originalTokens.get(i).length())),
                            exception.getMessagingErrorCode());
                }
            }
        }

        if (!invalidTokens.isEmpty()) {
            deviceTokenRepository.deleteByTokens(invalidTokens);
        }
    }

    // Firebase 예외가 무효한 토큰으로 인한 것인지 확인합니다.
    private boolean isInvalidToken(FirebaseMessagingException exception) {
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.INVALID_ARGUMENT || errorCode == MessagingErrorCode.UNREGISTERED;
    }
}
