package org.devkor.apu.saerok_server.domain.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdPlacement;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdPlacementRepository;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdRepository;
import org.devkor.apu.saerok_server.domain.ad.core.repository.SlotRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAdPlacementService {

    private final AdPlacementRepository adPlacementRepository;
    private final AdRepository adRepository;
    private final SlotRepository slotRepository;

    @Transactional(readOnly = true)
    public List<AdPlacement> listPlacements() {
        return adPlacementRepository.findAll();
    }

    public AdPlacement createPlacement(Long adId,
                                       Long slotId,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       Short weight,
                                       Boolean enabled) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 id예요."));
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 슬롯 id예요."));
        AdPlacement placement = AdPlacement.create(ad, slot, startDate, endDate, weight, enabled);
        return adPlacementRepository.save(placement);
    }

    public AdPlacement updatePlacement(Long id,
                                       Long slotId,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       Short weight,
                                       Boolean enabled) {
        AdPlacement placement = adPlacementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 배치 id예요."));

        Slot slot = null;
        if (slotId != null) {
            slot = slotRepository.findById(slotId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 슬롯 id예요."));
        }

        placement.update(slot, startDate, endDate, weight, enabled);
        return placement;
    }

    public void deletePlacement(Long id) {
        AdPlacement placement = adPlacementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 배치 id예요."));
        adPlacementRepository.delete(placement);
    }
}
