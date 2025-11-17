// ===== java/org/devkor/apu/saerok_server/domain/stat/core/entity/DailyStat.java =====
package org.devkor.apu.saerok_server.domain.admin.stat.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "daily_stat",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_stat_metric_date", columnNames = {"metric", "date"}))
@Getter
@NoArgsConstructor
public class DailyStat extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric", nullable = false, length = 64)
    private StatMetric metric;

    @Column(name = "date", nullable = false)
    private LocalDate date; // KST 기준 날짜

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload = new HashMap<>();

    private DailyStat(StatMetric metric, LocalDate date, Map<String, Object> payload) {
        this.metric = metric;
        this.date = date;
        if (payload != null) this.payload.putAll(payload);
    }

    public static DailyStat ofPayload(StatMetric metric, LocalDate date, Map<String, Object> payload) {
        return new DailyStat(metric, date, payload);
    }

    public static DailyStat ofValue(StatMetric metric, LocalDate date, Number value) {
        Map<String, Object> p = new HashMap<>();
        p.put("value", value);
        return new DailyStat(metric, date, p);
    }

    public void overwritePayload(Map<String, Object> newPayload) {
        this.payload.clear();
        if (newPayload != null) this.payload.putAll(newPayload);
    }
}
