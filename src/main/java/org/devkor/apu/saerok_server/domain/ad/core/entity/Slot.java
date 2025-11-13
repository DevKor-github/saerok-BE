package org.devkor.apu.saerok_server.domain.ad.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "fallback_ratio", nullable = false)
    private Double fallbackRatio;

    @Column(name = "ttl_seconds", nullable = false)
    private Integer ttlSeconds;

    private Slot(String name, Double fallbackRatio, Integer ttlSeconds) {
        this.name = name;
        this.fallbackRatio = fallbackRatio;
        this.ttlSeconds = ttlSeconds;
    }

    public static Slot create(String name, Double fallbackRatio, Integer ttlSeconds) {
        validateFallbackRatio(fallbackRatio);
        validateTtl(ttlSeconds);
        return new Slot(name, fallbackRatio, ttlSeconds);
    }

    public void update(Double fallbackRatio, Integer ttlSeconds) {
        if (fallbackRatio != null) {
            validateFallbackRatio(fallbackRatio);
            this.fallbackRatio = fallbackRatio;
        }
        if (ttlSeconds != null) {
            validateTtl(ttlSeconds);
            this.ttlSeconds = ttlSeconds;
        }
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
