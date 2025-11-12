package org.devkor.apu.saerok_server.global.shared.infra;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageDomainService {

    private final ImageVariantResolver imageVariantResolver;

    @Value("${aws.cloudfront.upload-image-domain}")
    private String uploadImageDomain;

    @Value("${aws.cloudfront.dex-image-domain}")
    private String dexImageDomain;

    /**
     * 사용자가 업로드한 이미지 전용 (컬렉션 이미지, 프로필 사진, ...) <br>
     * 현재 "도감 이미지"와 "사용자 업로드 이미지" 버킷을 나누어서 관리하고 있어서, toUploadImageUrl을 쓰면 "사용자 업로드 이미지 버킷"에 해당하는 CloudFront 도메인을 붙여줍니다.
     *
     * @param objectKey 해당 이미지의 objectKey
     * @return 해당 이미지에 접근할 수 있는 URL
     */
    public String toUploadImageUrl(String objectKey) {
        return uploadImageDomain + "/" + objectKey;
    }

    public String toThumbnailUrl(String objectKey) {
        String thumbKey = imageVariantResolver.thumbnailKeyOf(objectKey);
        return toUploadImageUrl(thumbKey);
    }

    public String toDexImageUrl(String objectKey) {
        return dexImageDomain + "/" + objectKey;
    }
}
