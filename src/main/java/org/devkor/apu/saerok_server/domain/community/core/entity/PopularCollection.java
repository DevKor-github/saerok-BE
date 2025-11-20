package org.devkor.apu.saerok_server.domain.community.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

import java.time.OffsetDateTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_bird_collection_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PopularCollection extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bird_collection_id", nullable = false)
    private UserBirdCollection collection;

    @Column(name = "trending_score", nullable = false)
    private double trendingScore;

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt;

    public PopularCollection(UserBirdCollection collection, double trendingScore, OffsetDateTime calculatedAt) {
        this.collection = collection;
        this.trendingScore = trendingScore;
        this.calculatedAt = calculatedAt;
    }
}
