package org.devkor.apu.saerok_server.domain.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdPlacement;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdPlacementRepository;
import org.devkor.apu.saerok_server.domain.ad.core.repository.SlotRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdSelectorService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SlotRepository slotRepository;
    private final AdPlacementRepository adPlacementRepository;

    public AdSelectionResult selectAdForSlot(String slotName) {
        Slot slot = slotRepository.findByName(slotName)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 슬롯 이름이에요."));

        int ttlSeconds = slot.getTtlSeconds() != null ? slot.getTtlSeconds() : 0;

        if (shouldFallback(slot.getFallbackRatio())) {
            return AdSelectionResult.fallback(ttlSeconds);
        }

        LocalDate today = LocalDate.now(KST);
        List<AdPlacement> placements = adPlacementRepository.findActivePlacements(slot.getId(), today);

        if (placements.isEmpty()) {
            return AdSelectionResult.fallback(ttlSeconds);
        }

        AdPlacement selected = selectByWeight(placements);
        return AdSelectionResult.ad(ttlSeconds, selected.getAd());
    }

    private boolean shouldFallback(Double fallbackRatio) {
        if (fallbackRatio == null) {
            return false;
        }
        if (fallbackRatio <= 0.0) {
            return false;
        }
        if (fallbackRatio >= 1.0) {
            return true;
        }
        double r = ThreadLocalRandom.current().nextDouble(); // [0,1)
        return r < fallbackRatio;
    }

    private AdPlacement selectByWeight(List<AdPlacement> placements) {
        int totalWeight = placements.stream()
                .map(AdPlacement::getWeight)
                .mapToInt(Short::intValue)
                .sum();

        int r = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;

        for (AdPlacement p : placements) {
            cumulative += p.getWeight();
            if (r < cumulative) {
                return p;
            }
        }
        // 이론상 도달하지 않지만, 방어적으로 마지막 요소 반환
        return placements.getLast();
    }
}
