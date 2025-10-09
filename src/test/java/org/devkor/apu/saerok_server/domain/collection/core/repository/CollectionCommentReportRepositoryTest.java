package org.devkor.apu.saerok_server.domain.collection.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentReport;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.CollectionBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CollectionCommentReportRepository.class)
@ActiveProfiles("test")
class CollectionCommentReportRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired CollectionCommentReportRepository repo;

    private static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    private User newUser() { return new UserBuilder(em).build(); }
    private UserBirdCollection newCollection(User owner) { return new CollectionBuilder(em).owner(owner).build(); }
    private UserBirdCollectionComment newComment(User author, UserBirdCollection col, String content) {
        UserBirdCollectionComment cm = UserBirdCollectionComment.of(author, col, content);
        em.persist(cm);
        return cm;
    }

    /** 신고 엔티티는 댓글 내용 스냅샷(commentContent)이 NOT NULL 이므로 반드시 채워준다. */
    private UserBirdCollectionCommentReport newReport(User reporter, User reported, UserBirdCollectionComment cm) {
        UserBirdCollectionCommentReport r = newInstance(UserBirdCollectionCommentReport.class);
        ReflectionTestUtils.setField(r, "reporter", reporter);
        ReflectionTestUtils.setField(r, "reportedUser", reported);
        ReflectionTestUtils.setField(r, "comment", cm);
        // 🔧 핵심 수정: 스냅샷 필드 채우기
        ReflectionTestUtils.setField(r, "commentContent", cm.getContent());
        repo.save(r);
        return r;
    }

    @Test
    @DisplayName("save / findById / deleteById")
    void basic_crud() {
        User rep = newUser();
        User owner = newUser();
        UserBirdCollection col = newCollection(owner);
        UserBirdCollectionComment cm = newComment(owner, col, "bad");

        UserBirdCollectionCommentReport r = newReport(rep, owner, cm);
        em.flush(); em.clear();

        assertThat(repo.findById(r.getId())).isPresent();

        boolean deleted = repo.deleteById(r.getId());
        em.flush(); em.clear();

        assertThat(deleted).isTrue();
        assertThat(repo.findById(r.getId())).isEmpty();
    }

    @Test
    @DisplayName("findAllOrderByCreatedAtDesc - createdAt 역순")
    void order_desc_by_createdAt() throws InterruptedException {  // ← throws 추가
        User rep = newUser();
        User owner = newUser();
        UserBirdCollection col = newCollection(owner);
        UserBirdCollectionComment cm = newComment(owner, col, "a");

        // older 먼저 생성
        UserBirdCollectionCommentReport older = newReport(rep, owner, cm);

        // createdAt이 초 단위라 충분히 다른 값이 되도록 1.1초 정도 대기
        Thread.sleep(1100);

        // newer 생성 (createdAt이 older보다 늦음)
        UserBirdCollectionComment cm2 = newComment(owner, col, "b");
        UserBirdCollectionCommentReport newer = newReport(rep, owner, cm2);

        em.flush(); em.clear();

        List<UserBirdCollectionCommentReport> list = repo.findAllOrderByCreatedAtDesc();
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(newer.getId());
        assertThat(list.get(1).getId()).isEqualTo(older.getId());
    }

    @Test
    @DisplayName("deleteByCommentId - 해당 댓글 신고 전부 삭제")
    void delete_by_comment() {
        User rep = newUser();
        User owner = newUser();
        UserBirdCollection col = newCollection(owner);
        UserBirdCollectionComment cm1 = newComment(owner, col, "a");
        UserBirdCollectionComment cm2 = newComment(owner, col, "b");

        newReport(rep, owner, cm1);
        newReport(rep, owner, cm1);
        newReport(rep, owner, cm2);
        em.flush(); em.clear();

        repo.deleteByCommentId(cm1.getId());
        em.flush(); em.clear();

        long remain = repo.findAllOrderByCreatedAtDesc()
                .stream().filter(r -> r.getComment().getId().equals(cm1.getId())).count();
        assertThat(remain).isZero();
    }

    @Test
    @DisplayName("deleteByCollectionId - 같은 컬렉션 소속 댓글 신고 전부 삭제")
    void delete_by_collection() {
        User rep = newUser();
        User owner = newUser();
        UserBirdCollection col1 = newCollection(owner);
        UserBirdCollection col2 = newCollection(owner);

        UserBirdCollectionComment cm1 = newComment(owner, col1, "a");
        UserBirdCollectionComment cm2 = newComment(owner, col1, "b");
        UserBirdCollectionComment cm3 = newComment(owner, col2, "c");

        newReport(rep, owner, cm1);
        newReport(rep, owner, cm2);
        newReport(rep, owner, cm3);
        em.flush(); em.clear();

        repo.deleteByCollectionId(col1.getId());
        em.flush(); em.clear();

        List<UserBirdCollectionCommentReport> list = repo.findAllOrderByCreatedAtDesc();
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getComment().getCollection().getId()).isEqualTo(col2.getId());
    }

    @Test
    @DisplayName("existsByReporterIdAndCommentId")
    void exists_by_reporter_and_comment() {
        User rep = newUser();
        User owner = newUser();
        UserBirdCollection col = newCollection(owner);
        UserBirdCollectionComment cm = newComment(owner, col, "x");

        boolean before = repo.existsByReporterIdAndCommentId(rep.getId(), cm.getId());
        assertThat(before).isFalse();

        newReport(rep, owner, cm);
        em.flush(); em.clear();

        boolean after = repo.existsByReporterIdAndCommentId(rep.getId(), cm.getId());
        assertThat(after).isTrue();
    }
}
