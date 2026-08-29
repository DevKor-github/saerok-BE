package org.devkor.apu.saerok_server.domain.dex.residency.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Getter
public class BirdResidency extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bird_id")
    private Bird bird;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residency_type_id")
    private ResidencyTypeEntity residencyTypeEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rarity_type_id")
    private RarityTypeEntity rarityTypeEntity;

    @Column(name = "month_bitmask")
    private Integer monthBitmask;

    public static BirdResidency of(Bird bird,
                                   ResidencyTypeEntity residencyTypeEntity,
                                   RarityTypeEntity rarityTypeEntity,
                                   Integer monthBitmask) {
        BirdResidency residency = new BirdResidency();
        residency.bird = bird;
        residency.residencyTypeEntity = residencyTypeEntity;
        residency.rarityTypeEntity = rarityTypeEntity;
        residency.monthBitmask = monthBitmask;
        return residency;
    }
}
