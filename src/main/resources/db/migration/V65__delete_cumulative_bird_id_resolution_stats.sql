-- 삭제 대상: 누적 기준 동정 해결 시간 통계
DELETE FROM daily_stat
WHERE metric = 'BIRD_ID_RESOLUTION_STATS';
