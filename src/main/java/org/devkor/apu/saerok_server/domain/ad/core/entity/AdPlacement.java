package org.devkor.apu.saerok_server.domain.ad.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

import java.time.LocalDate;

@Entity
@Table(name = "ad_placement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AdPlacement extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "weight", nullable = false)
    private Short weight;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    private AdPlacement(Ad ad,
                        Slot slot,
                        LocalDate startDate,
                        LocalDate endDate,
                        Short weight,
                        Boolean enabled) {
        this.ad = ad;
        this.slot = slot;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weight = weight;
        this.enabled = enabled != null ? enabled : Boolean.TRUE;
    }

    public static AdPlacement create(Ad ad,
                                     Slot slot,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     Short weight,
                                     Boolean enabled) {
        validateDateRange(startDate, endDate);
        validateWeight(weight);
        return new AdPlacement(ad, slot, startDate, endDate, weight, enabled);
    }

    public void update(Slot slot,
                       LocalDate startDate,
                       LocalDate endDate,
                       Short weight,
                       Boolean enabled) {
        if (slot != null) {
            this.slot = slot;
        }
        if (startDate != null || endDate != null) {
            LocalDate newStart = startDate != null ? startDate : this.startDate;
            LocalDate newEnd = endDate != null ? endDate : this.endDate;
            validateDateRange(newStart, newEnd);
            this.startDate = newStart;
            this.endDate = newEnd;
        }
        if (weight != null) {
            validateWeight(weight);
            this.weight = weight;
        }
        if (enabled != null) {
            this.enabled = enabled;
        }
    }

    private static void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("startDate와 endDate는 null일 수 없습니다.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("endDate는 startDate보다 이전일 수 없습니다.");
        }
    }

    private static void validateWeight(Short weight) {
        if (weight == null || weight < 1 || weight > 5) {
            throw new IllegalArgumentException("weight는 1 이상 5 이하의 값이어야 합니다.");
        }
    }
}
