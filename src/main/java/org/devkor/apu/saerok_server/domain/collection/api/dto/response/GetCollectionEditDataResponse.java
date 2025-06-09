package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;

import java.time.LocalDate;

@Data
@Schema(description = "컬렉션 수정용 상세 조회 응답 DTO")
public class GetCollectionEditDataResponse {

    @Schema(description = "관찰한 새의 ID", example = "101")
    private Long birdId;

    @Schema(description = "관찰 날짜", example = "2024-05-21")
    private LocalDate discoveredDate;

    @Schema(description = "관찰 위치 경도", example = "127.123456")
    private double longitude;

    @Schema(description = "관찰 위치 위도", example = "37.987654")
    private double latitude;

    @Schema(description = "관찰 위치 별칭", example = "서울숲")
    private String locationAlias;

    @Schema(description = "관찰 지점의 주소", example = "서울시 성동구 성수동")
    private String address;

    @Schema(description = "한 줄 평", example = "까치가 무리를 지어 날아다님")
    private String note;

    @Schema(description = "컬렉션 공개 범위 (공개/비공개)")
    private AccessLevelType accessLevel;

    @Schema(description = "이미지 ID", example = "300")
    private Long imageId;

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/images/300.jpg")
    private String imageUrl;
}