package org.devkor.apu.saerok_server.domain.dex.bird.repository;

import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class BirdTriggerIntegrationTest extends AbstractPostgresContainerTest {
    // TODO: BirdHabitat, BirdImage, BirdResidency 등 관련 리포지토리 작업 완료 후 구현.
    //  V4__bird_update_trigger.sql의 트리거가 잘 돌아가는지 테스트
    //  테스트 목표: bird_habitat, bird_image, bird_residency 데이터 변경 시 bird.updated_at이 자동 갱신되는지 검증
}
