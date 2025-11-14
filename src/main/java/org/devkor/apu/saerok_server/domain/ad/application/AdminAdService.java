package org.devkor.apu.saerok_server.domain.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdImagePresignResponse;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdRepository;
import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditTargetType;
import org.devkor.apu.saerok_server.domain.admin.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminAdService {

    private final AdRepository adRepository;
    private final ImageService imageService;
    private final ImageVariantService imageVariantService;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public Ad createAd(Long adminUserId,
                       String name,
                       String memo,
                       String objectKey,
                       String contentType,
                       String targetUrl) {

        Ad ad = Ad.create(name, memo, objectKey, contentType, targetUrl);
        Ad saved = adRepository.save(ad);

        User admin = loadAdmin(adminUserId);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", saved.getName());
        metadata.put("memo", saved.getMemo());
        metadata.put("targetUrl", saved.getTargetUrl());

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.AD_CREATED,
                AdminAuditTargetType.AD,
                saved.getId(),
                null,
                metadata
        ));

        return saved;
    }

    @Transactional
    public Ad updateAd(Long adminUserId,
                       Long id,
                       String name,
                       String memo,
                       String objectKey,
                       String contentType,
                       String targetUrl) {

        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 광고가 존재하지 않아요."));

        String oldObjectKey = ad.getObjectKey();
        String previousName = ad.getName();
        String previousMemo = ad.getMemo();
        String previousTargetUrl = ad.getTargetUrl();

        ad.update(name, memo, objectKey, contentType, targetUrl);

        // 이미지가 교체된 경우 이전 이미지 S3 삭제
        if (oldObjectKey != null && !oldObjectKey.equals(objectKey)) {
            String keyToDelete = oldObjectKey;
            runAfterCommitOrNow(() -> {
                var keys = imageVariantService.associatedKeys(ImageKind.AD_IMAGE, keyToDelete);
                imageService.deleteAll(keys);
            });
        }

        User admin = loadAdmin(adminUserId);
        Map<String, Object> previous = new LinkedHashMap<>();
        previous.put("name", previousName);
        previous.put("memo", previousMemo);
        previous.put("targetUrl", previousTargetUrl);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", ad.getName());
        metadata.put("memo", ad.getMemo());
        metadata.put("targetUrl", ad.getTargetUrl());
        metadata.put("previous", previous);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.AD_UPDATED,
                AdminAuditTargetType.AD,
                ad.getId(),
                null,
                metadata
        ));

        return ad;
    }

    @Transactional
    public void deleteAd(Long adminUserId, Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 광고가 존재하지 않아요."));

        String objectKey = ad.getObjectKey();
        String name = ad.getName();
        String memo = ad.getMemo();
        String targetUrl = ad.getTargetUrl();
        adRepository.delete(ad);

        User admin = loadAdmin(adminUserId);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", name);
        metadata.put("memo", memo);
        metadata.put("targetUrl", targetUrl);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.AD_DELETED,
                AdminAuditTargetType.AD,
                id,
                null,
                metadata
        ));

        if (objectKey != null && !objectKey.isBlank()) {
            runAfterCommitOrNow(() -> {
                var keys = imageVariantService.associatedKeys(ImageKind.AD_IMAGE, objectKey);
                imageService.deleteAll(keys);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<Ad> listAds() {
        return adRepository.findAll();
    }

    /**
     * 광고 배너 업로드를 위한 Presigned URL 발급
     */
    @Transactional
    public AdImagePresignResponse generateAdImagePresignUrl(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new BadRequestException("contentType 누락입니다.");
        }

        String fileName = UUID.randomUUID().toString();
        String objectKey = "ad-images/" + fileName;

        String uploadUrl = imageService.generateUploadUrl(objectKey, contentType, 10);

        return new AdImagePresignResponse(
                uploadUrl,
                objectKey
        );
    }

    private User loadAdmin(Long adminUserId) {
        return userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));
    }
}
