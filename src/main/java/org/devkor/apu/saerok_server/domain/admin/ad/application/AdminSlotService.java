package org.devkor.apu.saerok_server.domain.admin.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.devkor.apu.saerok_server.domain.ad.core.repository.SlotRepository;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditTargetType;
import org.devkor.apu.saerok_server.domain.admin.audit.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminSlotService {

    private final SlotRepository slotRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public Slot createSlot(Long adminUserId, String name, String memo, Double fallbackRatio, Integer ttlSeconds) {
        Slot slot = Slot.create(name, memo, fallbackRatio, ttlSeconds);
        Slot saved = slotRepository.save(slot);

        User admin = loadAdmin(adminUserId);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", saved.getName());
        metadata.put("memo", saved.getMemo());
        metadata.put("fallbackRatio", saved.getFallbackRatio());
        metadata.put("ttlSeconds", saved.getTtlSeconds());

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.SLOT_CREATED,
                AdminAuditTargetType.SLOT,
                saved.getId(),
                null,
                metadata
        ));

        return saved;
    }

    @Transactional
    public Slot updateSlot(Long adminUserId, Long id, String memo, Double fallbackRatio, Integer ttlSeconds) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 슬롯이 존재하지 않아요."));
        String previousMemo = slot.getMemo();
        Double previousFallbackRatio = slot.getFallbackRatio();
        Integer previousTtlSeconds = slot.getTtlSeconds();
        slot.update(memo, fallbackRatio, ttlSeconds);

        User admin = loadAdmin(adminUserId);
        Map<String, Object> previous = new LinkedHashMap<>();
        previous.put("memo", previousMemo);
        previous.put("fallbackRatio", previousFallbackRatio);
        previous.put("ttlSeconds", previousTtlSeconds);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", slot.getName());
        metadata.put("memo", slot.getMemo());
        metadata.put("fallbackRatio", slot.getFallbackRatio());
        metadata.put("ttlSeconds", slot.getTtlSeconds());
        metadata.put("previous", previous);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.SLOT_UPDATED,
                AdminAuditTargetType.SLOT,
                slot.getId(),
                null,
                metadata
        ));
        return slot;
    }

    @Transactional
    public void deleteSlot(Long adminUserId, Long id) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 슬롯이 존재하지 않아요."));
        // 슬롯 삭제 시 관련된 광고 배치도 함께 삭제됨 (DELETE ON CASCADE)
        String name = slot.getName();
        String memo = slot.getMemo();
        Double fallbackRatio = slot.getFallbackRatio();
        Integer ttlSeconds = slot.getTtlSeconds();
        slotRepository.delete(slot);

        User admin = loadAdmin(adminUserId);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", name);
        metadata.put("memo", memo);
        metadata.put("fallbackRatio", fallbackRatio);
        metadata.put("ttlSeconds", ttlSeconds);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.SLOT_DELETED,
                AdminAuditTargetType.SLOT,
                id,
                null,
                metadata
        ));
    }

    @Transactional(readOnly = true)
    public List<Slot> listSlots() {
        return slotRepository.findAll();
    }

    private User loadAdmin(Long adminUserId) {
        return userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));
    }
}
