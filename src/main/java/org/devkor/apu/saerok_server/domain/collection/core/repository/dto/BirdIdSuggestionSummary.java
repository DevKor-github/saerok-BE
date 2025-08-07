package org.devkor.apu.saerok_server.domain.collection.core.repository.dto;

/**
 * 특정 bird에 대한 동정 의견 종합 정보
 */
public record BirdIdSuggestionSummary(
        Long birdId,
        String birdKoreanName,
        String birdScientificName,
        String birdThumbImageObjectKey,
        Long agreeCount,
        Long disagreeCount,
        Boolean isAgreedByMe,
        Boolean isDisagreedByMe
) {
}
