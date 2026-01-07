package org.devkor.apu.saerok_server.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트용 PostgreSQL 컨테이너를 관리하는 추상 클래스입니다.
 *
 * <p><strong>Singleton Container 패턴</strong>을 사용하여 모든 테스트가 하나의 컨테이너를 공유합니다.
 * 이를 통해 컨테이너 시작/중지 오버헤드를 최소화하고 테스트 실행 속도를 크게 향상시킵니다.
 *
 * <h2>주요 특징</h2>
 * <ul>
 *   <li>컨테이너는 처음 테스트 실행 시 한 번만 시작되고 JVM 종료 시까지 유지됩니다</li>
 *   <li>Flyway 마이그레이션은 컨테이너당 한 번만 실행됩니다</li>
 *   <li>테스트 간 데이터 격리는 {@code @Transactional} 애노테이션으로 보장됩니다</li>
 * </ul>
 *
 * <h2>사용 시 주의사항</h2>
 * <ul>
 *   <li><strong>테스트 격리</strong>: 리포지토리 테스트는 반드시 {@code @Transactional}을 사용하여
 *       각 테스트 메서드가 독립적으로 실행되도록 해야 합니다 (기본적으로 {@code @DataJpaTest}가 제공)</li>
 *   <li><strong>병렬 실행</strong>: 트랜잭션 격리 수준 덕분에 테스트 병렬 실행이 가능하지만,
 *       격리 수준을 낮춘 통합 테스트는 주의가 필요합니다</li>
 *   <li><strong>데이터 정리</strong>: {@code @Transactional} 테스트는 자동으로 롤백되므로
 *       별도 정리 로직이 불필요합니다</li>
 * </ul>
 *
 * <h2>성능 개선 효과</h2>
 * <ul>
 *   <li>테스트 클래스당 컨테이너 시작 시간(~3초) 제거</li>
 *   <li>Flyway 마이그레이션 중복 실행 제거 (2번째 테스트부터)</li>
 * </ul>
 */
public abstract class AbstractPostgresContainerTest {
    private static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("postgis/postgis:16-3.5-alpine")
                        .asCompatibleSubstituteFor("postgres")
        );

        // 컨테이너를 시작하고 JVM 종료 시까지 유지
        postgres.start();

        // JVM 종료 시 컨테이너 정리를 위한 shutdown hook 등록 (Testcontainers가 자동으로 처리하지만 명시적으로 추가)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (postgres.isRunning()) {
                postgres.stop();
            }
        }));
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
