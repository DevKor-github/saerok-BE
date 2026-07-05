package org.devkor.apu.saerok_server.domain.dex.bird.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.HabitatType;

@Entity
@Getter
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

    public static BirdHabitat of(Bird bird, HabitatType habitatType) {
        BirdHabitat habitat = new BirdHabitat();
        habitat.bird = bird;
        habitat.habitatType = habitatType;
        return habitat;
    }
}
