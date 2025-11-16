package org.devkor.apu.saerok_server.domain.admin.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdPlacement;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdPlacementRepository;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAdPlacementService {

    private final AdPlacementRepository adPlacementRepository;
    private final AdRepository adRepository;
    private final SlotRepository slotRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AdPlacement> listPlacements() {
        return adPlacementRepository.findAll();
    }

    public AdPlacement createPlacement(Long adminUserId,
                                       Long adId,
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
        AdPlacement saved = adPlacementRepository.save(placement);

        User admin = loadAdmin(adminUserId);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("adId", saved.getAd().getId());
        metadata.put("slotId", saved.getSlot().getId());
        metadata.put("startDate", saved.getStartDate());
        metadata.put("endDate", saved.getEndDate());
        metadata.put("weight", saved.getWeight());
        metadata.put("enabled", saved.getEnabled());

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.AD_PLACEMENT_CREATED,
                AdminAuditTargetType.AD_PLACEMENT,
                saved.getId(),
                null,
                metadata
        ));

        return saved;
    }

    public AdPlacement updatePlacement(Long adminUserId,
                                       Long id,
                                       Long slotId,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       Short weight,
                                       Boolean enabled) {
        AdPlacement placement = adPlacementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 배치 id예요."));

        Slot slot = null;
        Long previousSlotId = placement.getSlot() != null ? placement.getSlot().getId() : null;
        LocalDate previousStartDate = placement.getStartDate();
        LocalDate previousEndDate = placement.getEndDate();
        Short previousWeight = placement.getWeight();
        Boolean previousEnabled = placement.getEnabled();
        if (slotId != null) {
            slot = slotRepository.findById(slotId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 슬롯 id예요."));
        }

        placement.update(slot, startDate, endDate, weight, enabled);

        User admin = loadAdmin(adminUserId);
        Map<String, Object> previous = new LinkedHashMap<>();
        previous.put("slotId", previousSlotId);
        previous.put("startDate", previousStartDate);
        previous.put("endDate", previousEndDate);
        previous.put("weight", previousWeight);
        previous.put("enabled", previousEnabled);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("adId", placement.getAd().getId());
        metadata.put("slotId", placement.getSlot().getId());
        metadata.put("startDate", placement.getStartDate());
        metadata.put("endDate", placement.getEndDate());
        metadata.put("weight", placement.getWeight());
        metadata.put("enabled", placement.getEnabled());
        metadata.put("previous", previous);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.AD_PLACEMENT_UPDATED,
                AdminAuditTargetType.AD_PLACEMENT,
                placement.getId(),
                null,
                metadata
        ));
        return placement;
    }

    public void deletePlacement(Long adminUserId, Long id) {
        AdPlacement placement = adPlacementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 배치 id예요."));
        Long adId = placement.getAd().getId();
        Long slotId = placement.getSlot().getId();
        LocalDate startDate = placement.getStartDate();
        LocalDate endDate = placement.getEndDate();
        Short weight = placement.getWeight();
        Boolean enabled = placement.getEnabled();
        adPlacementRepository.delete(placement);

        User admin = loadAdmin(adminUserId);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("adId", adId);
        metadata.put("slotId", slotId);
        metadata.put("startDate", startDate);
        metadata.put("endDate", endDate);
        metadata.put("weight", weight);
        metadata.put("enabled", enabled);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.AD_PLACEMENT_DELETED,
                AdminAuditTargetType.AD_PLACEMENT,
                id,
                null,
                metadata
        ));
    }

    private User loadAdmin(Long adminUserId) {
        return userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));
    }
}
