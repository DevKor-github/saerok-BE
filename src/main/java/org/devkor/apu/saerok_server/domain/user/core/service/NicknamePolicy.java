package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.dto.NicknameValidationResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NicknamePolicy {

    private final BannedWordService bannedWordService;
    private final ReservedWordService reservedWordService;

    public boolean isNicknameValid(String nickname) {
        return validateNicknameWithReason(nickname).isValid();
    }
    
    public NicknameValidationResult validateNicknameWithReason(String nickname) {
        // 1. 닉네임 비어있음 검사
        if (nickname == null || nickname.trim().isEmpty()) {
            return new NicknameValidationResult(false, "닉네임이 비어있습니다.");
        }
        
        // 2. 글자수 제한 [2, 9] (2글자 이상 9글자 이하)
        if (nickname.length() < 2) {
            return new NicknameValidationResult(false, "닉네임은 2글자 이상이어야 합니다.");
        }
        if (nickname.length() > 9) {
            return new NicknameValidationResult(false, "닉네임은 9글자 이하여야 합니다.");
        }

        // 3. 공백 불가 (앞뒤 공백 및 중간 공백 모두 불가)
        if (!nickname.equals(nickname.trim()) || nickname.contains(" ")) {
            return new NicknameValidationResult(false, "닉네임에 공백이 포함될 수 없습니다.");
        }

        // 4. 한글/영어/숫자 조합만 가능하며, 한글은 자음/모음 단일로는 불가
        for (char c : nickname.toCharArray()) {
            // 자음/모음 단일 체크
            if (c >= 0x3131 && c <= 0x3163) {
                return new NicknameValidationResult(false, "한글 자음/모음만 단독으로 사용할 수 없습니다.");
            }

            boolean isValidKorean = (c >= 0xAC00 && c <= 0xD7A3);  // 한글
            boolean isValidEnglish = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');  // 영어
            boolean isValidNumber = (c >= '0' && c <= '9');  // 숫자

            if (!isValidKorean && !isValidEnglish && !isValidNumber) {
                return new NicknameValidationResult(false, "한글, 영어, 숫자만 사용할 수 있습니다.");
            }
        }

        // 5. 예약어 검사 (정확 검사)
        if (reservedWordService.isReservedWord(nickname)) {
            return new NicknameValidationResult(false, "사용할 수 없는 단어입니다.");
        }

        // 6. 금칙어 및 욕설 검사 (포함 검사)
        if (bannedWordService.containsBannedWord(nickname)) {
            return new NicknameValidationResult(false, "사용할 수 없는 단어가 포함되어 있습니다.");
        }

        // 다 통과하면 valid
        return new NicknameValidationResult(true, null);
    }
}