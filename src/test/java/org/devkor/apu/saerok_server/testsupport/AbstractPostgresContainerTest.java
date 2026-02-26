package org.devkor.apu.saerok_server.testsupport;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트용 PostgreSQL 컨테이너를 관리하는 추상 클래스입니다.
 *
 * <p>Testcontainers를 통해 통합 테스트나 슬라이스 테스트 실행 시
 * 독립적인 Postgres+PostGIS 환경을 제공합니다.
 *
 * <p>컨테이너는 테스트 시작 시 자동으로 기동되고, 종료 시 자동으로 중단됩니다.
 * 데이터소스와 Flyway 설정은 컨테이너 연결 정보로 재정의됩니다.
 *
 * <p>이 클래스를 상속하면 테스트마다 중복 설정 없이 일관된 환경을 사용할 수 있습니다.
 */
public abstract class AbstractPostgresContainerTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.5-alpine").asCompatibleSubstituteFor("postgres")
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
