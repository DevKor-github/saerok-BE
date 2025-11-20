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

    @Column(name = "popularity_score", nullable = false)
    private double popularityScore;

    @Column(name = "freshness_score", nullable = false)
    private double freshnessScore;

    @Column(name = "trending_score", nullable = false)
    private double trendingScore;

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public PopularCollection(
            UserBirdCollection collection,
            double popularityScore,
            double freshnessScore,
            double trendingScore,
            OffsetDateTime calculatedAt,
            int displayOrder
    ) {
        this.collection = collection;
        this.popularityScore = popularityScore;
        this.freshnessScore = freshnessScore;
        this.trendingScore = trendingScore;
        this.calculatedAt = calculatedAt;
        this.displayOrder = displayOrder;
    }
}
