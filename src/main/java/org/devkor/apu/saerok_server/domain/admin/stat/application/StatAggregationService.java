package org.devkor.apu.saerok_server.domain.admin.stat.application;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.domain.admin.stat.core.repository.BirdIdRequestHistoryRepository;
import org.devkor.apu.saerok_server.domain.admin.stat.core.repository.DailyStatRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                case BIRD_ID_RESOLUTION_STATS_28D -> aggregateResolutionStatsRecent28d(date);

                case USER_COMPLETED_TOTAL -> aggregateUserCompletedTotal(date);
                case USER_SIGNUP_DAILY -> aggregateUserSignupDaily(date);
                case USER_WITHDRAWAL_DAILY -> aggregateUserWithdrawalDaily(date);
                case USER_DAU -> aggregateUserDau(date);
                case USER_WAU -> aggregateUserWau(date);
                case USER_MAU -> aggregateUserMau(date);
                case USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE -> aggregateUserDevicePlatformSignupDaily(date);

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

    /** 최근 28일: [end-28d, end) ADOPT 해결 시간 통계 */
    private void aggregateResolutionStatsRecent28d(LocalDate date) {
        var end = endExclusive(date);
        var start = end.minusDays(28);
        Object[] row = histRepo.resolutionStatsWindowSeconds(start, end); // [min,max,avg,stddev] in seconds

        // 최근 28일 내 ADOPT 해결이 하나도 없으면 저장 생략
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

        dailyRepo.upsertPayload(StatMetric.BIRD_ID_RESOLUTION_STATS_28D, date, p);
    }

    /** 누적 가입자 수: COMPLETED 이고, day 끝 시점 기준으로 삭제되지 않은 사용자 */
    private void aggregateUserCompletedTotal(LocalDate date) {
        var end = endExclusive(date);
        long total = em.createQuery("""
                SELECT COUNT(u) FROM User u
                WHERE (u.signupStatus = :completed AND u.deletedAt IS NULL)
                  OR (u.signupStatus = :withdrawn AND u.deletedAt IS NOT NULL AND u.deletedAt >= :end)
                """, Long.class)
                .setParameter("completed", SignupStatusType.COMPLETED)
                .setParameter("withdrawn", SignupStatusType.WITHDRAWN)
                .setParameter("end", end)
                .getSingleResult();

        dailyRepo.upsertValue(StatMetric.USER_COMPLETED_TOTAL, date, total);
    }

    /** 일일 가입자 수: signupCompletedAt ∈ [start, end) */
    private void aggregateUserSignupDaily(LocalDate date) {
        var start = date.atStartOfDay(KST).toOffsetDateTime();
        var end = endExclusive(date);
        long count = em.createQuery("""
                SELECT COUNT(u) FROM User u
                WHERE u.signupCompletedAt >= :start AND u.signupCompletedAt < :end
                """, Long.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        dailyRepo.upsertValue(StatMetric.USER_SIGNUP_DAILY, date, count);
    }

    /** 일일 탈퇴자 수: deletedAt ∈ [start, end) */
    private void aggregateUserWithdrawalDaily(LocalDate date) {
        var start = date.atStartOfDay(KST).toOffsetDateTime();
        var end = endExclusive(date);
        long count = em.createQuery("""
                SELECT COUNT(u) FROM User u
                WHERE u.deletedAt >= :start AND u.deletedAt < :end
                """, Long.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        dailyRepo.upsertValue(StatMetric.USER_WITHDRAWAL_DAILY, date, count);
    }

    /** DAU: user_activity_ping DISTINCT(user_id) ∈ [start, end) */
    private void aggregateUserDau(LocalDate date) {
        var start = date.atStartOfDay(KST).toOffsetDateTime();
        var end = endExclusive(date);
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(DISTINCT user_id) FROM user_activity_ping
                WHERE occurred_at >= ?1 AND occurred_at < ?2
                """)
                .setParameter(1, start)
                .setParameter(2, end)
                .getSingleResult();
        dailyRepo.upsertValue(StatMetric.USER_DAU, date, n.longValue());
    }

    /** WAU: [end-7d, end) */
    private void aggregateUserWau(LocalDate date) {
        var end = endExclusive(date);
        var start = end.minusDays(7);
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(DISTINCT user_id) FROM user_activity_ping
                WHERE occurred_at >= ?1 AND occurred_at < ?2
                """)
                .setParameter(1, start)
                .setParameter(2, end)
                .getSingleResult();
        dailyRepo.upsertValue(StatMetric.USER_WAU, date, n.longValue());
    }

    /** MAU: [end-30d, end) */
    private void aggregateUserMau(LocalDate date) {
        var end = endExclusive(date);
        var start = end.minusDays(30);
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(DISTINCT user_id) FROM user_activity_ping
                WHERE occurred_at >= ?1 AND occurred_at < ?2
                """)
                .setParameter(1, start)
                .setParameter(2, end)
                .getSingleResult();
        dailyRepo.upsertValue(StatMetric.USER_MAU, date, n.longValue());
    }

    /**
     * 플랫폼별 일일 신규 가입 사용자 수를 저장한다.
     *
     * <p>사용자-플랫폼마다 첫 기기 등록만 사용하며, 가입 완료와 첫 기기 등록 중 더 늦은 날을
     * 플랫폼 가입일로 본다. 조회 단계에서 이 일별 증분을 누적해 증가 추이를 만든다.</p>
     */
    private void aggregateUserDevicePlatformSignupDaily(LocalDate date) {
        var start = date.atStartOfDay(KST).toOffsetDateTime();
        var end = endExclusive(date);
        boolean initialAggregation = dailyRepo
                .findLastDateOf(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE)
                .isEmpty();

        List<Object[]> rows = em.createQuery("""
                SELECT ud.platform, COUNT(DISTINCT u.id) FROM UserDevice ud
                JOIN ud.user u
                WHERE ud.createdAt = (
                    SELECT MIN(ud2.createdAt) FROM UserDevice ud2
                    WHERE ud2.user.id = u.id
                      AND ud2.platform = ud.platform
                )
                  AND (
                    (
                        :initialAggregation = TRUE
                        AND (
                            (u.signupCompletedAt IS NOT NULL
                                AND u.signupCompletedAt < :end
                                AND ud.createdAt < :end)
                            OR (u.signupCompletedAt IS NULL
                                AND u.signupStatus IN (:completed, :withdrawn)
                                AND u.joinedAt < :end
                                AND ud.createdAt < :end)
                        )
                    )
                    OR (
                        :initialAggregation = FALSE
                        AND (
                            (
                                u.signupCompletedAt IS NOT NULL
                                AND (
                                    (u.signupCompletedAt >= :start AND u.signupCompletedAt < :end
                                        AND ud.createdAt <= u.signupCompletedAt)
                                    OR (ud.createdAt >= :start AND ud.createdAt < :end
                                        AND u.signupCompletedAt < ud.createdAt)
                                )
                            )
                            OR (
                                u.signupCompletedAt IS NULL
                                AND u.signupStatus IN (:completed, :withdrawn)
                                AND (
                                    (u.joinedAt >= :start AND u.joinedAt < :end
                                        AND ud.createdAt <= u.joinedAt)
                                    OR (ud.createdAt >= :start AND ud.createdAt < :end
                                        AND u.joinedAt < ud.createdAt)
                                )
                            )
                        )
                    )
                )
                GROUP BY ud.platform
                """, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("initialAggregation", initialAggregation)
                .setParameter("completed", SignupStatusType.COMPLETED)
                .setParameter("withdrawn", SignupStatusType.WITHDRAWN)
                .getResultList();

        Map<String, Object> payload = new HashMap<>();
        for (Object[] row : rows) {
            payload.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        dailyRepo.upsertPayload(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, date, payload);
    }

    /* Helpers */

    private OffsetDateTime endExclusive(LocalDate date) {
        return date.plusDays(1).atStartOfDay(KST).toOffsetDateTime();
    }
}
