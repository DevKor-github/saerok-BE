# 테스트 가이드

## 테스트 성능 최적화

이 프로젝트는 **Singleton Container 패턴**을 사용하여 테스트 실행 속도를 크게 향상시켰습니다.

### 주요 최적화 사항

1. **Singleton PostgreSQL Container**
   - 모든 테스트가 하나의 Testcontainers 인스턴스를 공유
   - 컨테이너는 첫 테스트 실행 시 한 번만 시작되고 JVM 종료 시까지 유지
   - 테스트 클래스마다 컨테이너를 시작/중지하는 오버헤드 제거

2. **Flyway 마이그레이션 최적화**
   - 컨테이너당 한 번만 마이그레이션 실행
   - `baseline-on-migrate: true` 설정으로 이미 마이그레이션된 DB 처리
   - `clean-disabled: true`로 불필요한 스키마 삭제 방지

3. **트랜잭션 기반 테스트 격리**
   - `@DataJpaTest`가 자동으로 `@Transactional` 제공
   - 각 테스트 메서드는 자동으로 롤백되어 독립성 보장
   - 별도 데이터 정리 로직 불필요

### 성능 개선 효과

- **이전**: 리포지토리 테스트 1개당 ~8초 (컨테이너 시작 + Flyway 마이그레이션)
- **현재**: 첫 테스트 ~8초, 이후 테스트 ~1-2초
- **전체 테스트 스위트**: 약 60-70% 시간 단축

## 리포지토리 테스트 작성 가이드

### 기본 구조

```java
@DataJpaTest  // 자동으로 @Transactional 포함
@Import(YourRepository.class)
@ActiveProfiles("test")
class YourRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    YourRepository repository;

    @Test
    void testSomething() {
        // Given
        YourEntity entity = new YourEntity();
        em.persist(entity);
        em.flush();

        // When
        YourEntity found = repository.findById(entity.getId()).orElseThrow();

        // Then
        assertThat(found).isNotNull();

        // 테스트 종료 시 자동 롤백 - 데이터 정리 불필요
    }
}
```

### 주의사항

#### ✅ DO

```java
@DataJpaTest
class GoodTest extends AbstractPostgresContainerTest {
    @Test
    void testWithAutoRollback() {
        // 테스트 로직
        // 자동 롤백됨 - 다음 테스트에 영향 없음
    }
}
```

#### ❌ DON'T

```java
@DataJpaTest
@Commit  // ❌ 롤백 비활성화하지 마세요!
class BadTest extends AbstractPostgresContainerTest {
    @Test
    void testWithCommit() {
        // 데이터가 커밋되어 다른 테스트에 영향
    }
}
```

```java
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)  // ❌ 트랜잭션 비활성화하지 마세요!
class BadTest extends AbstractPostgresContainerTest {
    @Test
    void testWithoutTransaction() {
        // 테스트 격리 깨짐
    }
}
```

### 통합 테스트 작성 시 주의사항

`@SpringBootTest`를 사용하는 통합 테스트에서도 Singleton Container의 혜택을 받을 수 있습니다:

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // 명시적으로 추가 필요
class IntegrationTest extends AbstractPostgresContainerTest {

    @Test
    void integrationTest() {
        // 통합 테스트 로직
    }
}
```

## 병렬 테스트 실행

Singleton Container 패턴은 병렬 테스트 실행과 호환됩니다:

```bash
# Gradle에서 병렬 테스트 실행
./gradlew test --parallel --max-workers=4
```

트랜잭션 격리 덕분에 각 테스트가 독립적으로 실행되므로 안전합니다.

## 트러블슈팅

### Flyway 마이그레이션 충돌

테스트 실행 중 Flyway 오류가 발생하면:

```yaml
# application-test.yml
spring:
  flyway:
    baseline-on-migrate: true
    clean-disabled: true
```

설정이 있는지 확인하세요.

### 테스트 간 데이터 오염

- `@DataJpaTest`가 적용되어 있는지 확인
- `@Commit`이나 `@Transactional(propagation = NOT_SUPPORTED)` 사용 여부 확인
- 필요시 `@Sql`로 특정 데이터 초기화:

```java
@Test
@Sql("/test-data/cleanup.sql")
void testWithCleanup() {
    // 테스트 로직
}
```

## 참고 자료

- [Testcontainers 공식 문서](https://www.testcontainers.org/)
- [Spring Boot Testing Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
