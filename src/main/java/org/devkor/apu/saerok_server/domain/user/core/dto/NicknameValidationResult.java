package org.devkor.apu.saerok_server.domain.user.core.dto;

/**
 * 닉네임 검증 결과를 담는 순수 데이터 클래스
 */
public record NicknameValidationResult(
        boolean isValid,
        String reason
) {
}
