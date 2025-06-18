package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;

import java.time.LocalDate;

public record UpdateCollectionResponse(
        @Schema(description = "컬렉션 ID", example = "1")
        Long collectionId,

        @Schema(description = "관찰한 새의 ID", example = "10", nullable = true)
        Long birdId,

        @Schema(description = "관찰한 날짜 (yyyy-MM-dd 형식)", example = "2024-05-15")
        LocalDate discoveredDate,

        @Schema(description = "경도", example = "126.5583")
        Double longitude,

        @Schema(description = "위도", example = "33.2395")
        Double latitude,

        @Schema(description = "주소", example = "제주특별자치도 서귀포시 안덕면 사계리", nullable = true)
        String address,

        @Schema(description = "사용자 지정 장소 별칭", example = "서귀포 갯벌", nullable = true)
        String locationAlias,

        @Schema(description = "한 줄 평", example = "까치가 엄청 날아다녔다", nullable = true)
        String note,

        @Schema(description = "이미지 URL", example = "https://cdn.example.com/collection/1.jpg", nullable = true)
        String imageUrl,

        @Schema(description = "공개/비공개 여부", example = "PUBLIC", nullable = true)
        AccessLevelType accessLevel
) {}
