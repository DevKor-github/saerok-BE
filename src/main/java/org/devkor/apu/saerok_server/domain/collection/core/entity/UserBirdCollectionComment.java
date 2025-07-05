package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(name = "content", nullable = false)
    private String content;
}
