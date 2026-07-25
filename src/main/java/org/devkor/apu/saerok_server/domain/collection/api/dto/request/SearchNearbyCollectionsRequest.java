package org.devkor.apu.saerok_server.domain.collection.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "새 이름으로 주위 컬렉션을 검색하는 요청 DTO")
public class SearchNearbyCollectionsRequest {

    @NotNull(message = "위도를 입력해주세요.")
    @Schema(description = "검색 중심 위도", example = "37.5665", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double latitude;

    @NotNull(message = "경도를 입력해주세요.")
    @Schema(description = "검색 중심 경도", example = "126.9780", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double longitude;

    @NotBlank(message = "query를 입력해주세요.")
    @Schema(description = "새 이름 검색어", example = "까치", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    @NotNull(message = "radiusMeters를 입력해주세요.")
    @Positive(message = "radiusMeters는 0보다 커야 해요.")
    @Schema(description = "최초 검색 반경 (m). 생략 시 5km", example = "5000")
    private Double radiusMeters = 5_000D;

    @Positive(message = "limit은 1 이상의 양수여야 해요.")
    @Max(value = 60, message = "limit은 60 이하여야 해요.")
    @Schema(description = "최초 검색 결과 최대 개수 (생략 시 60, 최대 60). 검색 반경이 늘어날수록 반환 개수도 줄어듭니다.", example = "60")
    private Integer limit;
}
