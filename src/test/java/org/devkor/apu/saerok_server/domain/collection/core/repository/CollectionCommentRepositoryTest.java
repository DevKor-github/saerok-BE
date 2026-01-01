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

    private User user() {
        User u = User.createUser("test@example.com");
        u.setNickname("testUser");
        em.persist(u);
        return u;
    }

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

    @Test @DisplayName("countByCollectionId는 ACTIVE 댓글만 카운트")
    void count_onlyActiveComments() {
        User u = user();
        UserBirdCollection col = collection(u);

        UserBirdCollectionComment active = UserBirdCollectionComment.of(u, col, "active");
        UserBirdCollectionComment deleted = UserBirdCollectionComment.of(u, col, "deleted");
        UserBirdCollectionComment banned = UserBirdCollectionComment.of(u, col, "banned");

        repo.save(active);
        repo.save(deleted);
        repo.save(banned);
        em.flush();

        deleted.softDelete();
        banned.ban();
        em.flush(); em.clear();

        assertThat(repo.countByCollectionId(col.getId())).isEqualTo(1L);
    }

    @Test @DisplayName("hasReplies - 대댓글이 있는 경우")
    void hasReplies_true() {
        User u = user();
        UserBirdCollection col = collection(u);

        UserBirdCollectionComment parent = UserBirdCollectionComment.of(u, col, "parent");
        repo.save(parent);
        em.flush();

        UserBirdCollectionComment reply = UserBirdCollectionComment.of(u, col, "reply", parent);
        repo.save(reply);
        em.flush(); em.clear();

        assertThat(repo.hasReplies(parent.getId())).isTrue();
    }

    @Test @DisplayName("hasReplies - 대댓글이 없는 경우")
    void hasReplies_false() {
        User u = user();
        UserBirdCollection col = collection(u);

        UserBirdCollectionComment comment = UserBirdCollectionComment.of(u, col, "no replies");
        repo.save(comment);
        em.flush(); em.clear();

        assertThat(repo.hasReplies(comment.getId())).isFalse();
    }

    @Test @DisplayName("부모-자식 관계 조회")
    void parentChildRelationship() {
        User u = user();
        UserBirdCollection col = collection(u);

        UserBirdCollectionComment parent = UserBirdCollectionComment.of(u, col, "parent");
        repo.save(parent);
        em.flush();

        UserBirdCollectionComment reply1 = UserBirdCollectionComment.of(u, col, "reply1", parent);
        UserBirdCollectionComment reply2 = UserBirdCollectionComment.of(u, col, "reply2", parent);
        repo.save(reply1);
        repo.save(reply2);
        em.flush(); em.clear();

        var comments = repo.findByCollectionId(col.getId());

        assertThat(comments).hasSize(3);
        assertThat(comments)
                .extracting(UserBirdCollectionComment::getContent)
                .containsExactly("parent", "reply1", "reply2");

        var loadedParent = comments.stream()
                .filter(c -> c.getContent().equals("parent"))
                .findFirst().get();
        var loadedReply1 = comments.stream()
                .filter(c -> c.getContent().equals("reply1"))
                .findFirst().get();

        assertThat(loadedParent.getParent()).isNull();
        assertThat(loadedReply1.getParent()).isNotNull();
        assertThat(loadedReply1.getParent().getId()).isEqualTo(loadedParent.getId());
        assertThat(loadedReply1.isReply()).isTrue();
        assertThat(loadedParent.isReply()).isFalse();
    }
}
