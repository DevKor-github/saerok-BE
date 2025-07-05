package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Getter
@NoArgsConstructor
public class UserBirdCollectionComment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bird_collection_id", nullable = false)
    private UserBirdCollection collection;

    @Setter
    @Column(name = "content", nullable = false)
    private String content;

    public static UserBirdCollectionComment of(User user, UserBirdCollection collection, String content) {
        UserBirdCollectionComment comment = new UserBirdCollectionComment();
        comment.user = user;
        comment.collection = collection;
        comment.content = content;

        return comment;
    }
}
