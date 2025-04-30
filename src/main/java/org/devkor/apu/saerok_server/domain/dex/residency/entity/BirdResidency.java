package org.devkor.apu.saerok_server.domain.dex.residency.entity;

import jakarta.persistence.*;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.entity.Bird;
import org.devkor.apu.saerok_server.global.entity.Auditable;

@Entity
public class BirdResidency extends Auditable {

    @Id
    @GeneratedValue
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
}
