package org.devkor.apu.saerok_server.domain.collection.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "컬렉션(관찰 기록) 수정 요청 DTO")
@Data
@NoArgsConstructor
public class UpdateCollectionRequest {

    @Schema(description = "birdId를 수정할 것인지 여부", example = "true")
    private Boolean isBirdIdUpdated;

    @Schema(description = "관찰한 새의 ID", example = "null", nullable = true)
    private Long birdId;

    @Schema(description = "관찰한 날짜 (yyyy-MM-dd 형식)", example = "2024-05-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate discoveredDate;

    @Schema(description = "관찰 지점의 경도", example = "126.5583", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double longitude;

    @Schema(description = "관찰 지점의 위도", example = "33.2395", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double latitude;

    @Schema(description = "관찰 지점에 대한 사용자 지정 장소 별칭", example = "서귀포 갯벌", nullable = true)
    private String locationAlias;

    @Schema(description = "관찰 기록에 대한 간단한 메모", example = "까치가 엄청 날아다녔다", nullable = true)
    private String note;
}
