package org.devkor.apu.saerok_server.domain.user.mapper;

import org.devkor.apu.saerok_server.domain.user.api.response.CheckNicknameResponse;
import org.devkor.apu.saerok_server.domain.user.core.dto.NicknameValidationResult;
import org.springframework.stereotype.Component;

@Component
public class NicknameMapper {

    /**
     * 닉네임 검증 결과를 응답 DTO로 변환
     * 
     * @param isValidByPolicy 닉네임 정책 준수 여부
     * @param isUsedByOtherUser 다른 사용자 사용 여부
     * @param reason 사용 불가 이유 (사용 가능한 경우 null)
     * (true, false, null)이면 통과임.
     */
    public CheckNicknameResponse toCheckNicknameResponse(boolean isValidByPolicy, boolean isUsedByOtherUser, String reason) {
        return new CheckNicknameResponse(isValidByPolicy, isUsedByOtherUser, reason);
    }
}
