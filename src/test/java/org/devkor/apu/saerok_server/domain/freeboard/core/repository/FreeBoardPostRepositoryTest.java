package org.devkor.apu.saerok_server.domain.freeboard.core.repository;

import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardPostQueryCommand;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.FreeBoardPostBuilder;
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
@Import(FreeBoardPostRepository.class)
@ActiveProfiles("test")
class FreeBoardPostRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired FreeBoardPostRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private FreeBoardPost post(User user, String content) {
        return new FreeBoardPostBuilder(em).user(user).content(content).build();
    }

    /* ------------------------------------------------------------------ */

    @Test @DisplayName("save / findById / remove")
    void save_findById_remove() {
        User u = user();
        FreeBoardPost p = post(u, "테스트");
        em.flush(); em.clear();

        Long id = p.getId();
        assertThat(repo.findById(id)).isPresent();

        repo.remove(em.find(FreeBoardPost.class, id));
        em.flush(); em.clear();

        assertThat(repo.findById(id)).isEmpty();
    }

    @Test @DisplayName("findByIdWithUser - user를 JOIN FETCH로 조회")
    void findByIdWithUser_fetchesUser() {
        User u = user();
        FreeBoardPost p = post(u, "내용");
        em.flush(); em.clear();

        var found = repo.findByIdWithUser(p.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getNickname()).isEqualTo(u.getNickname());
    }

    @Test @DisplayName("findByIdWithUser - 존재하지 않는 id는 빈 Optional")
    void findByIdWithUser_notFound() {
        assertThat(repo.findByIdWithUser(999999L)).isEmpty();
    }

    @Test @DisplayName("findAll - 최신순 정렬")
    void findAll_orderByCreatedAtDesc() {
        User u = user();
        FreeBoardPost p1 = post(u, "첫번째");
        FreeBoardPost p2 = post(u, "두번째");
        FreeBoardPost p3 = post(u, "세번째");
        em.flush(); em.clear();

        List<FreeBoardPost> result = repo.findAll(new FreeBoardPostQueryCommand(null, null));

        assertThat(result).hasSizeGreaterThanOrEqualTo(3);
        // 최신순이므로 세번째가 먼저
        List<Long> ids = result.stream().map(FreeBoardPost::getId).toList();
        assertThat(ids.indexOf(p3.getId())).isLessThan(ids.indexOf(p2.getId()));
        assertThat(ids.indexOf(p2.getId())).isLessThan(ids.indexOf(p1.getId()));
    }

    @Test @DisplayName("findAll - 페이지네이션 (size+1 패턴)")
    void findAll_pagination() {
        User u = user();
        for (int i = 0; i < 5; i++) {
            post(u, "게시글" + i);
        }
        em.flush(); em.clear();

        // size=2이면 3개 반환 (hasNext 판단용)
        List<FreeBoardPost> result = repo.findAll(new FreeBoardPostQueryCommand(1, 2));
        assertThat(result).hasSize(3);
    }

    @Test @DisplayName("findAll - 페이지 간 결과가 겹치지 않음")
    void findAll_pagesDoNotOverlap() {
        User u = user();
        for (int i = 0; i < 5; i++) {
            post(u, "페이지테스트" + i);
        }
        em.flush(); em.clear();

        List<FreeBoardPost> page1 = repo.findAll(new FreeBoardPostQueryCommand(1, 3));
        List<FreeBoardPost> page2 = repo.findAll(new FreeBoardPostQueryCommand(2, 3));

        // page1에서 size만큼만 취한 id와 page2의 id가 겹치지 않아야 함
        List<Long> page1Ids = page1.stream().limit(3).map(FreeBoardPost::getId).toList();
        List<Long> page2Ids = page2.stream().limit(3).map(FreeBoardPost::getId).toList();
        assertThat(page1Ids).doesNotContainAnyElementsOf(page2Ids);
    }

    @Test @DisplayName("countAll")
    void countAll() {
        long before = repo.countAll();
        User u = user();
        post(u, "A");
        post(u, "B");
        em.flush(); em.clear();

        assertThat(repo.countAll()).isEqualTo(before + 2);
    }
}
