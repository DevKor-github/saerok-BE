package org.devkor.apu.saerok_server.domain.user.core.service;

import org.springframework.stereotype.Service;

@Service
public class UserProfilePolicy {

    public boolean isNicknameValid(String nickname) {

        // 1. 닉네임은 null이거나 0자일 수 없음
        if (nickname == null || nickname.isEmpty()) return false;

        // 2. 글자수 제한 [2, 9] (2글자 이상 9글자 이하)
        if (nickname.length() < 2 || nickname.length() > 9) return false;

        // 3. 공백 불가 (앞뒤 공백 및 중간 공백 모두 불가)
        if (!nickname.equals(nickname.trim()) || nickname.contains(" ")) return false;

        // 4. 한글/영어/숫자 조합만 가능하며, 한글은 자음/모음 단일로는 불가
        for (char c : nickname.toCharArray()) {
            // 자음/모음 단일 체크 (U+3131-U+3163)
            if (c >= 0x3131 && c <= 0x3163) return false;

            boolean isValidKorean = (c >= 0xAC00 && c <= 0xD7A3);  // 한글
            boolean isValidEnglish = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');  // 영어
            boolean isValidNumber = (c >= '0' && c <= '9');  // 숫자

            if (!isValidKorean && !isValidEnglish && !isValidNumber) {
                return false;
            }
        }

        return true;
    }

    public boolean isEmailValid(String email) {
        if (email == null) return false;

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}