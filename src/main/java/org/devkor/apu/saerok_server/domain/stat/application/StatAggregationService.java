package org.devkor.apu.saerok_server.domain.stat.application;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.domain.stat.core.repository.BirdIdRequestHistoryRepository;
import org.devkor.apu.saerok_server.domain.stat.core.repository.DailyStatRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class StatAggregationService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final EntityManager em;
    private final DailyStatRepository dailyRepo;
    private final BirdIdRequestHistoryRepository histRepo;

    /* Public API */

    public void aggregateFor(LocalDate date, Set<StatMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) metrics = EnumSet.allOf(StatMetric.class);
        for (StatMetric m : metrics) {
            switch (m) {
                case COLLECTION_TOTAL_COUNT -> aggregateCollectionTotalCount(date);
                case COLLECTION_PRIVATE_RATIO -> aggregateCollectionPrivateRatio(date);
                case BIRD_ID_PENDING_COUNT -> aggregatePendingCount(date);
                case BIRD_ID_RESOLVED_COUNT -> aggregateResolvedDaily(date);
                case BIRD_ID_RESOLUTION_STATS -> aggregateResolutionStatsCumulative(date);
            }
        }
    }

    public void aggregateRange(LocalDate fromInclusive, LocalDate toInclusive, Set<StatMetric> metrics) {
        LocalDate d = fromInclusive;
        while (!d.isAfter(toInclusive)) {
            aggregateFor(d, metrics);
            d = d.plusDays(1);
        }
    }

    /* Metric calculators (단일값 → payload.value) */

    private void aggregateCollectionTotalCount(LocalDate date) {
        var end = endExclusive(date);
        long total = em.createQuery("""
                SELECT COUNT(c) FROM UserBirdCollection c
                WHERE c.createdAt < :end
                """, Long.class)
                .setParameter("end", end)
                .getSingleResult();

        dailyRepo.upsertValue(StatMetric.COLLECTION_TOTAL_COUNT, date, total);
    }

    private void aggregateCollectionPrivateRatio(LocalDate date) {
        var end = endExclusive(date);
        long total = em.createQuery("""
                SELECT COUNT(c) FROM UserBirdCollection c
                WHERE c.createdAt < :end
                """, Long.class)
                .setParameter("end", end)
                .getSingleResult();

        double ratio = 0.0;
        if (total > 0) {
            long priv = em.createQuery("""
                    SELECT COUNT(c) FROM UserBirdCollection c
                    WHERE c.createdAt < :end AND c.accessLevel = :priv
                    """, Long.class)
                    .setParameter("end", end)
                    .setParameter("priv", AccessLevelType.PRIVATE)
                    .getSingleResult();
            ratio = ((double) priv) / total; // 0..1
        }
        dailyRepo.upsertValue(StatMetric.COLLECTION_PRIVATE_RATIO, date, ratio);
    }

    private void aggregatePendingCount(LocalDate date) {
        var end = endExclusive(date);
        long open = histRepo.countPendingAsOf(end);
        dailyRepo.upsertValue(StatMetric.BIRD_ID_PENDING_COUNT, date, open);
    }

    private void aggregateResolvedDaily(LocalDate date) {
        var start = date.atStartOfDay(KST).toOffsetDateTime();
        var end = endExclusive(date);
        long resolvedDaily = histRepo.countResolvedOnDate(start, end);
        dailyRepo.upsertValue(StatMetric.BIRD_ID_RESOLVED_COUNT, date, resolvedDaily);
    }

    /* Metric calculators (멀티값 → payload.{min/max/avg/stddev}_hours) */

    private void aggregateResolutionStatsCumulative(LocalDate date) {
        var end = endExclusive(date);
        Object[] row = histRepo.resolutionStatsCumulativeSecondsAsOf(end); // [min,max,avg,stddev] in seconds

        // ADOPT 해결이 하나도 없으면 저장 생략
        if (row == null || row[0] == null || row[1] == null || row[2] == null || row[3] == null) return;

        double minH = ((Number) row[0]).doubleValue() / 3600.0;
        double maxH = ((Number) row[1]).doubleValue() / 3600.0;
        double avgH = ((Number) row[2]).doubleValue() / 3600.0;
        double stdH = ((Number) row[3]).doubleValue() / 3600.0;

        Map<String, Object> p = new HashMap<>();
        p.put("min_hours",    minH);
        p.put("max_hours",    maxH);
        p.put("avg_hours",    avgH);
        p.put("stddev_hours", stdH);

        dailyRepo.upsertPayload(StatMetric.BIRD_ID_RESOLUTION_STATS, date, p);
    }

    /* Helpers */

    private OffsetDateTime endExclusive(LocalDate date) {
        return date.plusDays(1).atStartOfDay(KST).toOffsetDateTime();
    }
}
