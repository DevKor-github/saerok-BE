package org.devkor.apu.saerok_server.global.shared.image;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 이미지 variant(썸네일 등) 키 파생 규칙을 제공하는 서비스.
 * 모든 연산은 반드시 ImageKind를 동반해야 한다.
 */
public interface ImageVariantService {

    /**
     * 주어진 원본 objectKey에 대한 썸네일 objectKey를 계산합니다.
     * 정책상 썸네일이 존재하지 않는 종류라면 Optional.empty()를 반환합니다.
     */
    Optional<String> thumbnailKey(ImageKind kind, String originalKey);

    /**
     * 원본 + 파생 키 세트를 반환합니다. 순서는 안정적이며 중복을 포함하지 않습니다.
     */
    List<String> associatedKeys(ImageKind kind, String originalKey);

    /**
     * 여러 원본 키에 대한 연관 키를 모두 펼쳐 반환합니다. 순서는 입력 순서를 보존합니다.
     */
    List<String> associatedKeys(ImageKind kind, Collection<String> originalKeys);
}
