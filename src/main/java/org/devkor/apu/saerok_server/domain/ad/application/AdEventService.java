package org.devkor.apu.saerok_server.domain.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdEventLog;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdEventType;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdEventLogRepository;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class AdEventService {

    private static final Duration DEDUP_TTL = Duration.ofSeconds(10);

    private final AdRepository adRepository;
    private final AdEventLogRepository adEventLogRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public void logEvent(AdEventType eventType,
                         Long adId,
                         String slotName,
                         String deviceId) {
        if (adId == null) {
            throw new BadRequestException("adId는 필수입니다.");
        }
        if (slotName == null || slotName.isBlank()) {
            throw new BadRequestException("slotName은 필수입니다.");
        }
        if (deviceId == null || deviceId.isBlank()) {
            throw new BadRequestException("deviceId는 필수입니다.");
        }

        String deviceHash = sha256Hex(deviceId);
        String key = buildRedisKey(eventType, deviceHash, adId, slotName);

        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", DEDUP_TTL);

        if (Boolean.FALSE.equals(success)) {
            // 10초 내 중복 이벤트 → 무시
            return;
        }

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 id예요."));

        AdEventLog log = AdEventLog.of(ad, slotName, eventType, deviceHash);
        adEventLogRepository.save(log);
    }

    private String buildRedisKey(AdEventType type,
                                 String deviceHash,
                                 Long adId,
                                 String slotName) {
        return "ad:event:" + type.name() + ":" + deviceHash + ":" + adId + ":" + slotName;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 환경 문제이므로 런타임 예외로 래핑
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
