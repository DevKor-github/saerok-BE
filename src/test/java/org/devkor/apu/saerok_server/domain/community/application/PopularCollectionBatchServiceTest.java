package org.devkor.apu.saerok_server.domain.community.application;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.community.core.entity.PopularCollection;
import org.devkor.apu.saerok_server.domain.community.core.repository.PopularCollectionRepository;
import org.devkor.apu.saerok_server.domain.community.core.repository.TrendingCollectionRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.BirdBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.CollectionBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({PopularCollectionBatchService.class, PopularCollectionRepository.class, TrendingCollectionRepository.class})
@ActiveProfiles("test")
class PopularCollectionBatchServiceTest extends AbstractPostgresContainerTest {

    @Autowired
    PopularCollectionBatchService popularCollectionBatchService;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("배치가 트렌딩 스코어 기준으로 PopularCollection을 리프레시한다")
    void refreshPopularCollections_updatesSnapshot() {
        OffsetDateTime now = OffsetDateTime.now();
        User owner = new UserBuilder(em).nickname("owner").build();
        Bird bird = new BirdBuilder(em).build();

        UserBirdCollection freshCollection = new CollectionBuilder(em)
                .owner(owner)
                .accessLevel(AccessLevelType.PUBLIC)
                .build();
        ReflectionTestUtils.setField(freshCollection, "bird", bird);

        UserBirdCollection staleCollection = new CollectionBuilder(em)
                .owner(owner)
                .accessLevel(AccessLevelType.PUBLIC)
                .build();
        ReflectionTestUtils.setField(staleCollection, "bird", bird);

        UserBirdCollection inactiveCollection = new CollectionBuilder(em)
                .owner(owner)
                .accessLevel(AccessLevelType.PUBLIC)
                .build();
        ReflectionTestUtils.setField(inactiveCollection, "bird", bird);

        addLikeWithTimestamp(freshCollection, now.minusDays(1));
        addLikeWithTimestamp(freshCollection, now.minusDays(2));
        addCommentWithTimestamp(freshCollection, now.minusDays(1));
        addCommentWithTimestamp(freshCollection, now.minusDays(1));

        addLikeWithTimestamp(staleCollection, now.minusDays(15));
        addLikeWithTimestamp(staleCollection, now.minusDays(16));
        addCommentWithTimestamp(staleCollection, now.minusDays(20));

        em.flush();
        em.clear();

        popularCollectionBatchService.refreshPopularCollections();

        List<PopularCollection> popular = em.getEntityManager().createQuery(
                        "SELECT pc FROM PopularCollection pc ORDER BY pc.trendingScore DESC",
                        PopularCollection.class)
                .getResultList();

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getCollection().getId()).isEqualTo(freshCollection.getId());
        assertThat(popular.get(0).getTrendingScore()).isGreaterThan(0);
        assertThat(popular.get(0).getCalculatedAt()).isNotNull();
        assertThat(popular.get(1).getCollection().getId()).isEqualTo(staleCollection.getId());
    }

    private void addLikeWithTimestamp(UserBirdCollection collection, OffsetDateTime createdAt) {
        User liker = new UserBuilder(em).nickname("liker-" + createdAt.toEpochSecond()).build();
        UserBirdCollectionLike like = new UserBirdCollectionLike(liker, collection);
        em.persist(like);
        em.flush();
        updateCreatedAt("user_bird_collection_like", like.getId(), createdAt);
    }

    private void addCommentWithTimestamp(UserBirdCollection collection, OffsetDateTime createdAt) {
        User commenter = new UserBuilder(em).nickname("commenter-" + createdAt.toEpochSecond()).build();
        UserBirdCollectionComment comment = UserBirdCollectionComment.of(commenter, collection, "comment");
        em.persist(comment);
        em.flush();
        updateCreatedAt("user_bird_collection_comment", comment.getId(), createdAt);
    }

    private void updateCreatedAt(String tableName, Long id, OffsetDateTime createdAt) {
        em.getEntityManager().createNativeQuery(
                        "UPDATE " + tableName + " SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", id)
                .executeUpdate();
    }
}
