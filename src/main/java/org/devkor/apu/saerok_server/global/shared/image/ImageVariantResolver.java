package org.devkor.apu.saerok_server.global.shared.image;

import java.util.Collection;
import java.util.List;

/**
 * 업로드된 원본 이미지로부터 파생(variant) 리소스들의 objectKey를 결정한다.
 * 예) 썸네일 등
 */
public interface ImageVariantResolver {

    /**
     * 원본 objectKey로부터 썸네일 objectKey를 계산한다.
     * 규칙: 확장자 제거 + .webp, 그리고 앞에 "thumbnails/" 접두사 부여
     * ex) "collection-images/1/a.jpg" -> "thumbnails/collection-images/1/a.webp"
     */
    String thumbnailKeyOf(String originalKey);

    /**
     * 해당 원본과 그 파생 리소스(썸네일 등)를 모두 포함한 리스트를 반환한다.
     * 반환 리스트는 중복되지 않는다.
     */
    List<String> associatedKeysOf(String originalKey);

    /**
     * 여러 원본 키에 대해 연관 리소스 키를 모두 펼쳐(distinct) 반환한다.
     */
    List<String> associatedKeysOf(Collection<String> originalKeys);
}
