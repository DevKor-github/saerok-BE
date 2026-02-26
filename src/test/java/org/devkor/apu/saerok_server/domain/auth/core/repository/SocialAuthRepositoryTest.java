package org.devkor.apu.saerok_server.domain.auth.core.repository;

import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(SocialAuthRepository.class)
@ActiveProfiles("test")
class SocialAuthRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired SocialAuthRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private void socialAuth(User user, SocialProviderType provider, String providerUserId) {
        SocialAuth auth = SocialAuth.createSocialAuth(user, provider, providerUserId);
        em.persist(auth);
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / findByProviderAndProviderUserId")
    void save_findByProviderAndProviderUserId() {
        User u = user();
        SocialAuth auth = SocialAuth.createSocialAuth(u, SocialProviderType.KAKAO, "kakao123");
        repo.save(auth);
        em.flush(); em.clear();

        Optional<SocialAuth> found = repo.findByProviderAndProviderUserId(SocialProviderType.KAKAO, "kakao123");
        assertThat(found).isPresent();
        assertThat(found.get().getProvider()).isEqualTo(SocialProviderType.KAKAO);
        assertThat(found.get().getProviderUserId()).isEqualTo("kakao123");
        assertThat(found.get().getUser().getId()).isEqualTo(u.getId());
    }

    @Test @DisplayName("findByProviderAndProviderUserId - 존재하지 않음")
    void findByProviderAndProviderUserId_notFound() {
        Optional<SocialAuth> found = repo.findByProviderAndProviderUserId(SocialProviderType.KAKAO, "nonexistent");
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("findByProviderAndProviderUserId - provider가 다르면 조회 안 됨")
    void findByProviderAndProviderUserId_differentProvider() {
        User u = user();
        socialAuth(u, SocialProviderType.KAKAO, "user123");
        em.flush(); em.clear();

        Optional<SocialAuth> found = repo.findByProviderAndProviderUserId(SocialProviderType.APPLE, "user123");
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("unique constraint - 같은 provider와 providerUserId 중복 불가")
    void uniqueConstraint_providerAndProviderUserId() {
        User u1 = user();
        User u2 = user();

        socialAuth(u1, SocialProviderType.KAKAO, "duplicate123");
        em.flush();

        SocialAuth duplicate = SocialAuth.createSocialAuth(u2, SocialProviderType.KAKAO, "duplicate123");
        em.persist(duplicate);

        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(Exception.class);
    }

    @Test @DisplayName("findByUserId")
    void findByUserId() {
        User u1 = user();
        User u2 = user();

        socialAuth(u1, SocialProviderType.KAKAO, "kakao123");
        socialAuth(u1, SocialProviderType.APPLE, "apple123");
        socialAuth(u2, SocialProviderType.KAKAO, "kakao456");
        em.flush(); em.clear();

        List<SocialAuth> u1Auths = repo.findByUserId(u1.getId());
        List<SocialAuth> u2Auths = repo.findByUserId(u2.getId());

        assertThat(u1Auths).hasSize(2);
        assertThat(u1Auths).extracting(SocialAuth::getProvider)
                .containsExactlyInAnyOrder(SocialProviderType.KAKAO, SocialProviderType.APPLE);
        assertThat(u2Auths).hasSize(1);
        assertThat(u2Auths.getFirst().getProvider()).isEqualTo(SocialProviderType.KAKAO);
    }

    @Test @DisplayName("findByUserId - 빈 리스트")
    void findByUserId_empty() {
        User u = user();
        em.flush(); em.clear();

        List<SocialAuth> auths = repo.findByUserId(u.getId());
        assertThat(auths).isEmpty();
    }

    @Test @DisplayName("같은 유저가 여러 provider 사용 가능")
    void sameUser_multipleProviders() {
        User u = user();
        socialAuth(u, SocialProviderType.KAKAO, "kakao123");
        socialAuth(u, SocialProviderType.APPLE, "apple123");
        em.flush(); em.clear();

        Optional<SocialAuth> kakao = repo.findByProviderAndProviderUserId(SocialProviderType.KAKAO, "kakao123");
        Optional<SocialAuth> apple = repo.findByProviderAndProviderUserId(SocialProviderType.APPLE, "apple123");

        assertThat(kakao).isPresent();
        assertThat(apple).isPresent();
        assertThat(kakao.get().getUser().getId()).isEqualTo(apple.get().getUser().getId());
    }
}
