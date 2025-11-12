package org.devkor.apu.saerok_server.global.shared.image;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @deprecated 정책 기반 ImageVariantService가 도입되었습니다.
 * 기존 호출부 호환을 위해 남겨진 어댑터 구현입니다.
 * - objectKey의 prefix로 ImageKind를 추론합니다.
 *   * user-profile-images/ -> USER_PROFILE_IMAGE
 *   * collection-images/   -> USER_COLLECTION_IMAGE
 *   * 그 외                 -> variant 없음(원본만)
 */
@Deprecated
@Component
@RequiredArgsConstructor
public class DefaultImageVariantResolver implements ImageVariantResolver {

    private final ImageVariantService imageVariantService;

    private static final String PROFILE_PREFIX = "user-profile-images/";
    private static final String COLLECTION_PREFIX = "collection-images/";

    private Optional<ImageKind> inferKind(String originalKey) {
        if (originalKey == null) return Optional.empty();
        if (originalKey.startsWith(PROFILE_PREFIX)) return Optional.of(ImageKind.USER_PROFILE_IMAGE);
        if (originalKey.startsWith(COLLECTION_PREFIX)) return Optional.of(ImageKind.USER_COLLECTION_IMAGE);
        return Optional.empty();
    }

    @Override
    public String thumbnailKeyOf(String originalKey) {
        ImageKind kind = inferKind(originalKey).orElseThrow(() ->
                new IllegalArgumentException("썸네일 키는 업로드 이미지(프로필/새록)에서만 지원돼요: " + originalKey));
        return imageVariantService.thumbnailKey(kind, originalKey)
                .orElseThrow(() -> new IllegalStateException("해당 종류는 썸네일이 비활성화되어 있어요: " + kind));
    }

    @Override
    public List<String> associatedKeysOf(String originalKey) {
        return inferKind(originalKey)
                .map(kind -> imageVariantService.associatedKeys(kind, originalKey))
                .orElseGet(() -> List.of(originalKey)); // 도감 등은 원본만
    }

    @Override
    public List<String> associatedKeysOf(Collection<String> originalKeys) {
        if (originalKeys == null || originalKeys.isEmpty()) return List.of();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String k : originalKeys) {
            out.addAll(associatedKeysOf(k));
        }
        return new ArrayList<>(out);
    }
}
