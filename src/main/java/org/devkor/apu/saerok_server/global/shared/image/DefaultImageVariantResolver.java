package org.devkor.apu.saerok_server.global.shared.image;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 기본 규칙 기반 variant resolver.
 * - 썸네일: "thumbnails/" + (확장자 제거한 원본) + ".webp"
 */
@Component
public class DefaultImageVariantResolver implements ImageVariantResolver {

    @Override
    public String thumbnailKeyOf(String originalKey) {
        if (originalKey == null || originalKey.isBlank()) return originalKey;
        String fileNameWithoutExt = originalKey.replaceFirst("\\.[^.]*$", "");
        return "thumbnails/" + fileNameWithoutExt + ".webp";
    }

    @Override
    public List<String> associatedKeysOf(String originalKey) {
        if (originalKey == null || originalKey.isBlank()) return List.of();
        List<String> out = new ArrayList<>(2);
        out.add(originalKey);
        out.add(thumbnailKeyOf(originalKey));
        // 중복 제거 보장
        return new ArrayList<>(new LinkedHashSet<>(out));
    }

    @Override
    public List<String> associatedKeysOf(Collection<String> originalKeys) {
        if (originalKeys == null || originalKeys.isEmpty()) return List.of();
        LinkedHashSet<String> acc = new LinkedHashSet<>();
        for (String k : originalKeys) {
            acc.addAll(associatedKeysOf(k));
        }
        return new ArrayList<>(acc);
    }
}
