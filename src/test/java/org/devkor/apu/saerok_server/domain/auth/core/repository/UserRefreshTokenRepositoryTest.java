package org.devkor.apu.saerok_server.domain.auth.core.repository;

import org.devkor.apu.saerok_server.domain.auth.core.entity.UserRefreshToken;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(UserRefreshTokenRepository.class)
@ActiveProfiles("test")
class UserRefreshTokenRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired UserRefreshTokenRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private UserRefreshToken token(User user, String hash) {
        UserRefreshToken t = UserRefreshToken.create(
                user,
                hash,
                "Mozilla/5.0",
                "127.0.0.1",
                Duration.ofDays(30)
        );
        em.persist(t);
        return t;
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / findByRefreshTokenHash")
    void save_findByRefreshTokenHash() {
        User u = user();
        UserRefreshToken token = UserRefreshToken.create(
                u,
                "hash123",
                "Mozilla/5.0",
                "127.0.0.1",
                Duration.ofDays(30)
        );
        repo.save(token);
        em.flush(); em.clear();

        Optional<UserRefreshToken> found = repo.findByRefreshTokenHash("hash123");
        assertThat(found).isPresent();
        assertThat(found.get().getRefreshTokenHash()).isEqualTo("hash123");
        assertThat(found.get().getUser().getId()).isEqualTo(u.getId());
        assertThat(found.get().getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(found.get().getIpAddress()).isEqualTo("127.0.0.1");
    }

    @Test @DisplayName("findByRefreshTokenHash - 존재하지 않음")
    void findByRefreshTokenHash_notFound() {
        Optional<UserRefreshToken> found = repo.findByRefreshTokenHash("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("unique constraint - 같은 user와 hash 중복 불가")
    void uniqueConstraint_userAndHash() {
        User u = user();
        token(u, "duplicateHash");
        em.flush();

        UserRefreshToken duplicate = UserRefreshToken.create(
                u,
                "duplicateHash",
                "Chrome",
                "192.168.1.1",
                Duration.ofDays(30)
        );
        em.persist(duplicate);

        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(Exception.class);
    }

    @Test @DisplayName("deleteByUserId")
    void deleteByUserId() {
        User u1 = user();
        User u2 = user();

        token(u1, "hash1");
        token(u1, "hash2");
        token(u2, "hash3");
        em.flush(); em.clear();

        int deletedCount = repo.deleteByUserId(u1.getId());
        em.flush(); em.clear();

        assertThat(deletedCount).isEqualTo(2);
        assertThat(repo.findByRefreshTokenHash("hash1")).isEmpty();
        assertThat(repo.findByRefreshTokenHash("hash2")).isEmpty();
        assertThat(repo.findByRefreshTokenHash("hash3")).isPresent();
    }

    @Test @DisplayName("deleteByUserId - 토큰 없음")
    void deleteByUserId_noTokens() {
        User u = user();
        em.flush(); em.clear();

        int deletedCount = repo.deleteByUserId(u.getId());
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test @DisplayName("isUsable - 정상 토큰")
    void isUsable_valid() {
        User u = user();
        UserRefreshToken token = token(u, "validHash");
        em.flush(); em.clear();

        UserRefreshToken found = repo.findByRefreshTokenHash("validHash").orElseThrow();
        assertThat(found.isUsable()).isTrue();
    }

    @Test @DisplayName("isUsable - revoked된 토큰")
    void isUsable_revoked() {
        User u = user();
        UserRefreshToken token = token(u, "revokedHash");
        token.revoke();
        em.flush(); em.clear();

        UserRefreshToken found = repo.findByRefreshTokenHash("revokedHash").orElseThrow();
        assertThat(found.isUsable()).isFalse();
        assertThat(found.getRevokedAt()).isNotNull();
    }

    @Test @DisplayName("isUsable - 만료된 토큰")
    void isUsable_expired() {
        User u = user();
        UserRefreshToken token = UserRefreshToken.create(
                u,
                "expiredHash",
                "Mozilla/5.0",
                "127.0.0.1",
                Duration.ofMillis(-1000)  // 이미 만료됨
        );
        em.persist(token);
        em.flush(); em.clear();

        UserRefreshToken found = repo.findByRefreshTokenHash("expiredHash").orElseThrow();
        assertThat(found.isUsable()).isFalse();
    }

    @Test @DisplayName("revoke")
    void revoke() {
        User u = user();
        UserRefreshToken token = token(u, "toRevoke");
        em.flush(); em.clear();

        UserRefreshToken found = repo.findByRefreshTokenHash("toRevoke").orElseThrow();
        assertThat(found.getRevokedAt()).isNull();

        found.revoke();
        em.flush(); em.clear();

        UserRefreshToken revoked = repo.findByRefreshTokenHash("toRevoke").orElseThrow();
        assertThat(revoked.getRevokedAt()).isNotNull();
        assertThat(revoked.isUsable()).isFalse();
    }

    @Test @DisplayName("revoke - 이중 revoke 예외")
    void revoke_alreadyRevoked() {
        User u = user();
        UserRefreshToken token = token(u, "doubleRevoke");
        token.revoke();
        em.flush(); em.clear();

        UserRefreshToken found = repo.findByRefreshTokenHash("doubleRevoke").orElseThrow();
        assertThatThrownBy(() -> found.revoke())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 revoke된 리프레시 토큰");
    }

    @Test @DisplayName("같은 유저가 여러 디바이스에서 여러 토큰 보유 가능")
    void sameUser_multipleTokens() {
        User u = user();
        token(u, "device1");
        token(u, "device2");
        token(u, "device3");
        em.flush(); em.clear();

        assertThat(repo.findByRefreshTokenHash("device1")).isPresent();
        assertThat(repo.findByRefreshTokenHash("device2")).isPresent();
        assertThat(repo.findByRefreshTokenHash("device3")).isPresent();
    }
}
