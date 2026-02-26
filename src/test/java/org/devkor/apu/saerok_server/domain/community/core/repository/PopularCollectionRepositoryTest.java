package org.devkor.apu.saerok_server.domain.community.core.repository;

import org.devkor.apu.saerok_server.domain.community.core.entity.PopularCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.CollectionBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(PopularCollectionRepository.class)
@ActiveProfiles("test")
class PopularCollectionRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired PopularCollectionRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private UserBirdCollection collection(User owner) {
        return new CollectionBuilder(em).owner(owner).build();
    }

    private PopularCollection popular(UserBirdCollection collection, int order) {
        return new PopularCollection(
                collection,
                1.0,
                2.0,
                3.0,
                OffsetDateTime.now(),
                order
        );
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / existsByCollectionId")
    void save_existsByCollectionId() {
        User owner = user();
        UserBirdCollection collection = collection(owner);
        repo.save(popular(collection, 1));
        em.flush(); em.clear();

        assertThat(repo.existsByCollectionId(collection.getId())).isTrue();
        assertThat(repo.existsByCollectionId(999999L)).isFalse();
    }

    @Test @DisplayName("existsByCollectionIds returns map for input ids")
    void existsByCollectionIds_returnsMap() {
        User owner = user();
        UserBirdCollection c1 = collection(owner);
        UserBirdCollection c2 = collection(owner);
        UserBirdCollection c3 = collection(owner);
        repo.save(popular(c1, 1));
        repo.save(popular(c3, 2));
        em.flush(); em.clear();

        Map<Long, Boolean> result = repo.existsByCollectionIds(List.of(c1.getId(), c2.getId(), c3.getId()));

        assertThat(result).containsEntry(c1.getId(), true);
        assertThat(result).containsEntry(c2.getId(), false);
        assertThat(result).containsEntry(c3.getId(), true);
    }

    @Test @DisplayName("deleteAll")
    void deleteAll() {
        User owner = user();
        UserBirdCollection c1 = collection(owner);
        UserBirdCollection c2 = collection(owner);
        repo.saveAll(List.of(popular(c1, 1), popular(c2, 2)));
        em.flush(); em.clear();

        repo.deleteAll();
        em.flush(); em.clear();

        assertThat(repo.existsByCollectionId(c1.getId())).isFalse();
        assertThat(repo.existsByCollectionId(c2.getId())).isFalse();
    }
}
