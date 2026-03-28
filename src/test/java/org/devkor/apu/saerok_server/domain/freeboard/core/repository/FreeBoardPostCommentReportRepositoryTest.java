package org.devkor.apu.saerok_server.domain.freeboard.core.repository;

import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostCommentReport;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.FreeBoardPostBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.FreeBoardPostCommentBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.FreeBoardPostCommentReportBuilder;
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
@Import(FreeBoardPostCommentReportRepository.class)
@ActiveProfiles("test")
class FreeBoardPostCommentReportRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired FreeBoardPostCommentReportRepository repo;

    private User user() { return new UserBuilder(em).build(); }
    private FreeBoardPost post(User user) { return new FreeBoardPostBuilder(em).user(user).build(); }
    private FreeBoardPostComment comment(User user, FreeBoardPost post, String content) {
        return new FreeBoardPostCommentBuilder(em).user(user).post(post).content(content).build();
    }

    private FreeBoardPostCommentReport report(User reporter, User reported, FreeBoardPostComment comment) {
        return new FreeBoardPostCommentReportBuilder(em)
                .reporter(reporter).reportedUser(reported).comment(comment).build();
    }

    @Test @DisplayName("save / findById / deleteById")
    void basic_crud() {
        User reporter = user();
        User owner = user();
        FreeBoardPost p = post(owner);
        FreeBoardPostComment c = comment(owner, p, "bad");

        FreeBoardPostCommentReport r = report(reporter, owner, c);
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
        User owner = user();
        FreeBoardPost p = post(owner);
        FreeBoardPostComment c1 = comment(owner, p, "a");

        FreeBoardPostCommentReport older = report(reporter, owner, c1);
        Thread.sleep(1100);
        FreeBoardPostComment c2 = comment(owner, p, "b");
        FreeBoardPostCommentReport newer = report(reporter, owner, c2);
        em.flush(); em.clear();

        List<FreeBoardPostCommentReport> list = repo.findAllOrderByCreatedAtDesc();
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(newer.getId());
        assertThat(list.get(1).getId()).isEqualTo(older.getId());
    }

    @Test @DisplayName("deleteByCommentId - 해당 댓글 신고 전부 삭제")
    void delete_by_comment() {
        User reporter = user();
        User owner = user();
        FreeBoardPost p = post(owner);
        FreeBoardPostComment c1 = comment(owner, p, "a");
        FreeBoardPostComment c2 = comment(owner, p, "b");

        report(reporter, owner, c1);
        report(reporter, owner, c1);
        report(reporter, owner, c2);
        em.flush(); em.clear();

        repo.deleteByCommentId(c1.getId());
        em.flush(); em.clear();

        List<FreeBoardPostCommentReport> remained = repo.findAllOrderByCreatedAtDesc();
        assertThat(remained).hasSize(1);
        assertThat(remained.getFirst().getComment().getId()).isEqualTo(c2.getId());
    }

    @Test @DisplayName("deleteByPostId - 같은 게시글 소속 댓글 신고 전부 삭제")
    void delete_by_post() {
        User reporter = user();
        User owner = user();
        FreeBoardPost p1 = post(owner);
        FreeBoardPost p2 = post(owner);

        FreeBoardPostComment c1 = comment(owner, p1, "a");
        FreeBoardPostComment c2 = comment(owner, p1, "b");
        FreeBoardPostComment c3 = comment(owner, p2, "c");

        report(reporter, owner, c1);
        report(reporter, owner, c2);
        report(reporter, owner, c3);
        em.flush(); em.clear();

        repo.deleteByPostId(p1.getId());
        em.flush(); em.clear();

        List<FreeBoardPostCommentReport> list = repo.findAllOrderByCreatedAtDesc();
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getComment().getPost().getId()).isEqualTo(p2.getId());
    }

    @Test @DisplayName("existsByReporterIdAndCommentId")
    void exists_by_reporter_and_comment() {
        User reporter = user();
        User owner = user();
        FreeBoardPost p = post(owner);
        FreeBoardPostComment c = comment(owner, p, "x");

        assertThat(repo.existsByReporterIdAndCommentId(reporter.getId(), c.getId())).isFalse();

        report(reporter, owner, c);
        em.flush(); em.clear();

        assertThat(repo.existsByReporterIdAndCommentId(reporter.getId(), c.getId())).isTrue();
    }

    @Test @DisplayName("commentContent 스냅샷이 저장된다")
    void comment_content_snapshot() {
        User reporter = user();
        User owner = user();
        FreeBoardPost p = post(owner);
        FreeBoardPostComment c = comment(owner, p, "원본 댓글 내용");

        FreeBoardPostCommentReport r = report(reporter, owner, c);
        em.flush(); em.clear();

        var found = repo.findById(r.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCommentContent()).isEqualTo("원본 댓글 내용");
    }
}
