package org.devkor.apu.saerok_server.domain.user.core.service;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.user.core.repository.BannedWordRepository;
import org.springframework.stereotype.Service;

/**
 * 금칙어 및 욕설 검증을 담당하는 서비스
 * - DB banned_words: 서비스 특화 금칙어 (정확 매칭)
 * - BadWordFiltering: 일반 욕설 및 띄어쓰기 변형 욕설 검사
 */
@Slf4j
@Service
public class BannedWordService {

    private final BannedWordRepository bannedWordRepository;
    private final BadWordFiltering badWordFiltering;

    public BannedWordService(BannedWordRepository bannedWordRepository) {
        this.bannedWordRepository = bannedWordRepository;
        this.badWordFiltering = new BadWordFiltering();
        log.info("BannedWordService initialized with BadWordFiltering library");
    }

    /**
     * 텍스트에 부적절한 표현이 포함되어 있는지 검사
     *
     * @param text 검사할 텍스트
     * @return 부적절한 표현 포함 여부
     */
    public boolean containsBannedWord(String text) {
        String normalizedText = text.trim();

        // 1단계: 서비스 특화 금칙어 검사 (DB)
        if (bannedWordRepository.existsByWordAndIsActiveTrue(normalizedText)) {
            return true;
        }

        // 2단계: 일반 욕설 검사 (라이브러리)
        if (badWordFiltering.check(normalizedText)) {
            return true;
        }

        // 3단계: 띄어쓰기로 우회한 욕설 검사 (라이브러리)
        if (badWordFiltering.blankCheck(normalizedText)) {
            return true;
        }

        return false;
    }

}
