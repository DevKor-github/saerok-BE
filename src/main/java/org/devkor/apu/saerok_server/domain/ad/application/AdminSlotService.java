package org.devkor.apu.saerok_server.domain.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.devkor.apu.saerok_server.domain.ad.core.repository.SlotRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSlotService {

    private final SlotRepository slotRepository;

    @Transactional(readOnly = true)
    public List<Slot> listSlots() {
        return slotRepository.findAll();
    }

    public Slot createSlot(String name,
                           Double fallbackRatio,
                           Integer ttlSeconds) {
        Slot slot = Slot.create(name, fallbackRatio, ttlSeconds);
        return slotRepository.save(slot);
    }

    public Slot updateSlot(Long id,
                           Double fallbackRatio,
                           Integer ttlSeconds) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 슬롯 id예요."));
        slot.update(fallbackRatio, ttlSeconds);
        return slot;
    }
}
