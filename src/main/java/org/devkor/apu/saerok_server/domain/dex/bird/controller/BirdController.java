package org.devkor.apu.saerok_server.domain.dex.bird.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Birds API", description = "도감 기능 관련 API")
@RestController
@RequestMapping("${api_prefix}/birds")
public class BirdController {

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
            summary = "🛠 [미구현] 특정 조류 상세 조회",
            description = "birdId를 기반으로 해당 조류의 상세 정보를 조회합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조류 상세 응답",
                    content = @Content(schema = @Schema(implementation = BirdDetailResponse.class))
            )
    )
    public void getBirdDetail() {
        // 미구현
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

    @Schema(description = "조류 목록 응답 DTO")
    public static class BirdListResponse {
        @Schema(description = "조류 ID", example = "1")
        public Long id;

        @Schema(description = "한글 이름", example = "까치")
        public String koreanName;

        @Schema(description = "학명", example = "Pica pica")
        public String scientificName;

        @Schema(description = "썸네일 이미지 URL", example = "https://example.com/images/bird-thumb.jpg")
        public String thumbImageUrl;
    }

    @Schema(description = "조류 상세 응답 DTO")
    public static class BirdDetailResponse {
        @Schema(description = "조류 ID", example = "1")
        public Long id;

        @Schema(description = "한글 이름", example = "까치")
        public String koreanName;

        @Schema(description = "학명", example = "Pica pica")
        public String scientificName;

        @Schema(description = "분류학적 정보")
        public BirdTaxonomy taxonomy;

        @Schema(description = "조류 설명", example = "전국 어디서나 흔하게 관찰되는 텃새입니다.")
        public String description;
    }

    @Schema(description = "분류학적 정보")
    public static class BirdTaxonomy {
        public String phylumEng;
        public String phylumKor;
        public String classEng;
        public String classKor;
        public String orderEng;
        public String orderKor;
        public String familyEng;
        public String familyKor;
        public String genusEng;
        public String genusKor;
        public String speciesEng;
        public String speciesKor;
    }

    @Schema(description = "조류 자동완성 응답 DTO")
    public static class BirdAutocompleteResponse {
        @Schema(description = "추천 이름 리스트", example = "[\"까치\", \"까마귀\"]")
        public List<String> suggestions;
    }
}
