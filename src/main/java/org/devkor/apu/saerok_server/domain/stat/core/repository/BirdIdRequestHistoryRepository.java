package org.devkor.apu.saerok_server.domain.stat.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.stat.core.entity.BirdIdRequestHistory;
import org.devkor.apu.saerok_server.domain.stat.core.entity.ResolutionKind;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class BirdIdRequestHistoryRepository {

    private final EntityManager em;

    public void save(BirdIdRequestHistory h) { em.persist(h); }

    public Optional<BirdIdRequestHistory> findOpenByCollectionId(Long collectionId) {
        return em.createQuery("""
                SELECT h FROM BirdIdRequestHistory h
                WHERE h.collection.id = :cid AND h.resolvedAt IS NULL
                ORDER BY h.startedAt DESC
                """, BirdIdRequestHistory.class)
                .setParameter("cid", collectionId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public void deleteOpenByCollectionId(Long collectionId) {
        em.createQuery("""
                DELETE FROM BirdIdRequestHistory h
                WHERE h.collection.id = :cid AND h.resolvedAt IS NULL
                """)
                .setParameter("cid", collectionId)
                .executeUpdate();
    }

    /** 여러 컬렉션에 대해 열린 히스토리의 startedAt을 한 번에 조회 */
    public Map<Long, OffsetDateTime> findOpenStartedAtMapByCollectionIds(List<Long> collectionIds) {
        if (collectionIds == null || collectionIds.isEmpty()) return Map.of();

        List<Object[]> rows = em.createQuery("""
                SELECT h.collection.id, h.startedAt
                FROM BirdIdRequestHistory h
                WHERE h.collection.id IN :ids AND h.resolvedAt IS NULL
                """, Object[].class)
                .setParameter("ids", collectionIds)
                .getResultList();

        Map<Long, OffsetDateTime> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (OffsetDateTime) row[1]);
        }
        return map;
    }

    public long countPendingAsOf(OffsetDateTime endExclusive) {
        return em.createQuery("""
                SELECT COUNT(h) FROM BirdIdRequestHistory h
                WHERE h.startedAt < :end
                  AND (h.resolvedAt IS NULL OR h.resolvedAt >= :end)
                """, Long.class)
                .setParameter("end", endExclusive)
                .getSingleResult();
    }

    /** 누적(해당 시점까지) 해결 시간 통계 — 단위: "초" (ADOPT만) */
    public Object[] resolutionStatsCumulativeSecondsAsOf(OffsetDateTime endExclusive) {
        Object[] row = (Object[]) em.createNativeQuery("""
        SELECT
          MIN(resolution_seconds) AS min_s,
          MAX(resolution_seconds) AS max_s,
          AVG(resolution_seconds) AS avg_s,
          STDDEV_POP(resolution_seconds) AS stddev_s
        FROM bird_id_request_history
        WHERE resolved_at IS NOT NULL
          AND resolution_kind = 'ADOPT'
          AND resolved_at < ?1
        """)
                .setParameter(1, endExclusive)
                .getSingleResult();
        return row;
    }

    /** 윈도우(구간) 해결 시간 통계 — 단위: "초" (ADOPT만) */
    public Object[] resolutionStatsWindowSeconds(OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
        Object[] row = (Object[]) em.createNativeQuery("""
        SELECT
          MIN(resolution_seconds) AS min_s,
          MAX(resolution_seconds) AS max_s,
          AVG(resolution_seconds) AS avg_s,
          STDDEV_POP(resolution_seconds) AS stddev_s
        FROM bird_id_request_history
        WHERE resolved_at IS NOT NULL
          AND resolution_kind = 'ADOPT'
          AND resolved_at >= ?1
          AND resolved_at < ?2
        """)
                .setParameter(1, startInclusive)
                .setParameter(2, endExclusive)
                .getSingleResult();
        return row;
    }

    public long countResolvedOnDate(OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
        return em.createQuery("""
                SELECT COUNT(h) FROM BirdIdRequestHistory h
                WHERE h.resolvedAt IS NOT NULL
                  AND h.resolutionKind = :kind
                  AND h.resolvedAt >= :start
                  AND h.resolvedAt < :end
                """, Long.class)
                .setParameter("kind", ResolutionKind.ADOPT)
                .setParameter("start", startInclusive)
                .setParameter("end", endExclusive)
                .getSingleResult();
    }
}
