package org.devkor.apu.saerok_server.domain.freeboard.core.repository;

import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardCommentQueryCommand;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.FreeBoardPostBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.FreeBoardPostCommentBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({FreeBoardPostCommentRepository.class, FreeBoardPostRepository.class})
@ActiveProfiles("test")
class FreeBoardPostCommentRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired FreeBoardPostCommentRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private FreeBoardPost post(User user) {
        return new FreeBoardPostBuilder(em).user(user).build();
    }

    private FreeBoardPostComment comment(User user, FreeBoardPost post, String content) {
        return new FreeBoardPostCommentBuilder(em).user(user).post(post).content(content).build();
    }

    private FreeBoardPostComment reply(User user, FreeBoardPost post, String content, FreeBoardPostComment parent) {
        return new FreeBoardPostCommentBuilder(em).user(user).post(post).content(content).parent(parent).build();
    }

    /* ------------------------------------------------------------------ */

    @Test @DisplayName("save / findById / remove")
    void save_findById_remove() {
        User u = user();
        FreeBoardPost p = post(u);
        FreeBoardPostComment c = comment(u, p, "댓글");
        em.flush(); em.clear();

        Long id = c.getId();
        assertThat(repo.findById(id)).isPresent();

        repo.remove(em.find(FreeBoardPostComment.class, id));
        em.flush(); em.clear();

        assertThat(repo.findById(id)).isEmpty();
    }

    @Test @DisplayName("findByIdWithUserAndPost - user와 post.user를 JOIN FETCH로 조회")
    void findByIdWithUserAndPost_fetchesRelations() {
        User postOwner = user();
        User commenter = user();
        FreeBoardPost p = post(postOwner);
        FreeBoardPostComment c = comment(commenter, p, "댓글");
        em.flush(); em.clear();

        var found = repo.findByIdWithUserAndPost(c.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getNickname()).isEqualTo(commenter.getNickname());
        assertThat(found.get().getPost().getUser().getNickname()).isEqualTo(postOwner.getNickname());
    }

    @Test @DisplayName("findByPostId & countByPostId")
    void list_and_count() {
        User u = user();
        FreeBoardPost p = post(u);
        comment(u, p, "A");
        comment(u, p, "B");
        em.flush(); em.clear();

        assertThat(repo.findByPostId(p.getId(), new FreeBoardCommentQueryCommand(null, null)))
                .extracting(FreeBoardPostComment::getContent)
                .containsExactly("A", "B");  // createdAt ASC

        assertThat(repo.countByPostId(p.getId())).isEqualTo(2L);
    }

    @Test @DisplayName("countByPostId는 ACTIVE 댓글만 카운트")
    void count_onlyActiveComments() {
        User u = user();
        FreeBoardPost p = post(u);
        comment(u, p, "active");
        FreeBoardPostComment deleted = comment(u, p, "deleted");
        em.flush();

        deleted.softDelete();
        em.flush(); em.clear();

        assertThat(repo.countByPostId(p.getId())).isEqualTo(1L);
    }

    @Test @DisplayName("countByPostIds - 배치 조회, 댓글 없는 게시글은 0")
    void countByPostIds_batchWithZeros() {
        User u = user();
        FreeBoardPost p1 = post(u);
        FreeBoardPost p2 = post(u);
        FreeBoardPost p3 = post(u);  // 댓글 없는 게시글

        comment(u, p1, "A");
        comment(u, p1, "B");
        comment(u, p2, "C");
        em.flush(); em.clear();

        Map<Long, Long> counts = repo.countByPostIds(List.of(p1.getId(), p2.getId(), p3.getId()));

        assertThat(counts).hasSize(3);
        assertThat(counts.get(p1.getId())).isEqualTo(2L);
        assertThat(counts.get(p2.getId())).isEqualTo(1L);
        assertThat(counts.get(p3.getId())).isEqualTo(0L);
    }

    @Test @DisplayName("countByPostIds - 빈 리스트 전달 시 빈 Map 반환")
    void countByPostIds_emptyList() {
        assertThat(repo.countByPostIds(List.of())).isEmpty();
    }

    @Test @DisplayName("countByPostIds - soft deleted 댓글은 제외")
    void countByPostIds_excludesSoftDeleted() {
        User u = user();
        FreeBoardPost p = post(u);
        comment(u, p, "active");
        FreeBoardPostComment deleted = comment(u, p, "deleted");
        em.flush();

        deleted.softDelete();
        em.flush(); em.clear();

        Map<Long, Long> counts = repo.countByPostIds(List.of(p.getId()));
        assertThat(counts.get(p.getId())).isEqualTo(1L);
    }

    @Test @DisplayName("hasReplies - 대댓글이 있는 경우")
    void hasReplies_true() {
        User u = user();
        FreeBoardPost p = post(u);
        FreeBoardPostComment parent = comment(u, p, "parent");
        em.flush();

        reply(u, p, "reply", parent);
        em.flush(); em.clear();

        assertThat(repo.hasReplies(parent.getId())).isTrue();
    }

    @Test @DisplayName("hasReplies - 대댓글이 없는 경우")
    void hasReplies_false() {
        User u = user();
        FreeBoardPost p = post(u);
        FreeBoardPostComment c = comment(u, p, "no replies");
        em.flush(); em.clear();

        assertThat(repo.hasReplies(c.getId())).isFalse();
    }

    @Test @DisplayName("부모-자식 관계 조회")
    void parentChildRelationship() {
        User u = user();
        FreeBoardPost p = post(u);
        FreeBoardPostComment parent = comment(u, p, "parent");
        em.flush();

        reply(u, p, "reply1", parent);
        reply(u, p, "reply2", parent);
        em.flush(); em.clear();

        var comments = repo.findByPostId(p.getId(), new FreeBoardCommentQueryCommand(null, null));

        assertThat(comments).hasSize(3);
        assertThat(comments)
                .extracting(FreeBoardPostComment::getContent)
                .containsExactly("parent", "reply1", "reply2");

        var loadedParent = comments.stream()
                .filter(c -> c.getContent().equals("parent"))
                .findFirst().get();
        var loadedReply = comments.stream()
                .filter(c -> c.getContent().equals("reply1"))
                .findFirst().get();

        assertThat(loadedParent.getParent()).isNull();
        assertThat(loadedParent.isReply()).isFalse();
        assertThat(loadedReply.getParent()).isNotNull();
        assertThat(loadedReply.getParent().getId()).isEqualTo(loadedParent.getId());
        assertThat(loadedReply.isReply()).isTrue();
    }

    @Test @DisplayName("findByPostId - 페이지네이션 (size+1 패턴)")
    void findByPostId_pagination() {
        User u = user();
        FreeBoardPost p = post(u);
        for (int i = 0; i < 5; i++) {
            comment(u, p, "댓글" + i);
        }
        em.flush(); em.clear();

        var result = repo.findByPostId(p.getId(), new FreeBoardCommentQueryCommand(1, 3));
        assertThat(result).hasSize(4);  // size+1 = 4
    }
}
