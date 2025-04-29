package org.devkor.apu.saerok_server.domain.dex.habitat.entity;

import jakarta.persistence.*;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.entity.Bird;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"bird_id", "habitat_type"}
        )
)
public class BirdHabitat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bird_id")
    private Bird bird;

    @Enumerated(EnumType.STRING)
    @Column(name = "habitat_type")
    private HabitatType habitatType;
}
