package org.devkor.apu.saerok_server.domain.collection.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CollectionLikeRepository.class)
@ActiveProfiles("test")
class CollectionLikeRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    CollectionLikeRepository collectionLikeRepository;

    @Autowired
    TestEntityManager em;

    GeometryFactory gf;
    Field collUserField;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        gf = new GeometryFactory();

        collUserField = UserBirdCollection.class.getDeclaredField("user");
        collUserField.setAccessible(true);
    }

    /* ------------------------------------------------------------------
     * helpers
     * ------------------------------------------------------------------ */
    private User newUser() {
        User user = User.createUser("test+" + System.nanoTime() + "@example.com");
        user.setNickname("user-" + user.hashCode());
        em.persist(user);
        em.flush();
        return user;
    }

    private UserBirdCollection newCollection(User owner) throws IllegalAccessException {
        UserBirdCollection c = new UserBirdCollection();
        collUserField.set(c, owner);
        c.setAccessLevel(AccessLevelType.PUBLIC);
        c.setDiscoveredDate(LocalDate.now());
        Point location = gf.createPoint(new Coordinate(126.9780, 37.5665));
        c.setLocation(location);
        em.persist(c);
        return c;
    }

    private UserBirdCollectionLike newLike(User user, UserBirdCollection collection) {
        UserBirdCollectionLike like = new UserBirdCollectionLike(user, collection);
        em.persist(like);
        return like;
    }

    /* ------------------------------------------------------------------
     * tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("좋아요 저장과 조회")
    void save_and_findByUserIdAndCollectionId() throws Exception {
        // given
        User user = newUser();
        UserBirdCollection collection = newCollection(user);

        UserBirdCollectionLike like = new UserBirdCollectionLike(user, collection);

        // when
        collectionLikeRepository.save(like);
        em.flush();
        em.clear();

        // then
        Optional<UserBirdCollectionLike> found =
                collectionLikeRepository.findByUserIdAndCollectionId(user.getId(), collection.getId());

        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getUser().getId());
        assertEquals(collection.getId(), found.get().getCollection().getId());
    }

    @Test
    @DisplayName("좋아요 존재 여부 확인")
    void existsByUserIdAndCollectionId() throws Exception {
        // given
        User user = newUser();
        UserBirdCollection collection = newCollection(user);

        // 좋아요가 없을 때
        assertFalse(collectionLikeRepository.existsByUserIdAndCollectionId(user.getId(), collection.getId()));

        // 좋아요 추가 후
        newLike(user, collection);
        em.flush();
        em.clear();

        // then
        assertTrue(collectionLikeRepository.existsByUserIdAndCollectionId(user.getId(), collection.getId()));
    }

    @Test
    @DisplayName("좋아요 제거")
    void remove() throws Exception {
        // given
        User user = newUser();
        UserBirdCollection collection = newCollection(user);
        UserBirdCollectionLike like = newLike(user, collection);

        em.flush();
        em.clear();

        // when
        UserBirdCollectionLike managedLike = em.find(UserBirdCollectionLike.class, like.getId());
        collectionLikeRepository.remove(managedLike);
        em.flush();
        em.clear();

        // then
        assertFalse(collectionLikeRepository.existsByUserIdAndCollectionId(user.getId(), collection.getId()));
    }

    @Test
    @DisplayName("컬렉션을 좋아요한 사용자 목록 조회")
    void findLikersByCollectionId() throws Exception {
        // given
        User user1 = newUser();
        User user2 = newUser();
        User user3 = newUser();
        UserBirdCollection collection = newCollection(user1);
        UserBirdCollection otherCollection = newCollection(user1);

        newLike(user1, collection);
        newLike(user2, collection);
        newLike(user3, otherCollection);  // 다른 컬렉션 좋아요

        em.flush();
        em.clear();

        // when
        List<User> likers = collectionLikeRepository.findLikersByCollectionId(collection.getId());

        // then
        assertEquals(2, likers.size());
        assertTrue(likers.stream().anyMatch(u -> u.getId().equals(user1.getId())));
        assertTrue(likers.stream().anyMatch(u -> u.getId().equals(user2.getId())));
        assertFalse(likers.stream().anyMatch(u -> u.getId().equals(user3.getId())));
    }

    @Test
    @DisplayName("사용자가 좋아요한 컬렉션 목록 조회")
    void findLikedCollectionsByUserId() throws Exception {
        // given
        User user = newUser();
        User otherUser = newUser();
        UserBirdCollection collection1 = newCollection(user);
        UserBirdCollection collection2 = newCollection(otherUser);
        UserBirdCollection collection3 = newCollection(otherUser);

        newLike(user, collection1);
        newLike(user, collection2);
        newLike(otherUser, collection3);  // 다른 사용자의 좋아요

        em.flush();
        em.clear();

        // when
        List<UserBirdCollection> likedCollections =
                collectionLikeRepository.findLikedCollectionsByUserId(user.getId());

        // then
        assertEquals(2, likedCollections.size());
        assertTrue(likedCollections.stream().anyMatch(c -> c.getId().equals(collection1.getId())));
        assertTrue(likedCollections.stream().anyMatch(c -> c.getId().equals(collection2.getId())));
        assertFalse(likedCollections.stream().anyMatch(c -> c.getId().equals(collection3.getId())));
    }

    @Test
    @DisplayName("컬렉션의 좋아요 수 조회")
    void countByCollectionId() throws Exception {
        // given
        User user1 = newUser();
        User user2 = newUser();
        User user3 = newUser();
        UserBirdCollection collection = newCollection(user1);
        UserBirdCollection otherCollection = newCollection(user1);

        newLike(user1, collection);
        newLike(user2, collection);
        newLike(user3, otherCollection);  // 다른 컬렉션 좋아요

        em.flush();
        em.clear();

        // when
        long count = collectionLikeRepository.countByCollectionId(collection.getId());

        // then
        assertEquals(2L, count);
    }

    @Test
    @DisplayName("중복 좋아요 방지 - UniqueConstraint 확인")
    void duplicateLike_shouldFail() throws Exception {
        // given
        User user = newUser();
        UserBirdCollection collection = newCollection(user);
        
        newLike(user, collection);
        em.flush();

        // when & then
        assertThrows(Exception.class, () -> {
            newLike(user, collection);  // 중복 좋아요 시도
            em.flush();
        });
    }
}
