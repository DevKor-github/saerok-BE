package org.devkor.apu.saerok_server.domain.dex.bookmark.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.entity.Auditable;

@Entity
@Table(
        name = "user_bird_bookmark",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "bird_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBirdBookmark extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bird_id", nullable = false)
    private Bird bird;

}
