package org.devkor.apu.saerok_server.global.shared.infra;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.springframework.stereotype.Service;

/**
 * ImageKind → 적절한 도메인으로 라우팅을 담당.
 * - 절대 키 파생(썸네일 계산)을 하지 않습니다.
 * - 오직 "어떤 도메인으로 보낼지"만 결정합니다.
 */
@Service
@RequiredArgsConstructor
public class ImageDomainRouter {

    private final ImageDomainService imageDomainService;

    /**
     * 주어진 kind, objectKey를 해당 도메인의 최종 URL로 변환합니다.
     */
    public String toUrlFor(ImageKind kind, String objectKey) {
        return switch (kind) {
            case USER_COLLECTION_IMAGE, USER_PROFILE_IMAGE -> imageDomainService.toUploadImageUrl(objectKey);
            case DEX_BIRD_IMAGE -> imageDomainService.toDexImageUrl(objectKey);
        };
    }
}
