package org.devkor.apu.saerok_server.domain.dex.bird.query.repository;

import org.assertj.core.api.Assertions;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        BirdProfileViewRepository.class,
        BirdRepository.class
})
@ActiveProfiles("test")
public class BirdProfileViewRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    BirdProfileViewRepository birdProfileRepository;

    @Autowired
    BirdRepository birdRepository;

    @Autowired
    TestEntityManager testEm;

    @Test
    void 새프로필_ID로_조회하기() {
        // when
        Optional<BirdProfileView> birdProfile = birdProfileRepository.findById(64L);

        // then
        assertTrue(birdProfile.isPresent());
        assertEquals(64L, birdProfile.get().getId());

        System.out.println(birdProfile.get().toSummaryString());
    }

    @Test
    void 새프로필_전부_조회하기() {
        // when
        List<BirdProfileView> result = birdProfileRepository.findAll();

        // then
        assertFalse(result.isEmpty());

        System.out.println(result.size() + "개의 결과 조회됨");
        System.out.println(result.getLast().toSummaryString());
    }

    @Disabled("새 추가 기능 구현 후 테스트 필요")
    @Test
    void 특정시각_이후_생성된_새프로필_조회하기() {
        // TODO
    }

    @Test
    void 특정시각_이후_수정된_새프로필_조회하기() throws InterruptedException {
        // given
        OffsetDateTime since = OffsetDateTime.now();
        Bird bird = birdRepository.findById(166L).orElseThrow(
                () -> new IllegalStateException("테스트 준비에 필요한 bird가 존재하지 않음")
        );
        Thread.sleep(100);
        bird.getDescription().setDescription("새록새록");
        testEm.flush();
        birdProfileRepository.refreshMaterializedView();
        testEm.clear();

        // when
        List<BirdProfileView> result = birdProfileRepository.findByUpdatedAtAfter(since);

        // then
        assertFalse(result.isEmpty(), "수정된 프로필이 조회되어야 한다");

        assertTrue(
                result.stream().anyMatch(v -> v.getId().equals(bird.getId())),
                "수정된 bird.id=" + bird.getId() + " 가 포함되어야 한다"
        );

        BirdProfileView updatedView = result.stream()
                .filter(v -> v.getId().equals(bird.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(
                updatedView.getUpdatedAt().isAfter(since),
                "updatedAt=" + updatedView.getUpdatedAt() + " 은 since=" + since + " 이후여야 한다"
        );

        System.out.println(updatedView.toString());
    }

    @Disabled("새 삭제 기능 구현 후 테스트 필요")
    @Test
    void 특정시각_이후_삭제된_새프로필_조회하기() {
        // TODO
    }
}
