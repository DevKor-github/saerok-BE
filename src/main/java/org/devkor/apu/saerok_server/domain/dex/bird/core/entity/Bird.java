package org.devkor.apu.saerok_server.domain.dex.bird.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.devkor.apu.saerok_server.domain.dex.bird.core.contract.HasBodyLength;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.ConservationGrade;
import org.devkor.apu.saerok_server.global.shared.entity.SoftDeletableAuditable;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "conservation_grade", nullable = false)
    private ConservationGrade conservationGrade = ConservationGrade.NONE;

    @OneToMany(mappedBy = "bird", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BirdImage> images;

    public static Bird create(BirdName name,
                              BirdTaxonomy taxonomy,
                              BirdDescription description,
                              Double bodyLengthCm,
                              String nibrUrl,
                              ConservationGrade conservationGrade) {
        Bird bird = new Bird();
        bird.name = name;
        bird.taxonomy = taxonomy;
        bird.description = description;
        bird.bodyLengthCm = bodyLengthCm;
        bird.nibrUrl = nibrUrl;
        bird.conservationGrade = conservationGrade == null ? ConservationGrade.NONE : conservationGrade;
        return bird;
    }

    public void update(BirdName name,
                       BirdTaxonomy taxonomy,
                       BirdDescription description,
                       Double bodyLengthCm,
                       String nibrUrl,
                       ConservationGrade conservationGrade) {
        this.name = name;
        this.taxonomy = taxonomy;
        this.description = description;
        this.bodyLengthCm = bodyLengthCm;
        this.nibrUrl = nibrUrl;
        this.conservationGrade = conservationGrade == null ? ConservationGrade.NONE : conservationGrade;
    }

    @Override
    public Double getBodyLengthCm() {
        return bodyLengthCm;
    }
}
