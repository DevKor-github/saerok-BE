package org.devkor.apu.saerok_server.domain.stat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.domain.stat.core.repository.DailyStatRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatBatchScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StatAggregationService aggregation;
    private final DailyStatRepository dailyRepo;

    /**
     * 매일 새벽 03:30 KST, 전날 기준으로 집계 누락분을 모두 채운다.
     * (신규 사용자 지표도 동일 정책: 마지막 집계 없으면 '어제'만 1회 집계)
     */
    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    public void runDailyAggregation() {
        LocalDate yesterday = LocalDate.now(KST).minusDays(1);

        for (StatMetric metric : EnumSet.of(
                StatMetric.COLLECTION_TOTAL_COUNT,
                StatMetric.COLLECTION_PRIVATE_RATIO,
                StatMetric.BIRD_ID_PENDING_COUNT,
                StatMetric.BIRD_ID_RESOLVED_COUNT,
                StatMetric.BIRD_ID_RESOLUTION_STATS_28D,

                StatMetric.USER_COMPLETED_TOTAL,
                StatMetric.USER_SIGNUP_DAILY,
                StatMetric.USER_WITHDRAWAL_DAILY,
                StatMetric.USER_DAU,
                StatMetric.USER_WAU,
                StatMetric.USER_MAU
        )) {
            var last = dailyRepo.findLastDateOf(metric).orElse(null);
            LocalDate from = (last == null) ? yesterday : last.plusDays(1);
            if (!from.isAfter(yesterday)) {
                log.info("[stat] aggregate {} from {} to {}", metric, from, yesterday);
                aggregation.aggregateRange(from, yesterday, EnumSet.of(metric));
            }
        }
    }
}
