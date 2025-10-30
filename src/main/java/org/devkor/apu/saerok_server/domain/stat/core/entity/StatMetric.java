package org.devkor.apu.saerok_server.domain.stat.core.entity;

public enum StatMetric {
    COLLECTION_TOTAL_COUNT,          // 누적 새록 수 (스냅샷, 단일값)
    COLLECTION_PRIVATE_RATIO,        // 비공개 새록 비율 0..1 (스냅샷, 단일값)
    BIRD_ID_PENDING_COUNT,           // 진행 중 동정 요청 수 (스냅샷, 단일값)
    BIRD_ID_RESOLVED_COUNT,          // 누적 동정 요청 해결 수 (스냅샷, 단일값) — 동정 제안 채택(ADOPT)으로만 증가
    BIRD_ID_RESOLUTION_STATS         // 동정 요청 해결 시간 통계(누적): min/max/avg/stddev (멀티값) — 채택(ADOPT)으로만 집계
}
