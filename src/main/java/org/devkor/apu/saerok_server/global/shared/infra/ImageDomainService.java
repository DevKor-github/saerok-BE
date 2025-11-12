package org.devkor.apu.saerok_server.global.shared.infra;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * CDN 도메인과 objectKey를 합성하여 최종 접근 URL을 만들어주는 서비스.
 * - 업로드 이미지(프로필/새록) 썸네일 URL을 계산할 때는 ImageKind 정책을 따릅니다.
 */
@Service
@RequiredArgsConstructor
public class ImageDomainService {

    private final ImageVariantService imageVariantService;

    @Value("${aws.cloudfront.upload-image-domain}")
    private String uploadImageDomain;

    @Value("${aws.cloudfront.dex-image-domain}")
    private String dexImageDomain;

    private static final String PROFILE_PREFIX = "user-profile-images/";
    private static final String COLLECTION_PREFIX = "collection-images/";

    /**
     * 업로드된 원본 이미지의 URL을 반환합니다.
     * @param objectKey S3 object key
     */
    public String toUploadImageUrl(String objectKey) {
        return uploadImageDomain + "/" + objectKey;
    }

    /**
     * (권장) 이미지 종류를 명시하여 썸네일 URL을 계산합니다.
     * 종류별 정책에 따라 썸네일이 없을 수 있으며, 그 경우 IllegalArgumentException을 발생시킵니다.
     */
    public String toThumbnailUrl(ImageKind kind, String objectKey) {
        String thumbKey = imageVariantService.thumbnailKey(kind, objectKey)
                .orElseThrow(() -> new IllegalArgumentException("해당 이미지 종류(" + kind + ")는 썸네일을 지원하지 않아요."));
        return toUploadImageUrl(thumbKey);
    }

    /**
     * (레거시) 업로드 이미지 전용 썸네일 URL.
     * - key prefix를 통해 종류를 추론합니다.
     * - 프로필/새록이 아니면 IllegalArgumentException을 발생시켜 오용을 방지합니다.
     */
    public String toThumbnailUrl(String objectKey) {
        ImageKind kind = inferKindOrThrow(objectKey);
        return toThumbnailUrl(kind, objectKey);
    }

    public String toDexImageUrl(String objectKey) {
        return dexImageDomain + "/" + objectKey;
    }

    /* ---------- helpers ---------- */

    private ImageKind inferKindOrThrow(String objectKey) {
        if (objectKey == null) throw new IllegalArgumentException("objectKey가 비어 있어요.");
        if (objectKey.startsWith(PROFILE_PREFIX)) return ImageKind.USER_PROFILE_IMAGE;
        if (objectKey.startsWith(COLLECTION_PREFIX)) return ImageKind.USER_COLLECTION_IMAGE;
        throw new IllegalArgumentException("썸네일 URL은 업로드 이미지(프로필/새록)에서만 사용할 수 있어요: " + objectKey);
    }
}
