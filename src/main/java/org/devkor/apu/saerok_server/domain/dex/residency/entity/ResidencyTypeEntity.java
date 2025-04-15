package org.devkor.apu.saerok_server.domain.dex.residency.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "residency_type")
public class ResidencyTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false)
    private ResidencyType code;

    @Column(name = "month_bitmask", nullable = false)
    private int monthBitmask;
}
