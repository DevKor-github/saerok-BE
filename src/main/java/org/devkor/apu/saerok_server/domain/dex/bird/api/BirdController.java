package org.devkor.apu.saerok_server.domain.dex.bird.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.dex.bird.application.BirdQueryService;
import org.devkor.apu.saerok_server.domain.dex.bird.application.dto.BirdSearchCommand;
import org.devkor.apu.saerok_server.global.exception.ErrorResponse;
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
            summary = "도감 조회/검색",
            description = """
            도감에 등록된 조류 목록을 조회하거나, 검색 키워드 및 다양한 필터를 조합해 검색할 수 있습니다.
            
            현재는 가나다순 정렬로 제공합니다. 필요에 따라 업데이트할 예정입니다.
            
            ✅ **일반 조회**
            - `q`를 비우면 전체 도감 목록을 조회합니다.
            
            🔍 **검색**
            - `q`에 검색 키워드를 입력하면 해당 단어를 포함하는 조류를 찾습니다.
            - `habitats`, `sizeCategories`, `seasons`에 필터를 추가하면 조건에 따라 결과를 좁힙니다.
            - **같은 필터 항목 내에서는 OR**, **다른 필터 항목 간에는 AND**로 적용됩니다.
            
            📄 **페이징 (선택)**
            - `page`는 1부터 시작합니다.
            - `page`와 `size`는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
            - 생략하면 전체 결과를 반환합니다.
            
            🔧 **쿼리 파라미터 예시**
            - `habitats=forest&habitats=marine&sizeCategories=small&seasons=summer`
            - List 타입 필터는 파라미터를 중복 선언하여 입력합니다.
            
            📌 **허용된 필터 값 목록** (대소문자 구분 없음, 잘못된 값 입력 시 Bad Request)
            
            - **habitats**:
                - mudflat (갯벌)
                - farmland (경작지/들판)
                - forest (산림/계곡)
                - marine (해양)
                - residential (거주지역)
                - plains_forest (평지숲)
                - river_lake (하천/호수)
                - artificial (인공시설)
                - cave (동굴)
                - wetland (습지)
                - others (기타)
            
            - **sizeCategories**:
                - xsmall, small, medium, large
            
            - **seasons**:
                - spring, summer, autumn, winter
            """,
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "도감 조회/검색 응답",
                    content = @Content(schema = @Schema(implementation = BirdSearchResponse.class))
            )
    )
    public ResponseEntity<BirdSearchResponse> getBirds(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(required = false) Integer size,
            @Parameter(description = "검색 키워드 (한글 이름)") @RequestParam(required = false) String q,
            @Parameter(description = "서식지 필터") @RequestParam(required = false) List<String> habitats,
            @Parameter(description = "크기 필터") @RequestParam(required = false) List<String> sizeCategories,
            @Parameter(description = "계절 필터") @RequestParam(required = false) List<String> seasons
    ) {
        BirdSearchCommand birdSearchCommand = new BirdSearchCommand();
        birdSearchCommand.setPage(page);
        birdSearchCommand.setSize(size);
        birdSearchCommand.setQ(q);
        birdSearchCommand.setHabitats(habitats);
        birdSearchCommand.setSizeCategories(sizeCategories);
        birdSearchCommand.setSeasons(seasons);

        BirdSearchResponse response = birdQueryService.getBirdSearchResponse(birdSearchCommand);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{birdId}")
    @Operation(
            summary = "특정 조류 상세 조회",
            description = "birdId를 기반으로 해당 조류의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                    responseCode = "200",
                    description = "조류 상세 응답",
                    content = @Content(schema = @Schema(implementation = BirdDetailResponse.class))
            ),
                    @ApiResponse(
                    responseCode = "404",
                    description = "조류를 찾을 수 없습니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ResponseEntity<BirdDetailResponse> getBirdDetail(
            @Parameter(description = "조회할 조류의 ID", example = "1") @PathVariable Long birdId) {
        BirdDetailResponse response = birdQueryService.getBirdDetailResponse(birdId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/autocomplete")
    @Operation(
            summary = "조류 자동완성",
            description = "조류 이름 검색을 위한 자동완성 제안을 반환합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "자동완성 결과",
                    content = @Content(schema = @Schema(implementation = BirdAutocompleteResponse.class))
            )
    )
    public ResponseEntity<BirdAutocompleteResponse> getBirdAutocomplete(
            @Parameter(description = "검색 키워드 (한글 이름)") @RequestParam String q
    ) {
        BirdAutocompleteResponse response = birdQueryService.getBirdAutocompleteResponse(q);
        return ResponseEntity.ok(response);
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
            summary = "조류 크기 카테고리 규칙 다운로드",
            description = "조류 크기 카테고리 규칙을 제공합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조류 크기 카테고리 규칙",
                    content = @Content(schema = @Schema(implementation = BirdSizeCategoryRulesResponse.class))
            )
    )
    public ResponseEntity<BirdSizeCategoryRulesResponse> getSizeCategoryRules() {
        BirdSizeCategoryRulesResponse response = birdQueryService.getSizeCategoryRulesResponse();
        return ResponseEntity.ok(response);
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
