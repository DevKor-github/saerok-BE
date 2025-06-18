package org.devkor.apu.saerok_server.domain.collection.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CollectionRepository.class)
@ActiveProfiles("test")
class CollectionRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    CollectionRepository collectionRepository;

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
        User u = new User();
        em.persist(u);
        return u;
    }

    private UserBirdCollection newCollection(
            User owner,
            Point point,
            AccessLevelType accessLevel
    ) throws IllegalAccessException {
        UserBirdCollection c = new UserBirdCollection();
        collUserField.set(c, owner);
        c.setLocation(point);
        c.setAccessLevel(accessLevel);
        c.setDiscoveredDate(LocalDate.now());
        em.persist(c);
        return c;
    }

    /* ------------------------------------------------------------------
     * tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("isMineOnly=true 이면 같은 반경 내 ‘내 컬렉션’만 조회")
    void findNearby_mineOnly_returnsOnlyMyCollections() throws Exception {
        // given
        User me = newUser();
        User other = newUser();

        Point ref = gf.createPoint(new Coordinate(126.9780, 37.5665));   // 서울시청
        Point near = gf.createPoint(new Coordinate(126.9779, 37.5666));
        Point far  = gf.createPoint(new Coordinate(127.0000, 37.5800));  // 반경 밖

        UserBirdCollection myColl   = newCollection(me, near, AccessLevelType.PRIVATE);
        newCollection(other, near, AccessLevelType.PUBLIC);              // 우리 지도용
        newCollection(other, far,  AccessLevelType.PUBLIC);              // 반경 밖

        em.flush();
        em.clear();

        // when
        List<UserBirdCollection> result =
                collectionRepository.findNearby(ref, 1_000, me.getId(), true);

        // then
        assertEquals(1, result.size(), "mineOnly=true 이므로 한 개만 반환");
        assertEquals(myColl.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("isMineOnly=false 이면 PUBLIC + 내 컬렉션이 모두 조회")
    void findNearby_all_returnsPublicAndMyCollections() throws Exception {
        // given
        User me = newUser();
        User other = newUser();

        Point ref = gf.createPoint(new Coordinate(126.9780, 37.5665));
        Point near = gf.createPoint(new Coordinate(126.9781, 37.5664));
        Point far = gf.createPoint(new Coordinate(127.0000, 37.6000));

        UserBirdCollection myColl       = newCollection(me, near, AccessLevelType.PRIVATE);
        UserBirdCollection publicColl   = newCollection(other, near, AccessLevelType.PUBLIC);
        newCollection(other, near, AccessLevelType.PRIVATE);  // 다른 사람의 PRIVATE 컬렉션 - should be filtered
        newCollection(me, far, AccessLevelType.PUBLIC); // 반경 밖의 내 컬렉션 - should be filtered

        em.flush();
        em.clear();

        // when
        List<UserBirdCollection> result =
                collectionRepository.findNearby(ref, 1_000, me.getId(), false);

        // then
        assertEquals(2, result.size(), "PUBLIC + 내 컬렉션 두 개 조회");
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(myColl.getId())));
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(publicColl.getId())));
    }

    @Test
    @DisplayName("익명 호출 시 PUBLIC 컬렉션만 조회")
    void findNearby_anonymous_returnsOnlyPublic() throws Exception {
        // given
        User owner = newUser();

        Point ref = gf.createPoint(new Coordinate(126.9780, 37.5665));
        Point near = gf.createPoint(new Coordinate(126.9782, 37.5663));

        UserBirdCollection publicColl  = newCollection(owner, near, AccessLevelType.PUBLIC);
        newCollection(owner, near, AccessLevelType.PRIVATE);

        em.flush();
        em.clear();

        // when
        List<UserBirdCollection> result =
                collectionRepository.findNearby(ref, 1_000, null, false);

        // then
        assertEquals(1, result.size(), "PUBLIC 하나만 조회");
        assertEquals(publicColl.getId(), result.getFirst().getId());
    }
}