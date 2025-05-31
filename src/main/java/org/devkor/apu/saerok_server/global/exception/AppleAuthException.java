package org.devkor.apu.saerok_server.global.exception;

/**
 * Apple 소셜 인증 실패 (authorizationCode 유효성 검증 실패 등)
 */
public class AppleAuthException extends BadRequestException {
    public AppleAuthException(String message) {
        super(message);
    }
}
