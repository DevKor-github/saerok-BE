package org.devkor.apu.saerok_server.domain.collection.core.repository.dto;

/**
 * 특정 bird에 몇 명이 동의했고, 내가 동의했는지 여부
 */
public record BirdIdSuggestionSummary(
        Long birdId,
        String birdKoreanName,
        String birdScientificName,
        String birdThumbImageObjectKey,
        Long agreeCount,
        Boolean isAgreedByMe
) {
}
