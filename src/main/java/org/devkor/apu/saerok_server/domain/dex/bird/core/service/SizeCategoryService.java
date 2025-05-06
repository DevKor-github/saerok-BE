package org.devkor.apu.saerok_server.domain.dex.bird.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.contract.HasBodyLength;
import org.devkor.apu.saerok_server.domain.dex.bird.core.dto.SizeCategoryDto;
import org.devkor.apu.saerok_server.global.config.SizeCategoryRulesConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SizeCategoryService {

    private final SizeCategoryRulesConfig rules;

    public SizeCategoryDto getSizeCategory(HasBodyLength item) {
        Double bodyLengthCm = item.getBodyLengthCm();

        if (bodyLengthCm == null) {
            return SizeCategoryDto.Empty();
        }

        for (SizeCategoryRulesConfig.Boundary boundary : rules.getBoundaries()) {
            if (boundary.getMaxCm() == null || bodyLengthCm <= boundary.getMaxCm()) {
                String category = boundary.getCategory();
                return new SizeCategoryDto(category, rules.getLabels().get(category));
            }
        }

        return SizeCategoryDto.Empty();
    }

    /**
     * 최소 길이(cm)를 반환한다.
     * size_category_rules.yml의 순서를 그대로 신뢰한다.
     * 매칭되는 category가 없으면 IllegalArgumentException.
     */
    public Double getMinCmFromCategory(String category) {

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("크기 카테고리 값이 비어있습니다.");
        }

        Double prevMax = 0.0;

        for (SizeCategoryRulesConfig.Boundary boundary : rules.getBoundaries()) {

            if (boundary.getCategory().equals(category.toLowerCase())) {
                return prevMax;
            }

            if (boundary.getMaxCm() != null) {
                prevMax = boundary.getMaxCm();
            }
        }

        throw new IllegalArgumentException("유효하지 않은 크기 카테고리: " + category);
    }

    /**
     * 최대 길이(cm)를 반환한다.
     * size_category_rules.yml의 순서를 그대로 신뢰한다.
     * 매칭되는 category가 없으면 IllegalArgumentException.
     */
    public Double getMaxCmFromCategory(String category) {

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("크기 카테고리 값이 비어있습니다.");
        }

        for (SizeCategoryRulesConfig.Boundary boundary : rules.getBoundaries()) {
            if (boundary.getCategory().equals(category.toLowerCase())) {
                return boundary.getMaxCm();
            }
        }

        throw new IllegalArgumentException("유효하지 않은 크기 카테고리: " + category);
    }
}
