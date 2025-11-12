package org.devkor.apu.saerok_server.global.shared.infra;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * CDN 도메인과 objectKey를 합성하여 최종 접근 URL을 만들어주는 서비스.
 * - 썸네일 URL 계산은 반드시 ImageKind를 명시해야 한다.
 */
@Service
@RequiredArgsConstructor
public class ImageDomainService {

    private final ImageVariantService imageVariantService;

    @Value("${aws.cloudfront.upload-image-domain}")
    private String uploadImageDomain;

    @Value("${aws.cloudfront.dex-image-domain}")
    private String dexImageDomain;

    /**
     * 업로드된 '원본' 이미지의 URL을 반환합니다.
     * @param objectKey S3 object key
     */
    public String toUploadImageUrl(String objectKey) {
        return uploadImageDomain + "/" + objectKey;
    }

    /**
     * 이미지 종류를 명시하여 썸네일 URL을 계산합니다.
     * 종류별 정책에 따라 썸네일이 없을 수 있으며, 그 경우 IllegalArgumentException을 발생시킵니다.
     */
    public String toThumbnailUrl(ImageKind kind, String objectKey) {
        String thumbKey = imageVariantService.thumbnailKey(kind, objectKey)
                .orElseThrow(() -> new IllegalArgumentException("해당 이미지 종류(" + kind + ")는 썸네일을 지원하지 않아요."));
        return toUploadImageUrl(thumbKey);
    }

    /**
     * 도감(Bird Dex) 이미지의 URL을 반환합니다.
     */
    public String toDexImageUrl(String objectKey) {
        return dexImageDomain + "/" + objectKey;
    }
}
