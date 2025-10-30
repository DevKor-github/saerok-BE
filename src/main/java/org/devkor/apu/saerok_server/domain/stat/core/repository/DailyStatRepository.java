package org.devkor.apu.saerok_server.domain.stat.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.stat.core.entity.DailyStat;
import org.devkor.apu.saerok_server.domain.stat.core.entity.StatMetric;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyStatRepository {

    private final EntityManager em;

    public void save(DailyStat stat) { em.persist(stat); }

    public Optional<DailyStat> findByMetricAndDate(StatMetric metric, LocalDate date) {
        return em.createQuery("SELECT s FROM DailyStat s WHERE s.metric = :m AND s.date = :d", DailyStat.class)
                .setParameter("m", metric)
                .setParameter("d", date)
                .getResultStream()
                .findFirst();
    }

    public void upsertPayload(StatMetric metric, LocalDate date, Map<String, Object> payload) {
        Optional<DailyStat> existing = findByMetricAndDate(metric, date);
        if (existing.isPresent()) {
            existing.get().overwritePayload(payload);
        } else {
            save(DailyStat.ofPayload(metric, date, payload));
        }
    }

    public void upsertValue(StatMetric metric, LocalDate date, Number value) {
        Optional<DailyStat> existing = findByMetricAndDate(metric, date);
        if (existing.isPresent()) {
            existing.get().overwritePayload(Map.of("value", value));
        } else {
            save(DailyStat.ofValue(metric, date, value));
        }
    }

    public List<DailyStat> findSeriesByMetric(StatMetric metric) {
        return em.createQuery("SELECT s FROM DailyStat s WHERE s.metric = :m ORDER BY s.date", DailyStat.class)
                .setParameter("m", metric)
                .getResultList();
    }

    public Optional<LocalDate> findLastDateOf(StatMetric metric) {
        return em.createQuery("SELECT MAX(s.date) FROM DailyStat s WHERE s.metric = :m", LocalDate.class)
                .setParameter("m", metric)
                .getResultStream()
                .findFirst();
    }
}
