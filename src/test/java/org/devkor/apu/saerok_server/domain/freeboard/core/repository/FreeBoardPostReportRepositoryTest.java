package org.devkor.apu.saerok_server.domain.freeboard.core.repository;

import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostReport;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.FreeBoardPostBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.FreeBoardPostReportBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(FreeBoardPostReportRepository.class)
@ActiveProfiles("test")
class FreeBoardPostReportRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired FreeBoardPostReportRepository repo;

    private User user() { return new UserBuilder(em).build(); }
    private FreeBoardPost post(User user) { return new FreeBoardPostBuilder(em).user(user).build(); }

    private FreeBoardPostReport report(User reporter, User reported, FreeBoardPost post) {
        return new FreeBoardPostReportBuilder(em)
                .reporter(reporter).reportedUser(reported).post(post).build();
    }

    @Test @DisplayName("save / findById / deleteById")
    void basic_crud() {
        User reporter = user();
        User reported = user();
        FreeBoardPost p = post(reported);

        FreeBoardPostReport r = report(reporter, reported, p);
        em.flush(); em.clear();

        assertThat(repo.findById(r.getId())).isPresent();

        boolean deleted = repo.deleteById(r.getId());
        em.flush(); em.clear();

        assertThat(deleted).isTrue();
        assertThat(repo.findById(r.getId())).isEmpty();
    }

    @Test @DisplayName("deleteById - 존재하지 않는 경우 false 반환")
    void deleteById_notFound() {
        assertThat(repo.deleteById(999999L)).isFalse();
    }

    @Test @DisplayName("findAllOrderByCreatedAtDesc - createdAt 역순")
    void order_desc_by_createdAt() throws InterruptedException {
        User reporter = user();
        User reported = user();
        FreeBoardPost p = post(reported);

        FreeBoardPostReport older = report(reporter, reported, p);
        Thread.sleep(1100);
        FreeBoardPostReport newer = report(reporter, reported, p);
        em.flush(); em.clear();

        List<FreeBoardPostReport> list = repo.findAllOrderByCreatedAtDesc();
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(newer.getId());
        assertThat(list.get(1).getId()).isEqualTo(older.getId());
    }

    @Test @DisplayName("deleteByPostId - 해당 게시글 신고 전부 삭제")
    void delete_by_post() {
        User u1 = user();
        User u2 = user();
        FreeBoardPost p1 = post(u2);
        FreeBoardPost p2 = post(u2);

        report(u1, u2, p1);
        report(u1, u2, p1);
        report(u1, u2, p2);
        em.flush(); em.clear();

        repo.deleteByPostId(p1.getId());
        em.flush(); em.clear();

        List<FreeBoardPostReport> remained = repo.findAllOrderByCreatedAtDesc();
        assertThat(remained).hasSize(1);
        assertThat(remained.getFirst().getPost().getId()).isEqualTo(p2.getId());
    }

    @Test @DisplayName("existsByReporterIdAndPostId")
    void exists_by_reporter_and_post() {
        User reporter = user();
        User owner = user();
        FreeBoardPost p = post(owner);

        assertThat(repo.existsByReporterIdAndPostId(reporter.getId(), p.getId())).isFalse();

        report(reporter, owner, p);
        em.flush(); em.clear();

        assertThat(repo.existsByReporterIdAndPostId(reporter.getId(), p.getId())).isTrue();
    }
}
