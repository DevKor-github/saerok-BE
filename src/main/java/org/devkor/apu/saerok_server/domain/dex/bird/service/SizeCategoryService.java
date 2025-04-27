package org.devkor.apu.saerok_server.domain.dex.bird.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.contract.HasBodyLength;
import org.devkor.apu.saerok_server.domain.dex.bird.dto.service.SizeCategoryDto;
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
}
