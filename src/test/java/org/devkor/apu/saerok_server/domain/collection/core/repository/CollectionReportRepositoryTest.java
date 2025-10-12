package org.devkor.apu.saerok_server.domain.collection.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionReport;
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
@Import(CollectionReportRepository.class)
@ActiveProfiles("test")
class CollectionReportRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired CollectionReportRepository repo;

    private static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    private User newUser() { return new UserBuilder(em).build(); }
    private UserBirdCollection newCollection(User owner) { return new CollectionBuilder(em).owner(owner).build(); }

    private UserBirdCollectionReport newReport(User reporter, User reported, UserBirdCollection col) {
        UserBirdCollectionReport r = newInstance(UserBirdCollectionReport.class);
        ReflectionTestUtils.setField(r, "reporter", reporter);
        ReflectionTestUtils.setField(r, "reportedUser", reported);
        ReflectionTestUtils.setField(r, "collection", col);
        repo.save(r);
        return r;
    }

    @Test
    @DisplayName("save / findById / deleteById")
    void basic_crud() {
        User reporter = newUser();
        User reported = newUser();
        UserBirdCollection col = newCollection(reported);

        UserBirdCollectionReport r = newReport(reporter, reported, col);
        em.flush(); em.clear();

        assertThat(repo.findById(r.getId())).isPresent();

        boolean deleted = repo.deleteById(r.getId());
        em.flush(); em.clear();

        assertThat(deleted).isTrue();
        assertThat(repo.findById(r.getId())).isEmpty();
    }

    @Test
    @DisplayName("findAllOrderByCreatedAtDesc - createdAt 역순")
    void order_desc_by_createdAt() throws InterruptedException {
        User a = newUser();
        User b = newUser();
        UserBirdCollection col = newCollection(b);

        // older 먼저 저장
        UserBirdCollectionReport older = newReport(a, b, col);

        // createdAt이 '초' 단위라 1.1초 정도 간격을 준다
        Thread.sleep(1100);

        // newer 저장 (createdAt > older)
        UserBirdCollectionReport newer = newReport(a, b, col);

        em.flush(); em.clear();

        List<UserBirdCollectionReport> list = repo.findAllOrderByCreatedAtDesc();
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(newer.getId());
        assertThat(list.get(1).getId()).isEqualTo(older.getId());
    }

    @Test
    @DisplayName("deleteByCollectionId - 해당 컬렉션 신고 전부 삭제")
    void delete_by_collection() {
        User u1 = newUser();
        User u2 = newUser();
        UserBirdCollection col1 = newCollection(u2);
        UserBirdCollection col2 = newCollection(u2);

        newReport(u1, u2, col1);
        newReport(u1, u2, col1);
        newReport(u1, u2, col2);
        em.flush(); em.clear();

        repo.deleteByCollectionId(col1.getId());
        em.flush(); em.clear();

        List<UserBirdCollectionReport> remained = repo.findAllOrderByCreatedAtDesc();
        assertThat(remained).hasSize(1);
        assertThat(remained.getFirst().getCollection().getId()).isEqualTo(col2.getId());
    }

    @Test
    @DisplayName("existsByReporterIdAndCollectionId")
    void exists_by_reporter_and_collection() {
        User rep = newUser();
        User owner = newUser();
        UserBirdCollection col = newCollection(owner);

        boolean before = repo.existsByReporterIdAndCollectionId(rep.getId(), col.getId());
        assertThat(before).isFalse();

        newReport(rep, owner, col);
        em.flush(); em.clear();

        boolean after = repo.existsByReporterIdAndCollectionId(rep.getId(), col.getId());
        assertThat(after).isTrue();
    }
}
