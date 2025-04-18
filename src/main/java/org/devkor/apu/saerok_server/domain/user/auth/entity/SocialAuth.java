package org.devkor.apu.saerok_server.domain.user.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.entity.CreatedAtOnly;

import java.time.OffsetDateTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAuth extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "last_login_at")
    @Setter
    private OffsetDateTime lastLoginAt;

}
