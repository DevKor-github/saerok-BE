package org.devkor.apu.saerok_server.domain.dex.bird.domain.entity;

import jakarta.persistence.*;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.enums.HabitatType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "habitat_type", columnDefinition = "habitat_type_enum")
    private HabitatType habitatType;
}
