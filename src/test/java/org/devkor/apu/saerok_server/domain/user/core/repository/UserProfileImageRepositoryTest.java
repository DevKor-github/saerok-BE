package org.devkor.apu.saerok_server.domain.user.core.repository;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(UserProfileImageRepository.class)
@ActiveProfiles("test")
class UserProfileImageRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired UserProfileImageRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private UserProfileImage image(User user, String objectKey) {
        UserProfileImage img = UserProfileImage.of(user, objectKey, "image/jpeg");
        repo.save(img);
        return img;
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / findByUserId")
    void save_findByUserId() {
        User user = user();
        UserProfileImage img = image(user, "profiles/test-1.jpg");
        em.flush(); em.clear();

        Optional<UserProfileImage> found = repo.findByUserId(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(img.getId());
        assertThat(found.get().getObjectKey()).isEqualTo("profiles/test-1.jpg");
    }

    @Test @DisplayName("findObjectKeyByUserId")
    void findObjectKeyByUserId() {
        User user = user();
        User missingUser = user();
        image(user, "profiles/test-2.jpg");
        em.flush(); em.clear();

        Optional<String> key = repo.findObjectKeyByUserId(user.getId());
        Optional<String> missing = repo.findObjectKeyByUserId(missingUser.getId());

        assertThat(key).contains("profiles/test-2.jpg");
        assertThat(missing).isEmpty();
    }

    @Test @DisplayName("findObjectKeysByUserIds maps missing users to null")
    void findObjectKeysByUserIds_mapsMissingToNull() {
        User user1 = user();
        User user2 = user();
        User user3 = user();
        image(user1, "profiles/user1.jpg");
        image(user3, "profiles/user3.jpg");
        em.flush(); em.clear();

        Map<Long, String> result = repo.findObjectKeysByUserIds(
                List.of(user1.getId(), user2.getId(), user3.getId())
        );

        assertThat(result).hasSize(3);
        assertThat(result.get(user1.getId())).isEqualTo("profiles/user1.jpg");
        assertThat(result.get(user2.getId())).isNull();
        assertThat(result.get(user3.getId())).isEqualTo("profiles/user3.jpg");
    }

    @Test @DisplayName("findObjectKeysByUserIds - 입력이 empty")
    void findObjectKeysByUserIds_emptyInput() {
        Map<Long, String> result = repo.findObjectKeysByUserIds(List.of());
        assertThat(result).isEmpty();
    }

    @Test @DisplayName("remove")
    void remove() {
        User user = user();
        UserProfileImage img = image(user, "profiles/remove.jpg");
        em.flush(); em.clear();

        UserProfileImage toDelete = em.find(UserProfileImage.class, img.getId());
        repo.remove(toDelete);
        em.flush(); em.clear();

        Optional<UserProfileImage> found = repo.findByUserId(user.getId());
        assertThat(found).isEmpty();
    }
}
