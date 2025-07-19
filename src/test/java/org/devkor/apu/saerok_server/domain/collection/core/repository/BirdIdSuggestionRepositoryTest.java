package org.devkor.apu.saerok_server.domain.collection.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.repository.dto.BirdIdSuggestionSummary;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.BirdBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.CollectionBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.SuggestionBuilder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(BirdIdSuggestionRepository.class)
@ActiveProfiles("test")
class BirdIdSuggestionRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private BirdIdSuggestionRepository repo;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("save / find / remove")
    void save_find_remove() {
        User u       = new UserBuilder(em).build();
        UserBirdCollection col = new CollectionBuilder(em).owner(u).build();
        Bird b       = new BirdBuilder(em)
                .korName("까치")
                .sciName("Pica pica")
                .build();

        BirdIdSuggestion s = new SuggestionBuilder(repo, em)
                .user(u)
                .collection(col)
                .bird(b)
                .build();

        em.flush();
        em.clear();

        Optional<BirdIdSuggestion> found = repo.findById(s.getId());
        assertThat(found).isPresent();
        System.out.println("[save_find_remove] ▶︎ found suggestion id=" + found.get().getId());

        repo.remove(found.get());
        em.flush();
        em.clear();

        assertThat(repo.findById(s.getId())).isEmpty();
        System.out.println("[save_find_remove] ✔︎ removal successful");
    }

    @Test
    @DisplayName("existsByUserIdAndCollectionIdAndBirdId")
    void exists_check() {
        User u       = new UserBuilder(em).build();
        UserBirdCollection col = new CollectionBuilder(em).owner(u).build();
        Bird b       = new BirdBuilder(em)
                .korName("까마귀")
                .sciName("Corvus corone")
                .build();

        boolean before = repo.existsByUserIdAndCollectionIdAndBirdId(u.getId(), col.getId(), b.getId());
        assertFalse(before);
        System.out.println("[exists_check] ▶︎ exists before save: " + before);

        new SuggestionBuilder(repo, em).user(u).collection(col).bird(b).build();
        em.flush();
        em.clear();

        boolean after = repo.existsByUserIdAndCollectionIdAndBirdId(u.getId(), col.getId(), b.getId());
        assertTrue(after);
        System.out.println("[exists_check] ✔︎ exists after save: " + after);
    }

    @Test
    @DisplayName("deleteByCollectionId – 전체 삭제")
    void delete_all_by_collection() {
        User u1  = new UserBuilder(em).build();
        User u2  = new UserBuilder(em).build();
        UserBirdCollection col = new CollectionBuilder(em).owner(u1).build();

        Bird b1 = new BirdBuilder(em)
                .korName("참새")
                .sciName("Passer montanus")
                .build();
        Bird b2 = new BirdBuilder(em)
                .korName("비둘기")
                .sciName("Columba livia")
                .build();

        new SuggestionBuilder(repo, em).user(u1).collection(col).bird(b1).build();
        new SuggestionBuilder(repo, em).user(u2).collection(col).bird(b2).build();

        em.flush();
        em.clear();

        repo.deleteByCollectionId(col.getId());
        em.flush();
        em.clear();

        List<BirdIdSuggestion> remaining = repo.findByCollectionId(col.getId());
        assertThat(remaining).isEmpty();
        System.out.println("[delete_all_by_collection] ✔︎ all suggestions deleted for collection " + col.getId());
    }

    @Test
    @DisplayName("findSummaryByCollectionId – agreeCnt & isAgreedByMe 계산")
    void summary() {
        User current = new UserBuilder(em).build();
        User other   = new UserBuilder(em).build();
        UserBirdCollection col = new CollectionBuilder(em).owner(other).build();

        Bird b1 = new BirdBuilder(em)
                .korName("직박구리")
                .sciName("Hypsipetes amaurotis")
                .build();
        Bird b2 = new BirdBuilder(em)
                .korName("박새")
                .sciName("Parus major")
                .build();

        // b1: two agrees (one by current, one by other)
        new SuggestionBuilder(repo, em).user(current).collection(col).bird(b1).build();
        new SuggestionBuilder(repo, em).user(other).collection(col).bird(b1).build();
        // b2: one agree (by other)
        new SuggestionBuilder(repo, em).user(other).collection(col).bird(b2).build();

        em.flush();
        em.clear();

        List<BirdIdSuggestionSummary> list =
                repo.findSummaryByCollectionId(col.getId(), current.getId());
        assertThat(list).hasSize(2);

        var first  = list.get(0);
        var second = list.get(1);

        assertThat(first.birdId()).isEqualTo(b1.getId());
        assertThat(first.agreeCount()).isEqualTo(2L);
        assertTrue(first.isAgreedByMe());

        assertThat(second.birdId()).isEqualTo(b2.getId());
        assertThat(second.agreeCount()).isEqualTo(1L);
        assertFalse(second.isAgreedByMe());

        System.out.printf(
                "[summary] ✔︎ %d summaries returned (first: birdId=%d, agreeCnt=%d, isAgreedByMe=%b)%n",
                list.size(),
                first.birdId(), first.agreeCount(), first.isAgreedByMe()
        );
    }
}
