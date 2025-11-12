package org.devkor.apu.saerok_server.global.shared.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * CDN 도메인과 objectKey를 합성하여 최종 접근 URL을 만들어주는 서비스.
 * - 이 클래스는 오직 "도메인 + objectKey → URL" 합성만을 책임집니다.
 */
@Service
public class ImageDomainService {

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
     * 도감(Bird Dex) 이미지의 URL을 반환합니다.
     */
    public String toDexImageUrl(String objectKey) {
        return dexImageDomain + "/" + objectKey;
    }
}
