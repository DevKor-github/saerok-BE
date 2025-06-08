package org.devkor.apu.saerok_server.domain.user.core.service;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 금칙어 및 욕설 검증을 담당하는 서비스
 * - DB banned_words: 부분 매칭 (추후 다시 구현)
 * - BadWordFiltering: 일반 욕설 검사
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannedWordService {

    private final BadWordFiltering badWordFiltering;

    /**
     * 텍스트에 부적절한 표현이 포함되어 있는지 검사
     *
     * @param text 검사할 텍스트
     * @return 부적절한 표현 포함 여부
     */
    public boolean containsBannedWord(String text) {
        // 욕설/비속어 검사 (라이브러리)
        if (badWordFiltering.check(text)) {
            return true;
        }

        return false;
    }

}
