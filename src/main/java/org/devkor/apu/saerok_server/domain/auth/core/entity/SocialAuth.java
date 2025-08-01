package org.devkor.apu.saerok_server.domain.auth.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;
import org.devkor.apu.saerok_server.global.security.crypto.EncryptedPayload;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SocialAuth extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private SocialProviderType provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Embedded
    private SocialAuthRefreshToken refreshToken;

    public static SocialAuth createSocialAuth(User user, SocialProviderType provider, String providerUserId) {
        SocialAuth socialAuth = new SocialAuth();
        socialAuth.user = user;
        socialAuth.provider = provider;
        socialAuth.providerUserId = providerUserId;
        return socialAuth;
    }

    public void setRefreshToken(EncryptedPayload p) {

        if (refreshToken == null) refreshToken = new SocialAuthRefreshToken();

        refreshToken.setCiphertext(p.ciphertext());
        refreshToken.setKey(p.encryptedDataKey());
        refreshToken.setIv(p.iv());
        refreshToken.setTag(p.authTag());
    }
}
