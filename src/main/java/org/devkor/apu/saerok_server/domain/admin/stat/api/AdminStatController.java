package org.devkor.apu.saerok_server.domain.admin.stat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.stat.api.dto.response.StatSeriesResponse;
import org.devkor.apu.saerok_server.domain.admin.stat.application.StatAggregationService;
import org.devkor.apu.saerok_server.domain.admin.stat.application.StatQueryService;
import org.devkor.apu.saerok_server.domain.admin.stat.application.CurrentUserStatQueryService;
import org.devkor.apu.saerok_server.domain.admin.stat.api.dto.response.CurrentUserStatResponse;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.StatMetric;
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
    private final CurrentUserStatQueryService currentUserStatQueryService;

    @GetMapping("/current-users")
    @PreAuthorize("@perm.has('ADMIN_STAT_READ')")
    @Operation(
            summary = "현재 사용자 현황 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            조회 시점의 가입 완료 사용자 현황을 반환합니다. 일별 통계 테이블을 사용하지 않습니다.
            플랫폼별 수는 활성 푸시 토큰을 보유한 사용자 수이며, 한 사용자가 여러 플랫폼에 중복 포함될 수 있습니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CurrentUserStatResponse.class)))
            }
    )
    public CurrentUserStatResponse getCurrentUserStats() {
        return currentUserStatQueryService.getCurrentUserStats();
    }

    @GetMapping("/series")
    @PreAuthorize("@perm.has('ADMIN_STAT_READ')")
    @Operation(
            summary = "시계열 통계 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            metric 목록을 지정하면, 각 metric에 대한 시계열을 반환합니다.
            - 단일값: COLLECTION_TOTAL_COUNT, COLLECTION_PRIVATE_RATIO, BIRD_ID_PENDING_COUNT, BIRD_ID_RESOLVED_COUNT  → payload.value
            - 멀티값: BIRD_ID_RESOLUTION_STATS_28D (min_hours, max_hours, avg_hours, stddev_hours),
              USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE (IOS, ANDROID 누적 가입 사용자 수)
            
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = StatSeriesResponse.class)))
            }
    )
    public StatSeriesResponse getSeries(
            @Parameter(description = "반복 지정 가능") @RequestParam List<String> metric,
            @Parameter(description = "조회 기간 (시작일,종료일)", example = "2024-01-01,2024-01-31")
            @RequestParam(required = false) String period
    ) {
        List<StatMetric> metrics = metric.stream()
                .map(s -> EnumParser.fromString(StatMetric.class, s))
                .toList();
        return queryService.getSeries(metrics, period);
    }

    @PostMapping("/aggregate-yesterday")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.has('ADMIN_STAT_WRITE')")
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
