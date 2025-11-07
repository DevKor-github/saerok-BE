package org.devkor.apu.saerok_server.domain.stat.core.entity;

public enum StatMetric {
    COLLECTION_TOTAL_COUNT,          // 누적 새록 수 (스냅샷, 단일값)
    COLLECTION_PRIVATE_RATIO,        // 비공개 새록 비율 0..1 (스냅샷, 단일값)
    BIRD_ID_PENDING_COUNT,           // 진행 중 동정 요청 수 (스냅샷, 단일값)
    BIRD_ID_RESOLVED_COUNT,          // 누적 동정 요청 해결 수 (스냅샷, 단일값) — 동정 제안 채택(ADOPT)으로만 증가
    BIRD_ID_RESOLUTION_STATS,        // 동정 요청 해결 시간 통계(누적): min/max/avg/stddev (멀티값) — 채택(ADOPT)만 집계
    BIRD_ID_RESOLUTION_STATS_28D,    // 동정 요청 해결 시간 통계(최근 28일): min/max/avg/stddev (멀티값) — 채택(ADOPT)만 집계

    USER_COMPLETED_TOTAL,            // 누적 가입자 수(스냅샷) — COMPLETED && 삭제되지 않음
    USER_SIGNUP_DAILY,               // 일일 가입자 수 — signup_completed_at 기준
    USER_WITHDRAWAL_DAILY,           // 일일 탈퇴자 수 — deleted_at 기준
    USER_DAU,                        // 일일 활성 사용자 수 — user_activity_ping DISTINCT(user_id)
    USER_WAU,                        // 주간 활성 사용자 수(마지막 7일 rolling)
    USER_MAU                         // 월간 활성 사용자 수(마지막 30일 rolling)
}
