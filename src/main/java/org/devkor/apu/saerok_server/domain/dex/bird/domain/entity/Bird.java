package org.devkor.apu.saerok_server.domain.dex.bird.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.contract.HasBodyLength;
import org.devkor.apu.saerok_server.global.entity.Auditable;
import org.devkor.apu.saerok_server.global.entity.SoftDeletableAuditable;

import java.util.List;

@Entity
@Getter
public class Bird extends SoftDeletableAuditable implements HasBodyLength {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Embedded
    private BirdName name;

    @Embedded
    private BirdTaxonomy taxonomy;

    @Embedded
    @Getter
    private BirdDescription description;

    @Column(name = "body_length_cm")
    private Double bodyLengthCm;

    @Column(name = "nibr_url")
    private String nibrUrl;

    @OneToMany(mappedBy = "bird", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BirdImage> images;

    @Override
    public Double getBodyLengthCm() {
        return bodyLengthCm;
    }
}
