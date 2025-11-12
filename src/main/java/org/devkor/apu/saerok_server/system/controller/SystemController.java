package org.devkor.apu.saerok_server.system.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Hidden
@Tag(name = "System API", description = "시스템 상태 점검 관련 API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class SystemController {

    private final StringRedisTemplate redisTemplate;


    @GetMapping("/health")
    @PermitAll
    @Operation(
            summary = "시스템 헬스 체크",
            description = "시스템이 살아있는지 확인합니다. (ELB 헬스 체크 대응)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답")
            }
    )
    public String ping() {
        return "I am healthy";
    }

    @GetMapping("/internal/redis")
    @PermitAll
    @Hidden
    @Operation(
            summary = "Redis 동작 확인 (internal)",
            description = "Redis ping 및 set/get 검증을 수행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답")
            }
    )
    public ResponseEntity<Map<String, Object>> redisHealth() {
        Map<String, Object> body = new HashMap<>();
        Instant start = Instant.now();
        String ping = null;
        boolean kvOk = false;

        try (RedisConnection conn = redisTemplate.getRequiredConnectionFactory().getConnection()) {
            // ping
            ping = conn.ping();
        } catch (Exception ex) {
            body.put("ok", false);
            body.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }

        // set/get with TTL
        String key = "system:health:redis:" + UUID.randomUUID();
        try {
            redisTemplate.opsForValue().set(key, "ok", Duration.ofSeconds(5));
            String val = redisTemplate.opsForValue().get(key);
            kvOk = "ok".equals(val);
            redisTemplate.delete(key);
        } catch (Exception ex) {
            body.put("ok", false);
            body.put("ping", ping);
            body.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }

        long latencyMillis = java.time.Duration.between(start, Instant.now()).toMillis();
        body.put("ok", "PONG".equalsIgnoreCase(ping) && kvOk);
        body.put("ping", ping);
        body.put("kv", kvOk);
        body.put("latencyMs", latencyMillis);
        body.put("now", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

}
