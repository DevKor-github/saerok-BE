package org.devkor.apu.saerok_server.domain.notification.infra.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.BatchKey;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.NotificationBatch;
import org.devkor.apu.saerok_server.domain.notification.application.store.NotificationBatchStore;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationBatchConfig;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Redis 기반 알림 배치 저장소 구현체.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationBatchStore implements NotificationBatchStore {

    private static final String KEY_PREFIX = "notification:batch:";
    private static final String KEY_PATTERN = KEY_PREFIX + "*";

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

        } catch (JsonProcessingException e) {
            log.error("Redis에서 배치 데이터 직렬화에 실패했습니다: {}", batch.getKey(), e);
            throw new IllegalStateException("Redis에 배치 저장하는 것에 실패했습니다", e);
        }
    }

    @Override
    public void deleteBatch(BatchKey key) {
        String redisKey = key.toRedisKey();
        redisTemplate.delete(redisKey);
    }

    @Override
    public List<NotificationBatch> findExpiredBatches() {
        List<NotificationBatch> expiredBatches = new ArrayList<>();

        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(KEY_PATTERN)
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                String redisKey = cursor.next();
                String json = redisTemplate.opsForValue().get(redisKey);

                if (json == null) {
                    // 키가 스캔 후 만료되었을 수 있음
                    continue;
                }

                try {
                    NotificationBatchDto dto = objectMapper.readValue(json, NotificationBatchDto.class);
                    NotificationBatch batch = dto.toBatch();

                    if (batch.isExpired()) {
                        expiredBatches.add(batch);
                    }

                } catch (JsonProcessingException e) {
                    log.error("Redis 키 역직렬화에 실패했습니다: {}", redisKey, e);
                }
            }
        } catch (Exception e) {
            log.error("만료된 배치 스캔에 실패했습니다.", e);
        }

        return expiredBatches;
    }
}
