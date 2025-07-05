package org.devkor.apu.saerok_server.domain.collection.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
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

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(CollectionCommentRepository.class)
@ActiveProfiles("test")
class CollectionCommentRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired CollectionCommentRepository repo;

    private final GeometryFactory gf = new GeometryFactory();
    private Field collUserField;

    @BeforeEach
    void setup() throws NoSuchFieldException {
        collUserField = UserBirdCollection.class.getDeclaredField("user");
        collUserField.setAccessible(true);
    }

    private User user() { User u = new User(); em.persist(u); return u; }

    private UserBirdCollection collection(User owner) {
        try {
            UserBirdCollection c = new UserBirdCollection();
            collUserField.set(c, owner);

            Point p = gf.createPoint(new Coordinate(126.9780, 37.5665));
            c.setLocation(p);
            c.setDiscoveredDate(LocalDate.now());
            c.setAccessLevel(AccessLevelType.PUBLIC);

            em.persist(c);
            return c;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }


    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / find / remove")
    void save_find_remove() {
        User u = user();
        UserBirdCollection col = collection(u);
        UserBirdCollectionComment cm = UserBirdCollectionComment.of(u, col, "hi");

        repo.save(cm);
        em.flush(); em.clear();

        Long id = cm.getId();
        assertThat(repo.findById(id)).isPresent();

        repo.remove(em.find(UserBirdCollectionComment.class, id));
        em.flush(); em.clear();

        assertThat(repo.findById(id)).isEmpty();
    }

    @Test @DisplayName("findByCollectionId & countByCollectionId")
    void list_and_count() {
        User u = user();
        UserBirdCollection col = collection(u);

        repo.save(UserBirdCollectionComment.of(u, col, "A"));
        repo.save(UserBirdCollectionComment.of(u, col, "B"));
        em.flush(); em.clear();

        assertThat(repo.findByCollectionId(col.getId()))
                .extracting(UserBirdCollectionComment::getContent)
                .containsExactly("A", "B");          // createdAt ASC

        assertThat(repo.countByCollectionId(col.getId())).isEqualTo(2L);
    }
}
