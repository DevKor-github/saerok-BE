package org.devkor.apu.saerok_server.domain.auth.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;

import java.time.Duration;
import java.time.OffsetDateTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "refresh_token_hash"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", length = 100, nullable = false)
    private String refreshTokenHash;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt = OffsetDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    @Setter
    private OffsetDateTime revokedAt;

    public static UserRefreshToken create(
            User user,
            String refreshTokenHash,
            String userAgent,
            String ipAddress,
            Duration validDuration
    ) {
        UserRefreshToken userRefreshToken = new UserRefreshToken();
        userRefreshToken.user = user;
        userRefreshToken.refreshTokenHash = refreshTokenHash;
        userRefreshToken.userAgent = userAgent;
        userRefreshToken.ipAddress = ipAddress;
        userRefreshToken.issuedAt = OffsetDateTime.now();
        userRefreshToken.expiresAt = userRefreshToken.issuedAt.plus(validDuration);
        userRefreshToken.revokedAt = null;
        return userRefreshToken;
    }

    public boolean isUsable() {
        return revokedAt == null && expiresAt.isAfter(OffsetDateTime.now());
    }

    public void revoke() {
        if (revokedAt != null) {
            throw new IllegalStateException("이미 revoke된 리프레시 토큰을 revoke하려고 시도했습니다");
        }

        revokedAt = OffsetDateTime.now();
    }
}
