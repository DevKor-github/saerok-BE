# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# 빌드
./gradlew build
./gradlew compileJava   # 컴파일만 확인

# 로컬 서버 실행 (Docker 필요)
./up.sh
./down.sh

# 테스트 (Docker Desktop이 켜져 있어야 함)
./gradlew test
./gradlew clean test

# 단일 테스트 실행
./gradlew test --tests="org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepositoryTest.findById_존재하는_id_컬렉션_반환"

# 커버리지 리포트: ./build/reports/jacoco/html/index.html
```

## Architecture

**Java 21 + Spring Boot 3.4.4 / JPA(Hibernate) / PostgreSQL+PostGIS / MapStruct / Flyway**

### 도메인 목록

| 도메인 | 역할 |
|--------|------|
| `user` | 사용자 계정, 프로필 이미지 |
| `auth` | OAuth 소셜 로그인, JWT |
| `profile` | 타 유저 프로필 조회 |
| `dex` | 새 도감 (bird, 북마크, 잔류여부) |
| `collection` | 새 관찰 기록 + 좋아요/댓글/신고/동정요청 |
| `community` | 커뮤니티 메인·검색, 인기글 배치 |
| `freeboard` | 자유게시판 + 댓글/대댓글 |
| `notification` | 인앱·푸시 알림 (FCM, 배치 윈도우 처리) |
| `admin` | RBAC, 어드민 기능 |
| `announcement` | 공지사항 |
| `ad` | 광고 슬롯 |

### 패키지 레이아웃

```
domain/{name}/
├── api/                  # Controller, Request/Response DTO
├── application/          # CommandService, QueryService, application DTO
├── core/
│   ├── entity/           # JPA Entity, Enum
│   ├── repository/       # EntityManager 기반 리포지토리
│   └── service/          # 순수 도메인 서비스
└── mapper/               # MapStruct 인터페이스

global/
├── shared/               # Auditable, CreatedAtOnly, SoftDeletableAuditable, Pageable, 예외
├── core/config/          # 설정 클래스
└── security/             # JWT 필터, UserPrincipal
```

### 레이어 의존성 (단방향)

```
api → mapper → application → core
```

- `core`는 `api`·`application`을 참조하지 않음
- 도메인 간: 다른 도메인의 **서비스만** 주입 가능, 리포지토리 직접 참조 금지

## 핵심 컨벤션 요약

> 상세 내용은 **`CONVENTIONS.md`** 참조 (체크리스트 포함)

### Entity

- `Auditable` / `CreatedAtOnly` / `SoftDeletableAuditable` 중 하나 상속 필수
- ID: `@GeneratedValue(strategy = GenerationType.SEQUENCE)` — 커스텀 generator 이름 없이 사용, Hibernate가 `{table_name}_seq`에 자동 매핑
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` / `@Getter` 클래스 레벨 / `@Setter` 변경 필드에만
- 생성은 정적 팩토리 `of(...)` 사용

### Repository

- **Spring Data JPA 사용 금지** — `EntityManager` 직접 사용
- N+1 방지: 반드시 `JOIN FETCH`
- 배치 조회: `Map<Long, V>` 반환, 요청된 모든 ID에 기본값(0/false) 초기화 후 결과 덮어쓰기
- 페이지네이션: `size + 1` 조회 → 서비스에서 `hasNext` 판단 후 초과분 제거

### Service

- Command(쓰기): `@Transactional` / Query(읽기): `@Transactional(readOnly = true)`
- `org.springframework.transaction.annotation.Transactional` 사용 (`jakarta.transaction` X)
- 외부 시스템 호출(S3 삭제, 알림 등)은 `TransactionUtils.runAfterCommitOrNow()` 안에서

### Controller

- API 경로: `${api_prefix}/...` 접두사 필수 (현재 `/api/v1`)
- 인증 필요: `@PreAuthorize("isAuthenticated()")` + `@SecurityRequirement(name = "bearerAuth")`
- 공개 엔드포인트: `@PermitAll`
- `ResponseEntity` 감싸지 않고 DTO 직접 반환
- POST 201: `@ResponseStatus(HttpStatus.CREATED)` / DELETE 204: `@ResponseStatus(HttpStatus.NO_CONTENT)`

### DTO

- Request: `@Data` + `@NoArgsConstructor` + Bean Validation (`@NotBlank`, `@Size` 등)
- Response 단순형: `record` / 복잡형(중첩): `@Data` class
- 모든 필드에 `@Schema(description, example)` 필수
- 시간 필드: DB `TIMESTAMPTZ` → Entity `OffsetDateTime` → Response `LocalDateTime` (`OffsetDateTimeLocalizer.toSeoulLocalDateTime()`)

### Migration

- 파일: `src/main/resources/db/migration/V{번호}__{설명}.sql`
- 시퀀스: `CREATE SEQUENCE {table}_seq START WITH 1 INCREMENT BY 50;`
- Enum 컬럼: `VARCHAR(32)` + `DEFAULT 'ACTIVE'`
- 인덱스: 실제 쿼리에서 사용하는 컬럼에만 생성 (FK 컬럼, WHERE/ORDER BY 자주 사용)

## 댓글 공통 패턴

`collection` 댓글과 `freeboard` 댓글이 동일한 구조를 따름:
- 대댓글 depth 1만 허용
- 삭제 시: 대댓글 존재하면 `status = DELETED`(soft), 없으면 hard delete
- 삭제/밴 처리된 댓글 content는 `CommentReplacementConfig` 설정값으로 대체

## 테스트 구조

```java
@DataJpaTest
@Import(FreeBoardPostRepository.class)
@ActiveProfiles("test")
class FreeBoardPostRepositoryTest extends AbstractPostgresContainerTest {
    @Autowired TestEntityManager em;
    // 테스트 데이터: testsupport/builder/{Entity}Builder
}
```

- PostgreSQL Testcontainers (싱글턴) 사용 — Docker Desktop 필요
- 빌더: `src/test/java/.../testsupport/builder/` — `{Entity}Builder(TestEntityManager em)`

## 커밋 컨벤션

```
<type>(<scope>): <한국어 제목>
```

- **type**: `feat` `fix` `docs` `refactor` `test` `chore` `ci` `perf` `revert`
- **scope** (선택): `user` `dex` `coll` `map` `entity` `infra`
- husky + commitlint로 강제됨 (`npm run commit` 또는 `npx cz` 사용 가능)
