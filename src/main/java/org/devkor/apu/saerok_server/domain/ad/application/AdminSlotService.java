package org.devkor.apu.saerok_server.domain.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.devkor.apu.saerok_server.domain.ad.core.repository.SlotRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminSlotService {

    private final SlotRepository slotRepository;

    @Transactional
    public Slot createSlot(String name, String memo, Double fallbackRatio, Integer ttlSeconds) {
        Slot slot = Slot.create(name, memo, fallbackRatio, ttlSeconds);
        return slotRepository.save(slot);
    }

    @Transactional
    public Slot updateSlot(Long id, String memo, Double fallbackRatio, Integer ttlSeconds) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 슬롯이 존재하지 않아요."));
        slot.update(memo, fallbackRatio, ttlSeconds);
        return slot;
    }

    @Transactional
    public void deleteSlot(Long id) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 슬롯이 존재하지 않아요."));
        // 슬롯 삭제 시 관련된 광고 배치도 함께 삭제됨 (DELETE ON CASCADE)
        slotRepository.delete(slot);
    }

    @Transactional(readOnly = true)
    public List<Slot> listSlots() {
        return slotRepository.findAll();
    }
}
