package org.devkor.apu.saerok_server.domain.stat.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.stat.api.dto.response.StatSeriesResponse;
import org.devkor.apu.saerok_server.domain.stat.core.entity.DailyStat;
import org.devkor.apu.saerok_server.domain.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.domain.stat.core.repository.DailyStatRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatQueryService {

    private final DailyStatRepository dailyRepo;

    public StatSeriesResponse getSeries(List<StatMetric> metrics, String period) {
        if (metrics == null || metrics.isEmpty()) return StatSeriesResponse.empty();

        LocalDateRange range = parsePeriod(period);
        List<StatSeriesResponse.Series> out = new ArrayList<>();
        for (StatMetric m : metrics) {
            List<DailyStat> rows = dailyRepo.findSeriesByMetric(m, range.startDate(), range.endDate());

            if (m == StatMetric.BIRD_ID_RESOLUTION_STATS_28D) {
                var minSeries = new StatSeriesResponse.ComponentSeries(
                        "min_hours",
                        rows.stream().map(s ->
                                new StatSeriesResponse.Point(s.getDate(), numberOrNull(s.getPayload().get("min_hours")))).toList()
                );
                var maxSeries = new StatSeriesResponse.ComponentSeries(
                        "max_hours",
                        rows.stream().map(s ->
                                new StatSeriesResponse.Point(s.getDate(), numberOrNull(s.getPayload().get("max_hours")))).toList()
                );
                var avgSeries = new StatSeriesResponse.ComponentSeries(
                        "avg_hours",
                        rows.stream().map(s ->
                                new StatSeriesResponse.Point(s.getDate(), numberOrNull(s.getPayload().get("avg_hours")))).toList()
                );
                var stdSeries = new StatSeriesResponse.ComponentSeries(
                        "stddev_hours",
                        rows.stream().map(s ->
                                new StatSeriesResponse.Point(s.getDate(), numberOrNull(s.getPayload().get("stddev_hours")))).toList()
                );

                out.add(StatSeriesResponse.multi(m.name(), List.of(minSeries, maxSeries, avgSeries, stdSeries)));
            } else {
                List<StatSeriesResponse.Point> points = rows.stream()
                        .map(s -> new StatSeriesResponse.Point(s.getDate(), numberOrNull(s.getPayload().get("value"))))
                        .toList();
                out.add(StatSeriesResponse.single(m.name(), points));
            }
        }
        return new StatSeriesResponse(out);
    }

    private static Number numberOrNull(Object o) {
        return (o instanceof Number n) ? n : null;
    }

    private LocalDateRange parsePeriod(String period) {
        if (period == null || period.isBlank()) {
            return LocalDateRange.empty();
        }

        String[] tokens = period.split(",");
        if (tokens.length != 2) {
            throw new BadRequestException("period 파라미터는 '시작일,종료일' 형식이어야 합니다.");
        }

        LocalDate start = parseDate(tokens[0].trim(), "시작일");
        LocalDate end = parseDate(tokens[1].trim(), "종료일");

        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("period 파라미터는 종료일이 시작일보다 빠를 수 없습니다.");
        }

        return new LocalDateRange(start, end);
    }

    private LocalDate parseDate(String value, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(label + "은 yyyy-MM-dd 형식이어야 합니다.");
        }
    }

    private record LocalDateRange(LocalDate startDate, LocalDate endDate) {
        private static LocalDateRange empty() {
            return new LocalDateRange(null, null);
        }
    }
}
