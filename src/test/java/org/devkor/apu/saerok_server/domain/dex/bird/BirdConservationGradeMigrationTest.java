package org.devkor.apu.saerok_server.domain.dex.bird;

import jakarta.persistence.EntityManager;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BirdConservationGradeMigrationTest extends AbstractPostgresContainerTest {

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("Flyway 보호등급 데이터가 현재 도감 seed 기준 카운트와 일치한다")
    void conservationGradeCounts_matchSpreadsheet() {
        Object[] row = (Object[]) em.createNativeQuery("""
                SELECT
                    COUNT(*) FILTER (WHERE conservation_grade = 'GRADE_I'),
                    COUNT(*) FILTER (WHERE conservation_grade = 'GRADE_II'),
                    COUNT(*) FILTER (WHERE conservation_grade = 'NONE')
                FROM bird
                """).getSingleResult();

        assertThat(((Number) row[0]).longValue()).isEqualTo(16L);
        assertThat(((Number) row[1]).longValue()).isEqualTo(58L);
        assertThat(((Number) row[2]).longValue()).isEqualTo(512L);
    }
}
