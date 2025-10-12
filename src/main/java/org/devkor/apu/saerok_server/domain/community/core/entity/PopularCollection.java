package org.devkor.apu.saerok_server.domain.community.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

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

    public PopularCollection(UserBirdCollection collection) {
        this.collection = collection;
    }
}
