package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

/**
 * 동정 의견.
 * user가 collection에 대해 bird라는 의견을 제시함.
 * type에 따라 제안(SUGGEST), 동의(AGREE), 비동의(DISAGREE)를 구분
 */
@Entity
@Table(
        name = "bird_id_suggestion",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "user_id", "user_bird_collection_id", "bird_id", "type"
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SuggestionType type;

    public BirdIdSuggestion(User user, UserBirdCollection collection, Bird bird, SuggestionType type) {
        if (user == null) throw new IllegalArgumentException("user는 null일 수 없습니다.");
        if (collection == null) throw new IllegalArgumentException("collection은 null일 수 없습니다.");
        if (bird == null) throw new IllegalArgumentException("bird는 null일 수 없습니다.");
        if (type == null) throw new IllegalArgumentException("type은 null일 수 없습니다.");
        this.user = user;
        this.collection = collection;
        this.bird = bird;
        this.type = type;
    }

    public enum SuggestionType {
        SUGGEST,    // 제안
        AGREE,      // 동의
        DISAGREE    // 비동의
    }
}
