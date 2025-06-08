package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 예약어 검증을 담당하는 서비스
 * - 서비스 특화 예약어 (정확 매칭)
 * - 관리자용 계정명, 서비스명 등을 보호
 */
@Slf4j
@Service
public class ReservedWordService {

    private final Set<String> reservedWords;

    public ReservedWordService(@Value("classpath:reserved-words.txt") Resource resource) {
        this.reservedWords = loadReservedWords(resource);
    }

    /**
     * 텍스트가 예약어인지 검사 (정확 매칭)
     *
     * @param text 검사할 텍스트
     * @return 예약어 여부
     */
    public boolean isReservedWord(String text) {
        return reservedWords.contains(text);
    }

    /**
     * 예약어 파일을 로드하여 Set으로 변환
     */
    private Set<String> loadReservedWords(Resource resource) {
        try {
            return Files.lines(resource.getFile().toPath())
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            log.error("Failed to load reserved words, using defaults", e);
            return Set.of("새록", "saerok", "SAEROK", "admin", "관리자");
        }
    }
}
