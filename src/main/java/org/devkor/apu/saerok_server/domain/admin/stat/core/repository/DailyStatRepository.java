package org.devkor.apu.saerok_server.domain.admin.stat.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.DailyStat;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.StatMetric;
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

    public List<DailyStat> findSeriesByMetric(StatMetric metric, LocalDate startDate, LocalDate endDate) {
        StringBuilder jpql = new StringBuilder("SELECT s FROM DailyStat s WHERE s.metric = :m");

        if (startDate != null) {
            jpql.append(" AND s.date >= :start");
        }
        if (endDate != null) {
            jpql.append(" AND s.date <= :end");
        }
        jpql.append(" ORDER BY s.date");

        var query = em.createQuery(jpql.toString(), DailyStat.class)
                .setParameter("m", metric);

        if (startDate != null) {
            query.setParameter("start", startDate);
        }
        if (endDate != null) {
            query.setParameter("end", endDate);
        }

        return query.getResultList();
    }

    public Optional<LocalDate> findLastDateOf(StatMetric metric) {
        LocalDate max = em.createQuery(
                        "SELECT MAX(s.date) FROM DailyStat s WHERE s.metric = :m", LocalDate.class)
                .setParameter("m", metric)
                .getSingleResult(); // 없으면 null 반환
        return Optional.ofNullable(max);
    }
}
