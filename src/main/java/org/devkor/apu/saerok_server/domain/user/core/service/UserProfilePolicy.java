package org.devkor.apu.saerok_server.domain.user.core.service;

import org.springframework.stereotype.Service;

@Service
public class UserProfilePolicy {

    public boolean isNicknameValid(String nickname) {

        if (nickname == null || nickname.isEmpty()) return false; // 닉네임은 null이거나 0자일 수 없음
        if (!nickname.equals(nickname.trim())) return false; // 닉네임 앞뒤로 공백이 있을 수 없음

        return true;
    }

    public boolean isEmailValid(String email) {
        if (email == null) return false;

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}