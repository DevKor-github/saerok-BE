package org.devkor.apu.saerok_server.domain.dex.bird.core.repository;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.query.dto.BirdSearchDto;
import org.devkor.apu.saerok_server.domain.dex.bird.query.dto.CmRangeDto;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.BirdSearchSortDirType;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.BirdSearchSortType;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.BirdBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(BirdRepository.class)
@ActiveProfiles("test")
class BirdRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired BirdRepository repo;

    private Bird newBird(String koreanName, double bodyLengthCm) {
        return new BirdBuilder(em)
                .korName(koreanName)
                .bodyLengthCm(bodyLengthCm)
                .build();
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("findById")
    void findById_returnsActiveBird() {
        Bird bird = newBird("search-bird-" + System.nanoTime(), 12.0);
        em.flush(); em.clear();

        Optional<Bird> found = repo.findById(bird.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(bird.getId());
    }

    @Test @DisplayName("findById - soft delete 된 새 제외")
    void findById_excludesSoftDeleted() {
        Bird bird = newBird("deleted-bird-" + System.nanoTime(), 10.0);
        em.flush();

        bird.softDelete();
        em.flush(); em.clear();

        Optional<Bird> found = repo.findById(bird.getId());
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("search filters by keyword")
    void search_filtersByKeyword_andOrdersById() {
        String keyword = "search-keyword-" + System.nanoTime();
        Bird first = newBird(keyword + "-a", 8.0);
        Bird second = newBird(keyword + "-b", 15.0);
        em.flush(); em.clear();

        BirdSearchDto dto = new BirdSearchDto(
                1,
                10,
                keyword,
                null,
                null,
                null,
                BirdSearchSortType.ID,
                BirdSearchSortDirType.ASC
        );

        List<Bird> result = repo.search(dto);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(first.getId());
        assertThat(result.get(1).getId()).isEqualTo(second.getId());
    }

    @Test @DisplayName("search filters by body length range")
    void search_filtersByBodyLengthRange() {
        String keyword = "length-keyword-" + System.nanoTime();
        Bird small = newBird(keyword + "-small", 9.0);
        newBird(keyword + "-large", 25.0);
        em.flush(); em.clear();

        BirdSearchDto dto = new BirdSearchDto(
                null,
                null,
                keyword,
                null,
                List.of(new CmRangeDto(5.0, 10.0)),
                null,
                BirdSearchSortType.ID,
                BirdSearchSortDirType.ASC
        );

        List<Bird> result = repo.search(dto);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(small.getId());
    }
}
