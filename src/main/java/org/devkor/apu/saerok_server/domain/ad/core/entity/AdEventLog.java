package org.devkor.apu.saerok_server.domain.ad.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

@Entity
@Table(name = "ad_event_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AdEventLog extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @Column(name = "slot_name", nullable = false, length = 100)
    private String slotName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 16)
    private AdEventType eventType;

    @Column(name = "device_hash", nullable = false, length = 64)
    private String deviceHash;

    private AdEventLog(Ad ad,
                       String slotName,
                       AdEventType eventType,
                       String deviceHash) {
        this.ad = ad;
        this.slotName = slotName;
        this.eventType = eventType;
        this.deviceHash = deviceHash;
    }

    public static AdEventLog of(Ad ad,
                                String slotName,
                                AdEventType eventType,
                                String deviceHash) {
        return new AdEventLog(ad, slotName, eventType, deviceHash);
    }
}
