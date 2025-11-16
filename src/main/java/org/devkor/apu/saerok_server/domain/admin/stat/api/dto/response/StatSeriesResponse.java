package org.devkor.apu.saerok_server.domain.admin.stat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "관리자 통계 시계열 응답 DTO")
public record StatSeriesResponse(
        List<Series> series
) {
    public static StatSeriesResponse empty() { return new StatSeriesResponse(List.of()); }

    public static Series single(String metric, List<Point> points) {
        return new Series(metric, points, null);
    }
    public static Series multi(String metric, List<ComponentSeries> components) {
        return new Series(metric, null, components);
    }

    /** 단일값 or 멀티값(컴포넌트들) 중 하나만 채운다. */
    public record Series(
            String metric,
            List<Point> points,                 // 단일값 메트릭일 때
            List<ComponentSeries> components    // 멀티값 메트릭일 때 (예: min/max/avg/stddev)
    ) {}

    public record ComponentSeries(
            String key,            // "min_hours" | "max_hours" | "avg_hours" | "stddev_hours"
            List<Point> points
    ) {}

    public record Point(
            LocalDate date,
            Number value  // long or double
    ) {}
}
