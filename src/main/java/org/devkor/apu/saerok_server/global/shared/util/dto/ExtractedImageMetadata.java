package org.devkor.apu.saerok_server.global.shared.util.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 이미지에서 추출된 EXIF 메타데이터 정보
 */
@Schema(description = "이미지에서 추출된 메타데이터 정보")
public record ExtractedImageMetadata(
        
        @Schema(
                description = "이미지 EXIF에서 추출된 촬영 날짜",
                example = "2024-05-15",
                nullable = true
        )
        LocalDate extractedDate,
        
        @Schema(
                description = "이미지 EXIF에서 추출된 GPS 위도",
                example = "37.5665",
                nullable = true
        )
        Double extractedLatitude,
        
        @Schema(
                description = "이미지 EXIF에서 추출된 GPS 경도",
                example = "126.9780",
                nullable = true
        )
        Double extractedLongitude
        
) {
}
