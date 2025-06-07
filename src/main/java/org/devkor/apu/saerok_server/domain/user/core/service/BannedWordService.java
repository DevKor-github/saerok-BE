package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.user.core.entity.BannedWord;
import org.devkor.apu.saerok_server.domain.user.core.repository.BannedWordRepository;
import org.springframework.stereotype.Service;

/**
 * 금칙어 검증을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannedWordService {

    private final BannedWordRepository bannedWordRepository;

    /**
     * 텍스트에 금칙어가 포함되어 있는지 확인
     * 
     * @param text 검증할 텍스트
     * @return 금칙어 포함 여부
     */
    public boolean containsBannedWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // 정확 매칭 검사
        return bannedWordRepository.existsByWordAndIsActiveTrue(text.trim());
    }
}
