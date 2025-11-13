package org.devkor.apu.saerok_server.domain.ad.application;

import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;

public record AdSelectionResult(
        String type,       // "AD" or "FALLBACK"
        int ttlSeconds,
        Ad ad              // type == "AD"일 때만 값 존재
) {

    public static AdSelectionResult fallback(int ttlSeconds) {
        return new AdSelectionResult("FALLBACK", ttlSeconds, null);
    }

    public static AdSelectionResult ad(int ttlSeconds, Ad ad) {
        return new AdSelectionResult("AD", ttlSeconds, ad);
    }

    public boolean isFallback() {
        return "FALLBACK".equals(type);
    }
}
