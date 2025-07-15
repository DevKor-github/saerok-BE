package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "user_bird_collection_comment_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserBirdCollectionCommentLike extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bird_collection_comment_id", nullable = false)
    private UserBirdCollectionComment comment;

    public UserBirdCollectionCommentLike(User user, UserBirdCollectionComment comment) {
        this.user = user;
        this.comment = comment;
    }
}
