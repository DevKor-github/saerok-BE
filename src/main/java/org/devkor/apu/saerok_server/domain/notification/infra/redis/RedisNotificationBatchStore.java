package org.devkor.apu.saerok_server.domain.notification.infra.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.BatchKey;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.NotificationBatch;
import org.devkor.apu.saerok_server.domain.notification.application.store.NotificationBatchStore;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationBatchConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Redis 기반 알림 배치 저장소 구현체.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationBatchStore implements NotificationBatchStore {

    /**
     * 만료 시간 인덱스용 Sorted Set.
     * score: 만료 시간 타임스탬프 (밀리초)
     * member: 배치 데이터 Redis 키
     */
    private static final String EXPIRY_INDEX = "notification:batch:expiry_index";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationBatchConfig batchConfig;

    @Override
    public Optional<NotificationBatch> findBatch(BatchKey key) {
        try {
            String redisKey = key.toRedisKey();
            String json = redisTemplate.opsForValue().get(redisKey);

            if (json == null) {
                return Optional.empty();
            }

            NotificationBatchDto dto = objectMapper.readValue(json, NotificationBatchDto.class);
            NotificationBatch batch = dto.toBatch();
            return Optional.of(batch);

        } catch (JsonProcessingException e) {
            log.error("Redis에서 배치 데이터 역직렬화에 실패했습니다: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void saveBatch(NotificationBatch batch) {
        try {
            String redisKey = batch.getKey().toRedisKey();
            NotificationBatchDto dto = NotificationBatchDto.fromBatch(batch);
            String json = objectMapper.writeValueAsString(dto);

            redisTemplate.opsForValue().set(redisKey, json, Duration.ofSeconds(batchConfig.getTtlSeconds()));

            long expiryTimestamp = batch.getExpiresAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            redisTemplate.opsForZSet().add(EXPIRY_INDEX, redisKey, expiryTimestamp);

        } catch (JsonProcessingException e) {
            log.error("Redis에서 배치 데이터 직렬화에 실패했습니다: {}", batch.getKey(), e);
            throw new IllegalStateException("Redis에 배치 저장하는 것에 실패했습니다", e);
        }
    }

    @Override
    public void deleteBatch(BatchKey key) {
        String redisKey = key.toRedisKey();
        redisTemplate.delete(redisKey);
        redisTemplate.opsForZSet().remove(EXPIRY_INDEX, redisKey);
    }

    @Override
    public List<NotificationBatch> findExpiredBatches() {
        List<NotificationBatch> expiredBatches = new ArrayList<>();

        try {
            long now = System.currentTimeMillis();

            // Sorted Set에서 score가 현재 시간 이하인 키들만 조회
            Set<String> expiredKeys = redisTemplate.opsForZSet()
                    .rangeByScore(EXPIRY_INDEX, 0, now);

            if (expiredKeys == null || expiredKeys.isEmpty()) {
                return expiredBatches;
            }

            // 만료된 키들의 데이터 조회
            for (String redisKey : expiredKeys) {
                String json = redisTemplate.opsForValue().get(redisKey);

                if (json == null) {
                    redisTemplate.opsForZSet().remove(EXPIRY_INDEX, redisKey);
                    continue;
                }

                try {
                    NotificationBatchDto dto = objectMapper.readValue(json, NotificationBatchDto.class);
                    NotificationBatch batch = dto.toBatch();

                    if (batch.isExpired()) {
                        expiredBatches.add(batch);
                        redisTemplate.opsForZSet().remove(EXPIRY_INDEX, redisKey);
                    }

                } catch (JsonProcessingException e) {
                    log.error("Redis 키 역직렬화에 실패했습니다: {}", redisKey, e);
                    redisTemplate.opsForZSet().remove(EXPIRY_INDEX, redisKey);
                }
            }
        } catch (Exception e) {
            log.error("만료된 배치 조회에 실패했습니다.", e);
        }

        return expiredBatches;
    }
}
