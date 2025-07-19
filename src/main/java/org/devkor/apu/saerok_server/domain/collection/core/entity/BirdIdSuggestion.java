package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

@Entity
@Table(
        name = "bird_id_suggestion",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "user_id", "user_bird_collection_id", "bird_id"
        })
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BirdIdSuggestion extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bird_collection_id", nullable = false)
    private UserBirdCollection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bird_id", nullable = false)
    private Bird bird;

    public BirdIdSuggestion(User user, UserBirdCollection collection, Bird bird) {
        if (user == null) throw new IllegalArgumentException("user는 null일 수 없습니다.");
        if (collection == null) throw new IllegalArgumentException("collection은 null일 수 없습니다.");
        if (bird == null) throw new IllegalArgumentException("bird는 null일 수 없습니다.");
        this.user = user;
        this.collection = collection;
        this.bird = bird;
    }
}
