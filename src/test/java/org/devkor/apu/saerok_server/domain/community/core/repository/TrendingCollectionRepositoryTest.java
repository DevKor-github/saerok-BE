package org.devkor.apu.saerok_server.domain.community.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.community.core.repository.dto.TrendingCollectionCandidate;
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
@Import(TrendingCollectionRepository.class)
@ActiveProfiles("test")
class TrendingCollectionRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired TrendingCollectionRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private UserBirdCollection collection(User owner, AccessLevelType accessLevel) {
        return new CollectionBuilder(em).owner(owner).accessLevel(accessLevel).build();
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("findRecentPublicCollections filters by access and date")
    void findRecentPublicCollections_filtersByAccessAndDate() throws InterruptedException {
        User owner = user();
        UserBirdCollection oldPublic = collection(owner, AccessLevelType.PUBLIC);
        em.flush();

        OffsetDateTime createdAfter = OffsetDateTime.now();
        Thread.sleep(10);

        UserBirdCollection newPublic = collection(owner, AccessLevelType.PUBLIC);
        UserBirdCollection newPrivate = collection(owner, AccessLevelType.PRIVATE);
        em.flush(); em.clear();

        List<TrendingCollectionCandidate> result = repo.findRecentPublicCollections(createdAfter);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().collectionId()).isEqualTo(newPublic.getId());
        assertThat(result).noneMatch(c -> c.collectionId().equals(oldPublic.getId()));
        assertThat(result).noneMatch(c -> c.collectionId().equals(newPrivate.getId()));
    }

    @Test @DisplayName("findLikeCreatedAtByCollectionIds")
    void findLikeCreatedAtByCollectionIds() {
        User owner = user();
        User liker1 = user();
        User liker2 = user();
        UserBirdCollection c1 = collection(owner, AccessLevelType.PUBLIC);
        UserBirdCollection c2 = collection(owner, AccessLevelType.PUBLIC);

        em.persist(new UserBirdCollectionLike(liker1, c1));
        em.persist(new UserBirdCollectionLike(liker2, c1));
        em.persist(new UserBirdCollectionLike(liker1, c2));
        em.flush(); em.clear();

        Map<Long, List<OffsetDateTime>> result =
                repo.findLikeCreatedAtByCollectionIds(List.of(c1.getId(), c2.getId()));

        assertThat(result).containsKeys(c1.getId(), c2.getId());
        assertThat(result.get(c1.getId())).hasSize(2);
        assertThat(result.get(c2.getId())).hasSize(1);
    }

    @Test @DisplayName("findLastCommentAtByCollectionIds")
    void findLastCommentAtByCollectionIds() throws InterruptedException {
        User owner = user();
        User commenter = user();
        UserBirdCollection c1 = collection(owner, AccessLevelType.PUBLIC);
        UserBirdCollection c2 = collection(owner, AccessLevelType.PUBLIC);

        UserBirdCollectionComment first = UserBirdCollectionComment.of(commenter, c1, "first");
        em.persist(first);
        em.flush();

        Thread.sleep(10);

        UserBirdCollectionComment second = UserBirdCollectionComment.of(commenter, c1, "second");
        em.persist(second);
        em.persist(UserBirdCollectionComment.of(commenter, c2, "other"));
        em.flush(); em.clear();

        Map<Long, Map<Long, OffsetDateTime>> result =
                repo.findLastCommentAtByCollectionIds(List.of(c1.getId(), c2.getId()));

        assertThat(result).containsKeys(c1.getId(), c2.getId());
        assertThat(result.get(c1.getId()).get(commenter.getId())).isEqualTo(second.getCreatedAt());
    }
}
