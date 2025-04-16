package org.devkor.apu.saerok_server.domain.dex.bird.entity;

import jakarta.persistence.*;
import org.devkor.apu.saerok_server.global.entity.Auditable;

@Entity
public class Bird extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Embedded
    private BirdName name;

    @Embedded
    private BirdTaxonomy taxonomy;

    @Embedded
    private BirdDescription description;

    @Column(name = "body_length_cm")
    private Double bodyLengthCm;

    @Column(name = "nibr_url")
    private String nibrUrl;
}
