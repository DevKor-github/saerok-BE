package org.devkor.apu.saerok_server.global.exception;

/**
 * 액세스 토큰 인증 예외 (액세스 토큰 검증 실패 등)
 */
public class JwtAuthException extends UnauthorizedException {
    public JwtAuthException(String message) {
        super(message);
    }
}
