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

import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion.SuggestionType;

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
                .type(SuggestionType.SUGGEST)
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
    @DisplayName("existsByUserIdAndCollectionIdAndBirdIdAndType")
    void exists_check() {
        User u       = new UserBuilder(em).build();
        UserBirdCollection col = new CollectionBuilder(em).owner(u).build();
        Bird b       = new BirdBuilder(em)
                .korName("까마귀")
                .sciName("Corvus corone")
                .build();

        boolean before = repo.existsByUserIdAndCollectionIdAndBirdIdAndType(u.getId(), col.getId(), b.getId(), SuggestionType.AGREE);
        assertFalse(before);
        System.out.println("[exists_check] ▶︎ exists before save: " + before);

        new SuggestionBuilder(repo, em).user(u).collection(col).bird(b).type(SuggestionType.AGREE).build();
        em.flush();
        em.clear();

        boolean after = repo.existsByUserIdAndCollectionIdAndBirdIdAndType(u.getId(), col.getId(), b.getId(), SuggestionType.AGREE);
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
    @DisplayName("findToggleStatusByCollectionIdAndBirdId - 특정 birdId의 토글 상태 조회")
    void toggle_status() {
        User current = new UserBuilder(em).build();
        User other1  = new UserBuilder(em).build();
        User other2  = new UserBuilder(em).build();
        User collectionOwner = new UserBuilder(em).nickname("owner").build();
        UserBirdCollection col = new CollectionBuilder(em).owner(collectionOwner).build();

        Bird b1 = new BirdBuilder(em)
                .korName("직박구리")
                .sciName("Hypsipetes amaurotis")
                .build();

        // b1: 제안(current), 동의(current, other1), 비동의(other2)
        new SuggestionBuilder(repo, em).user(current).collection(col).bird(b1).type(SuggestionType.SUGGEST).build();
        new SuggestionBuilder(repo, em).user(current).collection(col).bird(b1).type(SuggestionType.AGREE).build();
        new SuggestionBuilder(repo, em).user(other1).collection(col).bird(b1).type(SuggestionType.AGREE).build();
        new SuggestionBuilder(repo, em).user(other2).collection(col).bird(b1).type(SuggestionType.DISAGREE).build();

        em.flush();
        em.clear();

        // current 사용자 관점에서 조회
        Object[] status = repo.findToggleStatusByCollectionIdAndBirdId(col.getId(), b1.getId(), current.getId());
        
        assertThat(status).hasSize(4);
        assertThat((Long) status[0]).isEqualTo(2L);    // agreeCount
        assertThat((Long) status[1]).isEqualTo(1L);    // disagreeCount
        assertThat((Boolean) status[2]).isTrue();      // isAgreedByMe
        assertThat((Boolean) status[3]).isFalse();     // isDisagreedByMe
        
        System.out.println("[toggle_status] ✔︎ agreeCount=" + status[0] + ", disagreeCount=" + status[1] + 
                          ", isAgreedByMe=" + status[2] + ", isDisagreedByMe=" + status[3]);
    }

    @Test
    @DisplayName("findToggleStatusByCollectionIdAndBirdId - 데이터가 없는 경우")
    void toggle_status_empty() {
        User current = new UserBuilder(em).build();
        UserBirdCollection col = new CollectionBuilder(em).owner(current).build();
        Bird b1 = new BirdBuilder(em)
                .korName("참새")
                .sciName("Passer montanus")
                .build();

        em.flush();
        em.clear();

        // 아무 데이터도 없는 상태에서 조회
        Object[] status = repo.findToggleStatusByCollectionIdAndBirdId(col.getId(), b1.getId(), current.getId());
        
        assertThat(status).hasSize(4);
        assertThat((Long) status[0]).isEqualTo(0L);     // agreeCount
        assertThat((Long) status[1]).isEqualTo(0L);     // disagreeCount
        assertThat((Boolean) status[2]).isFalse();      // isAgreedByMe
        assertThat((Boolean) status[3]).isFalse();      // isDisagreedByMe
        
        System.out.println("[toggle_status_empty] ✔︎ all zeros and false as expected");
    }

    @Test
    @DisplayName("findSummaryByCollectionId – 동의/비동의 관련 계산")
    void summary() {
        User current = new UserBuilder(em).build();
        User other1  = new UserBuilder(em).build();
        User other2  = new UserBuilder(em).build();
        User collectionOwner = new UserBuilder(em).nickname("owner").build();
        UserBirdCollection col = new CollectionBuilder(em).owner(collectionOwner).build();

        Bird b1 = new BirdBuilder(em)
                .korName("직박구리")
                .sciName("Hypsipetes amaurotis")
                .build();
        Bird b2 = new BirdBuilder(em)
                .korName("박새")
                .sciName("Parus major")
                .build();
        Bird b3 = new BirdBuilder(em)
                .korName("까치")
                .sciName("Pica Serica")
                .build(); // 제안만 되고 아무도 동의/비동의 안함

        // b1: 제안(current), 동의(current, other1), 비동의(other2)
        new SuggestionBuilder(repo, em).user(current).collection(col).bird(b1).type(SuggestionType.SUGGEST).build();
        new SuggestionBuilder(repo, em).user(current).collection(col).bird(b1).type(SuggestionType.AGREE).build();
        new SuggestionBuilder(repo, em).user(other1).collection(col).bird(b1).type(SuggestionType.AGREE).build();
        new SuggestionBuilder(repo, em).user(other2).collection(col).bird(b1).type(SuggestionType.DISAGREE).build();

        // b2: 제안(other1), 동의(other2), 비동의(current)
        new SuggestionBuilder(repo, em).user(other1).collection(col).bird(b2).type(SuggestionType.SUGGEST).build();
        new SuggestionBuilder(repo, em).user(other2).collection(col).bird(b2).type(SuggestionType.AGREE).build();
        new SuggestionBuilder(repo, em).user(current).collection(col).bird(b2).type(SuggestionType.DISAGREE).build();

        // b3: 제안(other2)
        new SuggestionBuilder(repo, em).user(other2).collection(col).bird(b3).type(SuggestionType.SUGGEST).build();

        em.flush();
        em.clear();

        List<BirdIdSuggestionSummary> list =
                repo.findSummaryByCollectionId(col.getId(), current.getId());
        assertThat(list).hasSize(3);

        // 결과 검증 - 이름으로 개별 아이템 찾아서 검증 (정렬 순서에 의존하지 않음)
        var first = list.get(0);
        assertThat(first.agreeCount()).isEqualTo(2L);
        assertThat(first.disagreeCount()).isEqualTo(1L);
        assertTrue(first.isAgreedByMe());
        assertFalse(first.isDisagreedByMe());

        var second = list.get(1);
        assertThat(second.agreeCount()).isEqualTo(1L);
        assertThat(second.disagreeCount()).isEqualTo(1L);
        assertFalse(second.isAgreedByMe());
        assertTrue(second.isDisagreedByMe());

        var third = list.get(2);
        assertThat(third.agreeCount()).isEqualTo(0L);
        assertThat(third.disagreeCount()).isEqualTo(0L);
        assertFalse(third.isAgreedByMe());
        assertFalse(third.isDisagreedByMe());
    }
}
