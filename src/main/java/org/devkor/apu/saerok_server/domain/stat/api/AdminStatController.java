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
import org.devkor.apu.saerok_server.domain.stat.application.StatQueryService;
import org.devkor.apu.saerok_server.domain.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.global.shared.util.EnumParser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Stats API", description = "관리자용 통계 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/stats")
public class AdminStatController {

    private final StatQueryService queryService;

    @GetMapping("/series")
    @PreAuthorize("hasAnyRole('ADMIN_EDITOR', 'ADMIN_VIEWER')")
    @Operation(
            summary = "시계열 통계 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            metric 목록을 지정하면, 각 metric에 대한 시계열을 반환합니다.
            - 단일값: COLLECTION_TOTAL_COUNT, COLLECTION_PRIVATE_RATIO, BIRD_ID_PENDING_COUNT, BIRD_ID_RESOLVED_COUNT  → payload.value
            - 멀티값: BIRD_ID_RESOLUTION_STATS (min_hours, max_hours, avg_hours, stddev_hours)
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
}
