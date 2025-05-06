package org.devkor.apu.saerok_server.domain.dex.bird.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SizeCategoryDto {
    private String code; // ex: "small"
    private String label; // ex: "참새 크기"

    public static SizeCategoryDto Empty() {
        return new SizeCategoryDto(null, null);
    }
}
