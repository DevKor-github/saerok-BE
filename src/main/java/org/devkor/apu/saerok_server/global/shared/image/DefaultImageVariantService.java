package org.devkor.apu.saerok_server.global.shared.image;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.global.core.config.feature.ImageVariantsConfig;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * YAML 기반 변환 규칙을 적용하는 기본 구현.
 * - 정책이 없는 경우(또는 로드 실패)는 안전 기본값을 따릅니다:
 *   USER_COLLECTION_IMAGE, USER_PROFILE_IMAGE -> THUMBNAIL(webp, thumbnails/)
 *   DEX_BIRD_IMAGE -> variant 없음
 */
@Service
@RequiredArgsConstructor
public class DefaultImageVariantService implements ImageVariantService {

    private final ImageVariantsConfig config;

    @Override
    public Optional<String> thumbnailKey(ImageKind kind, String originalKey) {
        ImageVariantsConfig.Variant rule = findVariant(kind, "THUMBNAIL").orElse(null);
        if (rule == null) return Optional.empty();
        return Optional.of(apply(rule, originalKey));
    }

    @Override
    public List<String> associatedKeys(ImageKind kind, String originalKey) {
        if (originalKey == null || originalKey.isBlank()) return List.of();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        out.add(originalKey);
        findVariant(kind, "THUMBNAIL").ifPresent(v -> out.add(apply(v, originalKey)));
        return new ArrayList<>(out);
    }

    @Override
    public List<String> associatedKeys(ImageKind kind, Collection<String> originalKeys) {
        if (originalKeys == null || originalKeys.isEmpty()) return List.of();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String k : originalKeys) {
            out.addAll(associatedKeys(kind, k));
        }
        return new ArrayList<>(out);
    }

    /* ---------- helpers ---------- */

    private Optional<ImageVariantsConfig.Variant> findVariant(ImageKind kind, String name) {
        // 1) yml에서 우선 조회
        ImageVariantsConfig.Kind k = config.getKinds() != null ? config.getKinds().get(kind.name()) : null;
        if (k != null && k.getVariants() != null) {
            for (ImageVariantsConfig.Variant v : k.getVariants()) {
                if (name.equalsIgnoreCase(v.getName())) return Optional.of(v);
            }
        }
        // 2) 없으면 안전 기본값
        if (kind == ImageKind.USER_COLLECTION_IMAGE || kind == ImageKind.USER_PROFILE_IMAGE) {
            ImageVariantsConfig.Variant def = new ImageVariantsConfig.Variant();
            def.setName("THUMBNAIL");
            def.setStrategy("REPLACE_EXTENSION");
            def.setTargetExtension("webp");
            def.setPrefix("thumbnails/");
            return Optional.of(def);
        }
        return Optional.empty(); // DEX 등은 기본적으로 없음
    }

    private String apply(ImageVariantsConfig.Variant rule, String originalKey) {
        String strategy = rule.getStrategy() != null ? rule.getStrategy().toUpperCase(Locale.ROOT) : "";
        switch (strategy) {
            case "REPLACE_EXTENSION":
            case "THUMBNAIL_WEBP": // 과거 명칭 호환
                String base = stripExtension(originalKey);
                String ext = rule.getTargetExtension() != null ? rule.getTargetExtension() : "webp";
                String pref = rule.getPrefix() != null ? rule.getPrefix() : "";
                return pref + base + "." + ext;
            default:
                // 알 수 없는 전략은 보수적으로 원본 반환(썸네일 미지원 취급)
                return originalKey;
        }
    }

    private String stripExtension(String key) {
        if (key == null) return null;
        int slash = key.lastIndexOf('/');
        int dot = key.lastIndexOf('.');
        if (dot > slash) {
            return key.substring(0, dot);
        }
        return key; // 확장자 없으면 그대로
    }
}
