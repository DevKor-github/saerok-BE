package org.devkor.apu.saerok_server.domain.stat.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.stat.api.dto.response.StatSeriesResponse;
import org.devkor.apu.saerok_server.domain.stat.core.entity.DailyStat;
import org.devkor.apu.saerok_server.domain.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.domain.stat.core.repository.DailyStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatQueryService {

    private final DailyStatRepository dailyRepo;

    public StatSeriesResponse getSeries(List<StatMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) return StatSeriesResponse.empty();

        List<StatSeriesResponse.Series> out = new ArrayList<>();
        for (StatMetric m : metrics) {
            List<DailyStat> rows = dailyRepo.findSeriesByMetric(m);

            if (m == StatMetric.BIRD_ID_RESOLUTION_STATS || m == StatMetric.BIRD_ID_RESOLUTION_STATS_28D) {
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
}
