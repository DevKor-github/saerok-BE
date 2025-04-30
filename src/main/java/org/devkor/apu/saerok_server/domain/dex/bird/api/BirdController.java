package org.devkor.apu.saerok_server.domain.dex.bird.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.dex.bird.application.BirdQueryService;
import org.devkor.apu.saerok_server.global.exception.ErrorResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "Birds API", description = "도감 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/birds")
public class BirdController {

    private final BirdQueryService birdQueryService;

    @GetMapping("/")
    @Operation(
            summary = "🛠 [미구현] 조류 목록 조회 및 검색",
            description = "도감에 등록된 조류 목록을 조회하거나, 필터 조건 및 키워드를 이용해 검색할 수 있습니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조류 목록 응답",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BirdListResponse.class)))
            )
    )
    public void getBirds(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(required = false) Integer size,
            @Parameter(description = "검색 키워드 (한글 이름)") @RequestParam(required = false) String q,
            @Parameter(description = "서식지 필터", example = "[\"FOREST\", \"WETLAND\"]") @RequestParam(required = false) List<String> habitat,
            @Parameter(description = "크기 필터", example = "[\"SMALL\", \"MEDIUM\"]") @RequestParam(required = false) List<String> bodySize,
            @Parameter(description = "계절 필터", example = "[\"SPRING\", \"SUMMER\"]") @RequestParam(required = false) List<String> season
    ) {
        // 미구현
    }

    @GetMapping("/{birdId}")
    @Operation(
            summary = "특정 조류 상세 조회",
            description = "birdId를 기반으로 해당 조류의 상세 정보를 조회합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조류 상세 응답",
                    content = @Content(schema = @Schema(implementation = BirdDetailResponse.class))
            )
    )
    public ResponseEntity<BirdDetailResponse> getBirdDetail(
            @Parameter(description = "조회할 조류의 ID", example = "1") @PathVariable Long birdId) {
        BirdDetailResponse response = birdQueryService.getBirdDetailResponse(birdId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/autocomplete")
    @Operation(
            summary = "🛠 [미구현] 조류 자동완성",
            description = "조류 이름 검색을 위한 자동완성 제안을 반환합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "자동완성 결과",
                    content = @Content(schema = @Schema(implementation = BirdAutocompleteResponse.class))
            )
    )
    public void getBirdAutocomplete(
            @Parameter(description = "검색 키워드 (한글 이름)") @RequestParam String q
    ) {
        // 미구현
    }

    @GetMapping("/full-sync")
    @Operation(
            summary = "조류 도감 전체 동기화 (App 전용)",
            description = "조류 도감 전체 데이터를 제공합니다. (App 전용)<br>" +
                    "[⚠️ 주의]️ 크기 카테고리 정보는 포함되어 있지 않습니다. GET /api/v1/birds/size-category-rules로 크기 카테고리 규칙을 다운로드받아 사용해야 합니다.",
            responses = {
                    @ApiResponse(
                    responseCode = "200",
                    description = "도감 전체 데이터",
                    content = @Content(schema = @Schema(implementation = BirdFullSyncResponse.class))
            ),
                    @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 (예: 도감이 비어 있는 경우)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ResponseEntity<BirdFullSyncResponse> getBirdsFullSync() {
        BirdFullSyncResponse response = birdQueryService.getBirdFullSyncResponse();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/size-category-rules")
    @Operation(
            summary = "🛠 [미구현] 조류 크기 카테고리 규칙 다운로드 (App 전용)",
            description = "조류 크기 카테고리 규칙을 제공합니다. (App 전용)",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조류 크기 카테고리 규칙",
                    content = @Content(schema = @Schema(implementation = BirdSizeCategoryRulesResponse.class))
            )
    )
    public void getSizeCategoryRules() {
        // 미구현
    }

    @GetMapping("/changes")
    @Operation(
            summary = "조류 도감 업데이트 동기화 (App 전용)",
            description = "기준 시각 이후로 추가/변경/삭제된 도감 데이터를 제공합니다. (App 전용)",
            responses = {
                    @ApiResponse(
                    responseCode = "200",
                    description = "기준 시각 이후 업데이트할 데이터가 있을 경우",
                    content = @Content(schema = @Schema(implementation = BirdChangesResponse.class))
            ),
                    @ApiResponse(
                    responseCode = "204",
                    description = "기준 시각 이후 업데이트할 데이터가 없을 경우",
                    content = @Content()
                    )
            }
    )
    public ResponseEntity<BirdChangesResponse> getChanges(
            @Parameter(description = "기준 시각 (날짜 + T + 시간 + 타임존 오프셋)" +
                    "<br>참고: 한국 시간 오프셋 +09:00",
                    example = "2025-05-01T15:30:00+09:00")
            @RequestParam
            OffsetDateTime since) {

        BirdChangesResponse response = birdQueryService.getBirdChangesResponse(since);
        if (response.hasNoChanges()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

}
