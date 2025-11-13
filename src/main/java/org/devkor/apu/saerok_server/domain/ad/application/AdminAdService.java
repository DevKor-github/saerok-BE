package org.devkor.apu.saerok_server.domain.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdImagePresignResponse;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminAdService {

    private final AdRepository adRepository;
    private final ImageService imageService;
    private final ImageVariantService imageVariantService;

    @Transactional
    public Ad createAd(String name,
                       String memo,
                       String objectKey,
                       String contentType,
                       String targetUrl) {

        Ad ad = Ad.create(name, memo, objectKey, contentType, targetUrl);
        return adRepository.save(ad);
    }

    @Transactional
    public Ad updateAd(Long id,
                       String name,
                       String memo,
                       String objectKey,
                       String contentType,
                       String targetUrl) {

        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 광고가 존재하지 않아요."));

        String oldObjectKey = ad.getObjectKey();

        ad.update(name, memo, objectKey, contentType, targetUrl);

        // 이미지가 교체된 경우 이전 이미지 S3 삭제
        if (oldObjectKey != null && !oldObjectKey.equals(objectKey)) {
            String keyToDelete = oldObjectKey;
            runAfterCommitOrNow(() -> {
                var keys = imageVariantService.associatedKeys(ImageKind.AD_IMAGE, keyToDelete);
                imageService.deleteAll(keys);
            });
        }

        return ad;
    }

    @Transactional
    public void deleteAd(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 광고가 존재하지 않아요."));

        String objectKey = ad.getObjectKey();
        adRepository.delete(ad);

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
}
