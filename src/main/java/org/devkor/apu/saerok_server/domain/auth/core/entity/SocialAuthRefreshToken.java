package org.devkor.apu.saerok_server.domain.auth.core.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 소셜 공급자로부터 받은 리프레시 토큰을 AES-256-GCM 방식으로 양방향 암호화해서 관리
 */
@Embeddable
@Data
public class SocialAuthRefreshToken {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "refresh_token_ciphertext", columnDefinition = "BYTEA")
    private byte[] ciphertext;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "refresh_token_key", columnDefinition = "BYTEA")
    private byte[] key;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "refresh_token_iv", columnDefinition = "BYTEA")
    private byte[] iv;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "refresh_token_tag", columnDefinition = "BYTEA")
    private byte[] tag;
}
