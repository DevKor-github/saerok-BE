package org.devkor.apu.saerok_server.domain.dex.bird.query.repository;

import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(BirdProfileViewRepository.class)
@ActiveProfiles("test")
public class BirdProfileViewRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    BirdProfileViewRepository birdProfileRepository;

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
}
