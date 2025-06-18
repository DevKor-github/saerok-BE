package org.devkor.apu.saerok_server.domain.dex.residency.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "rarity_type")
public class RarityTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false)
    private RarityType code;

    @Column(name = "priority", nullable = false)
    private int priority;
}
