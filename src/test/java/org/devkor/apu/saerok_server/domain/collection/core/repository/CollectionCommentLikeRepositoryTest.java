package org.devkor.apu.saerok_server.domain.collection.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.*;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CollectionCommentLikeRepository.class)
@ActiveProfiles("test")
class CollectionCommentLikeRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired CollectionCommentLikeRepository repo;
    @Autowired TestEntityManager em;

    private final GeometryFactory gf = new GeometryFactory();
    private Field collUserField;

    /* ------------------------------------------------------------------
     * helpers
     * ------------------------------------------------------------------ */
    @BeforeEach
    void setup() throws NoSuchFieldException {
        collUserField = UserBirdCollection.class.getDeclaredField("user");
        collUserField.setAccessible(true);
    }

    private User newUser() {
        User user = User.createUser("test+" + System.nanoTime() + "@example.com");
        em.persist(user);
        em.flush();
        return user;
    }

    private UserBirdCollection newCollection(User owner) {
        try {
            UserBirdCollection c = new UserBirdCollection();
            collUserField.set(c, owner);

            c.setAccessLevel(AccessLevelType.PUBLIC);
            c.setDiscoveredDate(LocalDate.now());
            Point p = gf.createPoint(new Coordinate(126.9780, 37.5665));
            c.setLocation(p);

            em.persist(c);
            return c;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private UserBirdCollectionComment newComment(User user, UserBirdCollection col, String content) {
        UserBirdCollectionComment cm = UserBirdCollectionComment.of(user, col, content);
        em.persist(cm);
        return cm;
    }

    private UserBirdCollectionCommentLike newLike(User user, UserBirdCollectionComment cm) {
        UserBirdCollectionCommentLike like = new UserBirdCollectionCommentLike(user, cm);
        em.persist(like);
        return like;
    }

    /* ------------------------------------------------------------------
     * tests
     * ------------------------------------------------------------------ */

    @Test @DisplayName("save & findByUserIdAndCommentId")
    void save_and_find() {
        User u = newUser();
        UserBirdCollection col = newCollection(u);
        UserBirdCollectionComment cm = newComment(u, col, "hi");

        UserBirdCollectionCommentLike like = new UserBirdCollectionCommentLike(u, cm);
        repo.save(like);
        em.flush(); em.clear();

        Optional<UserBirdCollectionCommentLike> found =
                repo.findByUserIdAndCommentId(u.getId(), cm.getId());

        assertTrue(found.isPresent());
        assertEquals(u.getId(), found.get().getUser().getId());
        assertEquals(cm.getId(), found.get().getComment().getId());
    }

    @Test @DisplayName("existsByUserIdAndCommentId")
    void exists() {
        User u = newUser();
        UserBirdCollection col = newCollection(u);
        UserBirdCollectionComment cm = newComment(u, col, "hi");

        assertFalse(repo.existsByUserIdAndCommentId(u.getId(), cm.getId()));

        newLike(u, cm);
        em.flush(); em.clear();

        assertTrue(repo.existsByUserIdAndCommentId(u.getId(), cm.getId()));
    }

    @Test @DisplayName("remove")
    void remove() {
        User u = newUser();
        UserBirdCollection col = newCollection(u);
        UserBirdCollectionComment cm = newComment(u, col, "hi");

        UserBirdCollectionCommentLike like = newLike(u, cm);
        em.flush(); em.clear();

        repo.remove(em.find(UserBirdCollectionCommentLike.class, like.getId()));
        em.flush(); em.clear();

        assertFalse(repo.existsByUserIdAndCommentId(u.getId(), cm.getId()));
    }

    @Test @DisplayName("countByCommentId")
    void count_single() {
        User u1 = newUser();
        User u2 = newUser();
        UserBirdCollection col = newCollection(u1);
        UserBirdCollectionComment cm = newComment(u1, col, "hi");

        newLike(u1, cm);
        newLike(u2, cm);
        em.flush(); em.clear();

        assertEquals(2L, repo.countByCommentId(cm.getId()));
    }

    @Test @DisplayName("countLikesByCommentIds – 0 포함")
    void count_batch_with_zeros() {
        User u = newUser();
        UserBirdCollection col = newCollection(u);

        UserBirdCollectionComment cm1 = newComment(u, col, "A"); // 1 like
        UserBirdCollectionComment cm2 = newComment(u, col, "B"); // 0 like
        UserBirdCollectionComment cm3 = newComment(u, col, "C"); // 0 like

        newLike(u, cm1);
        em.flush(); em.clear();

        Map<Long, Long> counts = repo.countLikesByCommentIds(
                List.of(cm1.getId(), cm2.getId(), cm3.getId()));

        assertEquals(1L, counts.get(cm1.getId()));
        assertEquals(0L, counts.get(cm2.getId()));
        assertEquals(0L, counts.get(cm3.getId()));
    }

    @Test @DisplayName("findLikeStatusByUserIdAndCommentIds")
    void like_status_batch() {
        User liker = newUser();
        User other = newUser();
        UserBirdCollection col = newCollection(liker);

        UserBirdCollectionComment cm1 = newComment(other, col, "A");
        UserBirdCollectionComment cm2 = newComment(other, col, "B");

        newLike(liker, cm1);
        em.flush(); em.clear();

        Map<Long, Boolean> status = repo.findLikeStatusByUserIdAndCommentIds(
                liker.getId(), List.of(cm1.getId(), cm2.getId()));

        assertTrue(status.get(cm1.getId()));
        assertFalse(status.get(cm2.getId()));
    }
}
