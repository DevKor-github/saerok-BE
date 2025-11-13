package org.devkor.apu.saerok_server.domain.ad.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(
        name = "ad_slot",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_ad_slot_name",
                columnNames = "name"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Slot extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * 슬롯 이름 (예: HOME_TOP) - 유니크
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 관리자용 메모 (optional)
     */
    @Column(name = "memo")
    private String memo;

    /**
     * 기본 광고(fallback)로 폴백할 확률 (0.0 ~ 1.0)
     */
    @Column(name = "fallback_ratio", nullable = false)
    private Double fallbackRatio;

    /**
     * 클라이언트가 동일 광고를 유지해야 하는 TTL(초)
     */
    @Column(name = "ttl_seconds", nullable = false)
    private Integer ttlSeconds;

    private Slot(String name, String memo, Double fallbackRatio, Integer ttlSeconds) {
        this.name = name;
        this.memo = memo;
        this.fallbackRatio = fallbackRatio;
        this.ttlSeconds = ttlSeconds;
    }

    public static Slot create(String name, String memo, Double fallbackRatio, Integer ttlSeconds) {
        validateFallbackRatio(fallbackRatio);
        validateTtl(ttlSeconds);
        return new Slot(name, memo, fallbackRatio, ttlSeconds);
    }

    public void update(String memo, Double fallbackRatio, Integer ttlSeconds) {
        if (fallbackRatio != null) {
            validateFallbackRatio(fallbackRatio);
            this.fallbackRatio = fallbackRatio;
        }
        if (ttlSeconds != null) {
            validateTtl(ttlSeconds);
            this.ttlSeconds = ttlSeconds;
        }
        this.memo = memo;
    }

    private static void validateFallbackRatio(Double value) {
        if (value == null || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("fallbackRatio는 0.0 이상 1.0 이하 값이어야 합니다.");
        }
    }

    private static void validateTtl(Integer ttlSeconds) {
        if (ttlSeconds == null || ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds는 1 이상이어야 합니다.");
        }
    }
}
