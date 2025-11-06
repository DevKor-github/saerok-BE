package org.devkor.apu.saerok_server.domain.stat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.stat.api.dto.response.StatSeriesResponse;
import org.devkor.apu.saerok_server.domain.stat.application.StatAggregationService;
import org.devkor.apu.saerok_server.domain.stat.application.StatQueryService;
import org.devkor.apu.saerok_server.domain.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.global.shared.util.EnumParser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Admin Stats API", description = "관리자용 통계 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/stats")
public class AdminStatController {

    private final StatQueryService queryService;
    private final StatAggregationService aggregationService;

    @GetMapping("/series")
    @PreAuthorize("hasAnyRole('ADMIN_EDITOR', 'ADMIN_VIEWER')")
    @Operation(
            summary = "시계열 통계 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            metric 목록을 지정하면, 각 metric에 대한 시계열을 반환합니다.
            - 단일값: COLLECTION_TOTAL_COUNT, COLLECTION_PRIVATE_RATIO, BIRD_ID_PENDING_COUNT, BIRD_ID_RESOLVED_COUNT  → payload.value
            - 멀티값: BIRD_ID_RESOLUTION_STATS (min_hours, max_hours, avg_hours, stddev_hours)
            
            예시: GET /api/v1/admin/stats/series?metric=COLLECTION_PRIVATE_RATIO&metric=BIRD_ID_RESOLUTION_STATS
            
                    {
                      "series": [
                        {
                          "metric": "COLLECTION_PRIVATE_RATIO",
                          "points": [
                            {
                              "date": "2025-10-29",
                              "value": 0
                            },
                            {
                              "date": "2025-10-30",
                              "value": 0.3333333333333333
                            }
                          ],
                          "components": null
                        },
                        {
                          "metric": "BIRD_ID_RESOLUTION_STATS",
                          "points": null,
                          "components": [
                            {
                              "key": "min_hours",
                              "points": [
                                {
                                  "date": "2025-10-30",
                                  "value": 0.3258333333333333
                                }
                              ]
                            },
                            {
                              "key": "max_hours",
                              "points": [
                                {
                                  "date": "2025-10-30",
                                  "value": 0.3258333333333333
                                }
                              ]
                            },
                            {
                              "key": "avg_hours",
                              "points": [
                                {
                                  "date": "2025-10-30",
                                  "value": 0.3258333333333333
                                }
                              ]
                            },
                            {
                              "key": "stddev_hours",
                              "points": [
                                {
                                  "date": "2025-10-30",
                                  "value": 0
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
            
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = StatSeriesResponse.class)))
            }
    )
    public StatSeriesResponse getSeries(
            @Parameter(description = "반복 지정 가능") @RequestParam List<String> metric
    ) {
        List<StatMetric> metrics = metric.stream()
                .map(s -> EnumParser.fromString(StatMetric.class, s))
                .toList();
        return queryService.getSeries(metrics);
    }

    @PostMapping("/aggregate-yesterday")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN_EDITOR')")
    @Operation(
            summary = "수동 집계: 기준일의 전날(어제) 한 날짜만 집계",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            basisDate(yyyy-MM-dd, KST 기준)를 받아 **그 전날 하루치** 통계를 모든 metric에 대해 집계합니다.
            예) basisDate=2025-10-30 → 2025-10-29 하루치 집계
            """
    )
    public void aggregateYesterday(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd, KST 기준)", example = "2025-10-30", required = true)
            @RequestParam String basisDate
    ) {
        LocalDate base = LocalDate.parse(basisDate);
        LocalDate target = base.minusDays(1);
        // 모든 metric을 해당 날짜에 대해 한 번만 집계
        aggregationService.aggregateFor(target, null);
    }
}
