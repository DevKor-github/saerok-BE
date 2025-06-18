package org.devkor.apu.saerok_server.domain.user.core.entity;

public enum SignupStatusType {
    PROFILE_REQUIRED, // 소셜 인증만 된 상태 (닉네임 미입력)
    COMPLETED // 소셜 인증 + 회원정보 모두 입력 완료
}
