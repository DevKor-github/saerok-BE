# Saerok Backend 코드 컨벤션

> Java 21 + Spring Boot 3.4.4 | JPA/Hibernate | PostgreSQL | MapStruct | Flyway
>
> Claude Code 에이전트는 이 문서의 컨벤션을 반드시 준수하여 코드를 작성해야 합니다.

---

## 목차

1. [패키지 구조](#1-패키지-구조)
2. [레이어 및 도메인 의존성 규칙](#2-레이어-및-도메인-의존성-규칙)
3. [엔티티](#3-엔티티)
4. [리포지토리](#4-리포지토리)
5. [서비스](#5-서비스)
6. [컨트롤러](#6-컨트롤러)
7. [DTO 및 입력 검증](#7-dto-및-입력-검증)
8. [매퍼](#8-매퍼)
9. [예외 처리 및 에러 응답](#9-예외-처리-및-에러-응답)
10. [트랜잭션 및 동시성](#10-트랜잭션-및-동시성)
11. [로깅](#11-로깅)
12. [데이터베이스 마이그레이션](#12-데이터베이스-마이그레이션)
13. [테스트](#13-테스트)
14. [공통 패턴](#14-공통-패턴)

---

## 1. 패키지 구조

### 도메인 패키지

```
domain/
└── {domainName}/
    ├── api/                          # 컨트롤러 & API DTO
    │   ├── {Entity}Controller.java
    │   └── dto/
    │       ├── request/              # 요청 DTO
    │       └── response/             # 응답 DTO
    │
    ├── application/                  # 비즈니스 로직 서비스
    │   ├── {Entity}CommandService.java
    │   ├── {Entity}QueryService.java
    │   └── dto/                      # 서비스 레이어 내부 DTO (Command 등)
    │
    ├── core/
    │   ├── entity/                   # JPA 엔티티 & Enum
    │   ├── repository/               # EntityManager 기반 리포지토리
    │   │   └── dto/                  # 리포지토리 전용 DTO (프로젝션 등)
    │   └── service/                  # 도메인 서비스 (순수 도메인 로직)
    │
    └── mapper/                       # MapStruct 매퍼 인터페이스
        └── {Entity}WebMapper.java
```

### 글로벌 패키지

```
global/
├── shared/
│   ├── entity/         # Auditable, CreatedAtOnly, SoftDeletableAuditable
│   ├── exception/      # NotFoundException, ForbiddenException, GlobalExceptionHandler
│   └── util/           # Pageable, OffsetDateTimeLocalizer, TransactionUtils 등
├── core/
│   ├── config/         # 애플리케이션 설정 (WebClient, Async, Firebase 등)
│   └── properties/     # @ConfigurationProperties 클래스 (CorsProperties 등)
└── security/           # 인증/인가 (UserPrincipal, JWT 필터 등)
```

---

## 2. 레이어 및 도메인 의존성 규칙

모놀리스에서 도메인 간 결합을 관리하는 것은 유지보수성의 핵심입니다.

### 레이어 의존성 (상위 → 하위만 허용)

```
api (Controller, Request/Response DTO)
 ↓
mapper (WebMapper — api ↔ application 변환 담당)
 ↓
application (CommandService, QueryService, Command DTO)
 ↓
core (Entity, Repository, Domain Service)
```

- **상위 레이어는 하위 레이어만 참조** — 역방향 의존 금지
- `core`는 `api`나 `application`을 참조하지 않음
- `api`는 `core/entity`를 직접 참조하지 않음 — 매퍼를 통해 변환
- `mapper`는 `api/dto`와 `application/dto`, `core/entity` 모두 참조 가능 (변환 레이어)

### 도메인 간 의존성

```
domain/freeboard → domain/user/application  ✅ 서비스 주입으로 기능 사용
domain/freeboard → domain/user/core/entity  ✅ JPA 관계 매핑 (@ManyToOne 등)
domain/freeboard → domain/user/core/repository  ❌ 다른 도메인의 리포지토리 직접 사용 금지
```

- **서비스 레이어**: 다른 도메인의 기능이 필요하면 해당 도메인의 **서비스를 주입**
- **엔티티 레이어**: JPA 관계 매핑(`@ManyToOne` 등)을 위해 다른 도메인의 엔티티 참조는 허용
- **리포지토리 레이어**: 다른 도메인의 리포지토리를 직접 주입하지 않음
- 공통으로 사용하는 엔티티/유틸은 `global/shared/`에 위치
- 도메인 간 공유 DTO가 필요하면 `global/shared/dto/`를 고려하되, 가능하면 각 도메인에서 자체 DTO 유지

### global 패키지 참조 규칙

- `global/shared/`는 모든 도메인에서 참조 가능 (공통 인프라)
- `global/security/`는 `api` 레이어에서만 참조 (`UserPrincipal` 등)
- 도메인 코드에서 `global/core/config/`를 직접 참조하지 않음 — 설정값은 주입받아 사용

---

## 3. 엔티티

### 기본 규칙

- **파일 위치**: `domain/{domainName}/core/entity/`
- **파일 당 하나의 엔티티** (Enum 포함 가능)
- 모든 엔티티는 아래 3가지 베이스 클래스 중 하나를 상속

| 베이스 클래스              | 용도                   | 제공 필드                              |
|---------------------------|------------------------|---------------------------------------|
| `Auditable`               | 일반 엔티티             | `createdAt`, `updatedAt`              |
| `CreatedAtOnly`           | 불변 엔티티 (좋아요 등)  | `createdAt`                           |
| `SoftDeletableAuditable`  | 소프트 삭제 엔티티       | `createdAt`, `updatedAt`, `deletedAt` |

### ID 생성 전략

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE)
private Long id;
```

- `GenerationType.SEQUENCE` 사용 (커스텀 generator name 지정하지 않음)
- Hibernate가 `{table_name}_seq` 시퀀스를 자동 매핑
- 마이그레이션에서 시퀀스를 직접 생성해야 함 (자동 생성 X)

### 클래스 어노테이션

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "free_board_post")
public class FreeBoardPost extends Auditable {
    
}
```

- `@Getter`: 클래스 레벨에 선언
- `@Setter`: 변경 가능한 **개별 필드**에만 선언 (클래스 레벨 X)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: JPA용
- `@Data` 사용 금지 (엔티티에서)

### 관계 매핑

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

- **항상** `fetch = FetchType.LAZY` 사용
- `@JoinColumn`에 `name` 속성 필수 (FK 컬럼명: `{entity}_id`)
- 필수 관계: `nullable = false`
- 선택적 관계 (예: 대댓글 `parent_id`): `nullable` 생략

### Enum 처리

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private CommentStatus status;
```

- 반드시 `EnumType.STRING` 사용 (ordinal 금지)
- DB 컬럼: `VARCHAR(32)`
- Enum 값: `UPPER_CASE` (예: `ACTIVE`, `DELETED`, `BANNED`)
- Enum 파일은 해당 엔티티와 같은 `core/entity/` 디렉토리에 위치

### 엔티티 생성 패턴

```java
// 정적 팩토리 메서드 사용
public static FreeBoardPost of(User user, String title, String content) {
    FreeBoardPost post = new FreeBoardPost();
    post.user = user;
    post.title = title;
    post.content = content;
    return post;
}
```

- 정적 팩토리 메서드 (`of(...)`) 선호
- 복잡한 생성: `@Builder` 허용
- 생성자 직접 호출 지양

### 도메인 메서드

```java
public boolean isActive() { return this.status == CommentStatus.ACTIVE; }
public boolean isReply() { return this.parent != null; }
public void softDelete() { this.status = CommentStatus.DELETED; }
```

- 비즈니스 판단 로직은 엔티티 내부에 작성
- `is`, `has` 접두사로 상태 확인 메서드
- 상태 변경 메서드는 동사형

---

## 4. 리포지토리

### 기본 규칙

- **파일 위치**: `domain/{domainName}/core/repository/`
- **Spring Data JPA 사용 금지** — `EntityManager` 직접 사용
- 엔티티당 하나의 리포지토리 클래스

```java
@Repository
@RequiredArgsConstructor
public class FreeBoardPostRepository {
    private final EntityManager em;
}
```

### 메서드 네이밍

| 접두사        | 반환 타입                | 예시                                    |
|--------------|-------------------------|----------------------------------------|
| `findById`   | `Optional<T>` 또는 `T`  | `findById(Long id)`                    |
| `findBy...`  | `List<T>`               | `findByUserId(Long userId)`            |
| `existsBy...`| `boolean`               | `existsByUserIdAndPostId(Long, Long)`  |
| `countBy...` | `long`                  | `countByPostId(Long postId)`           |
| `countBy...s`| `Map<Long, Long>`       | `countByPostIds(List<Long> ids)`       |

### 페이지네이션

```java
public List<FreeBoardPost> findAll(int offset, int size) {
    return em.createQuery(
            "SELECT p FROM FreeBoardPost p ORDER BY p.createdAt DESC", FreeBoardPost.class)
        .setFirstResult(offset)
        .setMaxResults(size + 1)  // hasNext 판단을 위해 +1
        .getResultList();
}
```

- `size + 1`개를 조회하여 다음 페이지 존재 여부 판단
- 서비스 레이어에서 초과분 제거 후 `hasNext` 반환

### N+1 방지

```java
// 반드시 JOIN FETCH 사용
"SELECT c FROM FreeBoardComment c JOIN FETCH c.user WHERE c.post.id = :postId"
```

### 배치 조회

```java
public Map<Long, Long> countByPostIds(List<Long> postIds) {
    // 결과 맵에 요청된 모든 ID를 기본값과 함께 초기화
    Map<Long, Long> result = new LinkedHashMap<>();
    postIds.forEach(id -> result.put(id, 0L));
    // 쿼리 결과로 덮어쓰기
    ...
    return result;
}
```

- 배치 메서드는 `Map<Long, V>` 반환 (키: 요청 ID)
- 요청된 모든 ID에 대해 기본값 설정 (0, false 등)
- `LinkedHashMap`으로 삽입 순서 보장

### 트랜잭션

- 리포지토리에 `@Transactional` 선언하지 않음 — 서비스 레이어에서 관리

---

## 5. 서비스

### Command/Query 분리

| 구분            | 클래스명                        | 역할                    | 트랜잭션                        |
|-----------------|-------------------------------|-------------------------|-------------------------------|
| **Command**     | `{Entity}CommandService`      | 생성/수정/삭제            | `@Transactional`              |
| **Query**       | `{Entity}QueryService`        | 조회 및 데이터 조합        | `@Transactional(readOnly = true)` |

### 기본 구조

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // QueryService인 경우
public class FreeBoardPostQueryService {
    private final FreeBoardPostRepository freeBoardPostRepository;
    private final FreeBoardPostCommentRepository commentRepository;
    private final UserProfileImageUrlService userProfileImageUrlService;
    private final FreeBoardPostWebMapper mapper;
}
```

- `@RequiredArgsConstructor` + `private final` 필드로 생성자 주입
- 필드 순서: 리포지토리 → 외부 서비스 → 유틸리티 → 매퍼

### 메서드 반환 타입

```java
// CommandService
public Long createPost(CreatePostCommand command) { ... }      // 생성 → ID 반환
public void deletePost(DeletePostCommand command) { ... }       // 삭제 → void
public UpdatePostResponse updatePost(UpdatePostCommand cmd) {}  // 수정 → 응답 DTO

// QueryService
public GetPostDetailResponse getPostDetail(Long postId, Long userId) { ... }
public GetPostListResponse getPosts(PostQueryCommand command) { ... }
```

### Command 객체

```java
// application/dto/ 디렉토리에 위치
public record CreatePostCommand(Long userId, String title, String content) {}
public record PostQueryCommand(Integer page, Integer size) implements Pageable {}
```

- `record` 사용 (불변)
- 페이지네이션 필요 시 `Pageable` 인터페이스 구현

### 비즈니스 로직 패턴

```java
// 존재 확인
FreeBoardPost post = repository.findById(postId)
    .orElseThrow(() -> new NotFoundException("해당 id의 게시글이 존재하지 않아요"));

// 권한 확인
if (!post.getUser().getId().equals(userId)) {
    throw new ForbiddenException("해당 게시글에 대한 권한이 없어요");
}

// 입력 검증
if (command.title().length() > MAX_TITLE_LENGTH) {
    throw new BadRequestException("제목은 " + MAX_TITLE_LENGTH + "자 이하여야 해요");
}
```

### 도메인 서비스

- **파일 위치**: `domain/{domainName}/core/service/`
- 상태 없는 순수 도메인 로직 (예: `CommentContentResolver`)
- `@Service` 없이 `@Component` 또는 Spring Bean으로 등록

---

## 6. 컨트롤러

### 기본 구조

```java
@RestController
@RequestMapping("${api_prefix}/community/freeboard/posts")
@RequiredArgsConstructor
@Tag(name = "Freeboard Post API")
public class FreeBoardPostController {
    private final FreeBoardPostCommandService commandService;
    private final FreeBoardPostQueryService queryService;
    private final FreeBoardPostWebMapper mapper;
}
```

- API 경로: `${api_prefix}` 접두사 사용 (프로퍼티에서 주입)
- RESTful 경로: `/collections`, `/collections/{collectionId}`, `/{id}/like`

### HTTP 메서드 매핑

| HTTP Method   | 어노테이션       | 용도       | 응답 코드  |
|--------------|-----------------|-----------|-----------|
| POST         | `@PostMapping`  | 생성       | 201       |
| GET          | `@GetMapping`   | 조회       | 200       |
| PATCH        | `@PatchMapping` | 부분 수정   | 200       |
| DELETE       | `@DeleteMapping`| 삭제       | 204       |

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
@PreAuthorize("isAuthenticated()")
```

### 인증/인가

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
@PreAuthorize("isAuthenticated()")
@Operation(security = @SecurityRequirement(name = "bearerAuth"))
public CreatePostResponse createPost(
    @AuthenticationPrincipal UserPrincipal userPrincipal,
    @Valid @RequestBody CreatePostRequest request
) {
    Long userId = userPrincipal.getId();
    CreatePostCommand command = mapper.toCreatePostCommand(request, userId);
    Long postId = commandService.createPost(command);
    return new CreatePostResponse(postId);
}
```

- 인증 필요: `@PreAuthorize("isAuthenticated()")`
- 사용자 정보: `@AuthenticationPrincipal UserPrincipal userPrincipal`
- 요청 바디에 `@Valid` 필수 — Bean Validation 트리거
- 반환 타입: **Response DTO를 직접 반환** (`ResponseEntity`로 감싸지 않음)

### Swagger 문서화

```java
@Operation(
    summary = "게시글 작성",
    description = "자유게시판 게시글을 작성합니다.",
    security = @SecurityRequirement(name = "bearerAuth"),
    responses = {
        @ApiResponse(responseCode = "201", description = "게시글 작성 성공",
            content = @Content(schema = @Schema(implementation = CreatePostResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
    }
)
```

- 모든 엔드포인트에 `@Operation` 필수
- `responses`: 가능한 모든 응답 코드 명시 (200, 201, 400, 401, 403, 404)

### 컨트롤러 역할

- **컨트롤러는 얇게 유지** — 비즈니스 로직 금지
- 요청 → Command 변환은 매퍼에 위임
- 서비스 호출 후 응답 DTO 반환

---

## 7. DTO 및 입력 검증

### 요청 DTO (Request)

```java
// domain/{domainName}/api/dto/request/
@Data
@NoArgsConstructor
public class CreatePostRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "게시글 제목", example = "오늘 본 새", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank
    @Schema(description = "게시글 내용", example = "한강에서 왜가리를 봤어요", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
```

- `@Data` + `@NoArgsConstructor` (POJO 스타일)
- 모든 필드에 `@Schema(description, example)` 필수
- 필수 필드: `requiredMode = Schema.RequiredMode.REQUIRED`
- 선택 필드: `requiredMode = Schema.RequiredMode.NOT_REQUIRED`

### 입력 검증 전략

검증은 **두 단계**로 나뉩니다:

| 검증 위치          | 검증 종류               | 예시                                       |
|-------------------|------------------------|--------------------------------------------|
| **Request DTO**   | 형식 검증 (Bean Validation) | `@NotBlank`, `@Size(max=100)`, `@NotNull` |
| **Service 레이어** | 비즈니스 규칙 검증         | 중복 확인, 권한 확인, 도메인 제약조건          |

```java
// ✅ Request DTO — 형식 검증 (null, 빈 문자열, 길이 등)
@NotBlank @Size(max = 255) String title;
@NotNull Boolean sendNotification;
@Valid List<ImageRequest> images;  // 중첩 객체는 @Valid로 전파

// ✅ Service — 비즈니스 규칙 검증
if (!post.getUser().getId().equals(userId))
    throw new ForbiddenException("해당 게시글에 대한 권한이 없어요");
```

- 컨트롤러에서 `@Valid` 또는 `@Validated`로 Bean Validation 트리거
- 중첩 객체/컬렉션에는 `@Valid` 어노테이션 필수
- 비즈니스 규칙 위반은 서비스에서 커스텀 예외로 처리

### 응답 DTO (Response)

```java
// domain/{domainName}/api/dto/response/
// 단순 응답: record 사용
@Schema(description = "게시글 작성 응답")
public record CreatePostResponse(
    @Schema(description = "게시글 ID", example = "1")
    Long postId
) {}

// 복잡한 응답: @Data 사용 (중첩 객체 포함)
@Data
@Schema(description = "게시글 상세 조회 응답")
public class GetPostDetailResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "작성자 정보")
    private UserInfo user;

    @Data
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "42")
        private Long userId;

        @Schema(description = "닉네임", example = "새덕후")
        private String nickname;
    }
}
```

### 필드 네이밍 규칙

| 카테고리     | 패턴                    | 예시                                     |
|-------------|------------------------|------------------------------------------|
| ID          | `{entity}Id`           | `postId`, `userId`, `commentId`          |
| 시간         | `createdAt`, `updatedAt`| `LocalDateTime` 타입                     |
| 이미지 URL   | `{prefix}ImageUrl`     | `profileImageUrl`, `thumbnailImageUrl`   |
| 카운트       | `{entity}Count`        | `likeCount`, `commentCount`              |
| 상태 플래그   | `is{State}`            | `isLiked`, `isMine`, `isActive`          |

### 내부 Command DTO

```java
// domain/{domainName}/application/dto/
public record PostQueryCommand(Integer page, Integer size) implements Pageable {}
```

- `record` 사용 (불변)
- Swagger 어노테이션 없음 (내부 전용)
- 페이지네이션 시 `Pageable` 구현

---

## 8. 매퍼

### 기본 구조

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FreeBoardPostWebMapper {

    @Mapping(target = "userId", source = "userId")
    CreatePostCommand toCreatePostCommand(CreatePostRequest request, Long userId);

    @Mapping(target = "createdAt", expression = "java(OffsetDateTimeLocalizer.toSeoulLocalDateTime(post.getCreatedAt()))")
    GetPostDetailResponse toGetPostDetailResponse(FreeBoardPost post, String imageUrl);
}
```

- **파일 위치**: `domain/{domainName}/mapper/`
- 인터페이스로 선언 (`class` 아님)
- `componentModel = MappingConstants.ComponentModel.SPRING` 필수

### 커스텀 매핑

```java
@Named("resolveContent")
default String resolveContent(FreeBoardComment comment) {
    return comment.isActive() ? comment.getContent() : "삭제된 댓글입니다.";
}

@Mapping(target = "content", source = "comment", qualifiedByName = "resolveContent")
CommentDto toCommentDto(FreeBoardComment comment);
```

- `@Named` + `default` 메서드로 커스텀 로직
- `qualifiedByName`으로 참조

### 시간 변환

```java
// OffsetDateTime → LocalDateTime (서울 시간)
@Mapping(target = "createdAt",
    expression = "java(OffsetDateTimeLocalizer.toSeoulLocalDateTime(entity.getCreatedAt()))")
```

- DB: `TIMESTAMPTZ` → Entity: `OffsetDateTime` → Response: `LocalDateTime` (서울)
- 항상 `OffsetDateTimeLocalizer.toSeoulLocalDateTime()` 사용

---

## 9. 예외 처리 및 에러 응답

### 예외 타입

| 예외                     | HTTP 코드 | 사용 상황                    |
|-------------------------|-----------|----------------------------|
| `NotFoundException`     | 404       | 엔티티가 존재하지 않을 때      |
| `ForbiddenException`    | 403       | 권한이 없을 때               |
| `BadRequestException`   | 400       | 비즈니스 규칙 위반 시          |
| `UnauthorizedException` | 401       | 인증 실패 시                 |

### 메시지 규칙

```java
throw new NotFoundException("해당 id의 게시글이 존재하지 않아요");
throw new ForbiddenException("해당 게시글에 대한 권한이 없어요");
throw new BadRequestException("제목은 50자 이하여야 해요");
```

- **한국어** 사용자 친화적 메시지
- 존재하지 않음: `"해당 id의 {엔티티}이/가 존재하지 않아요"`
- 권한 없음: `"해당 {엔티티}에 대한 권한이 없어요"`
- 모든 예외는 `RuntimeException` 상속 (unchecked)

### 에러 응답 포맷

```java
// GlobalExceptionHandler가 반환하는 통일된 에러 응답
public record ErrorResponse(
    int status,      // HTTP 상태 코드
    String message   // 사용자 친화적 에러 메시지
) {}
```

- **응답 래퍼(envelope) 패턴을 사용하지 않음** — DTO를 직접 반환
- 성공 응답: 각 도메인의 Response DTO 직접 반환
- 에러 응답: `ErrorResponse` record로 통일
- `GlobalExceptionHandler`에서 예외 타입별로 HTTP 상태 코드 매핑

---

## 10. 트랜잭션 및 동시성

### 트랜잭션 관리

```java
import org.springframework.transaction.annotation.Transactional;  // ← Spring 것 사용

// CommandService — 쓰기 트랜잭션
@Service
@Transactional
public class PostCommandService { ... }

// QueryService — 읽기 전용 트랜잭션
@Service
@Transactional(readOnly = true)
public class PostQueryService { ... }
```

- `org.springframework.transaction.annotation.Transactional` 사용 (`jakarta.transaction.Transactional` 아님)
- `@Transactional`은 **서비스 레이어에서만** 선언 (리포지토리, 컨트롤러 X)
- 기본 전파: `Propagation.REQUIRED` (명시적 변경하지 않음)
- `readOnly = true`는 QueryService에서만 사용 (Hibernate flush 모드 최적화)

### 트랜잭션 후처리 (After-Commit)

외부 시스템 호출(S3 삭제, 알림 발송 등)은 **트랜잭션 커밋 이후** 실행해야 합니다.

```java
// ✅ 올바른 패턴 — 커밋 후 실행
TransactionUtils.runAfterCommitOrNow(() -> {
    s3Client.deleteObject(imageKey);
});

// ❌ 잘못된 패턴 — 트랜잭션 내에서 외부 호출
s3Client.deleteObject(imageKey);  // 롤백 시 이미 삭제된 파일 복구 불가
```

- `TransactionUtils.runAfterCommitOrNow()` 유틸리티 사용
- 트랜잭션이 없는 컨텍스트에서는 즉시 실행
- 용도: S3 파일 삭제, 푸시 알림 발송, 외부 API 호출

### 동시성 제어

```java
// Striped Lock (Guava) — 배치 처리 등 동시성 제어 시
private final Striped<Lock> stripedLocks = Striped.lock(256);

public void processBatch(String key) {
    Lock lock = stripedLocks.get(key);
    lock.lock();
    try {
        // 임계 영역
    } finally {
        lock.unlock();
    }
}
```

- JPA 낙관적/비관적 락(`@Version`, `@Lock`)은 현재 사용하지 않음
- 동시성 이슈가 발생하면 Striped Lock 패턴 사용
- `synchronized` 블록 사용 지양 — 확장성 제한

---

## 11. 로깅

### 기본 규칙

```java
@Slf4j  // Lombok
@Service
public class NotificationBatchService {

    public void process() {
        log.info("배치 처리 시작: batchId={}", batchId);
        log.warn("Redis에서 배치 데이터 역직렬화에 실패했습니다: {}", key);
        log.error("FCM 발송 실패: userId={}, error={}", userId, e.getMessage(), e);
    }
}
```

- `@Slf4j` (Lombok) 사용 — `LoggerFactory.getLogger()` 직접 호출 지양
- 문자열 연결 대신 **플레이스홀더 `{}`** 사용 (성능)
- 에러 로그에는 예외 객체를 마지막 인자로 전달 (스택 트레이스 포함)

### 로그 레벨 가이드

| 레벨    | 용도                                          |
|--------|----------------------------------------------|
| `ERROR`| 즉시 대응 필요한 장애 (외부 시스템 실패, 데이터 정합성 오류) |
| `WARN` | 비정상이지만 자동 복구 가능한 상황 (역직렬화 실패, 재시도) |
| `INFO` | 주요 비즈니스 이벤트 (배치 시작/완료, 사용자 가입)       |
| `DEBUG`| 개발 중 디버깅용 (운영에서는 비활성화)                 |

- 컨트롤러에서는 로깅하지 않음 (요청/응답 로깅은 필터/인터셉터에서)
- 개인정보(이메일, 전화번호 등)는 로그에 남기지 않음

---

## 12. 데이터베이스 마이그레이션

### 파일 규칙

- **파일 위치**: `src/main/resources/db/migration/`
- **네이밍**: `V{번호}__{설명}.sql` (이중 언더스코어 필수)
- 예: `V88__create_freeboard_domain.sql`

### 시퀀스

```sql
CREATE SEQUENCE free_board_post_seq START WITH 1 INCREMENT BY 50;
```

- 이름: `{테이블명}_seq` (단수형)
- `INCREMENT BY 50` (Hibernate 기본값)
- `START WITH 1`

### 테이블

```sql
CREATE TABLE free_board_post (
    id         BIGINT       NOT NULL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(100) NOT NULL,
    content    TEXT         NOT NULL,
    status     VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 네이밍 규칙

| 구분         | 패턴                              | 예시                            |
|-------------|----------------------------------|--------------------------------|
| 테이블       | `snake_case` (단수)               | `free_board_post`              |
| PK          | `id`                             | `id BIGINT NOT NULL PRIMARY KEY`|
| FK          | `{참조_테이블}_id`                 | `user_id`, `post_id`           |
| 시간 컬럼    | `created_at`, `updated_at`       | `TIMESTAMPTZ`                  |
| 상태 컬럼    | `status`                         | `VARCHAR(32)`                  |
| FK 제약조건  | `fk_{from}_{to}`                 | `fk_free_board_post_user`      |
| 인덱스       | `idx_{테이블}_{컬럼}`              | `idx_free_board_post_user_id`  |

### Enum 타입

- 대부분 `VARCHAR(32)`로 저장 (유연성)
- PostgreSQL `CREATE TYPE ... AS ENUM`은 특수한 경우에만 사용

### 인덱스 전략

인덱스는 마이그레이션에서 테이블과 함께 생성합니다.

```sql
-- 단일 컬럼 인덱스: FK나 자주 조회하는 컬럼
CREATE INDEX idx_free_board_post_user_id ON free_board_post(user_id);

-- 복합 인덱스: 여러 조건으로 함께 조회되는 컬럼
CREATE INDEX idx_bird_id_suggestion_collection_bird_type
    ON bird_id_suggestion(user_bird_collection_id, bird_id, type);

-- 시간 기반 인덱스: 통계/분석 쿼리용
CREATE INDEX idx_daily_stat_metric_date ON daily_stat(metric, date);
```

**인덱스 추가 기준:**

| 상황                           | 인덱스 필요 여부 |
|-------------------------------|----------------|
| FK 컬럼                       | 항상 추가        |
| WHERE 절에서 자주 사용되는 컬럼   | 추가            |
| 정렬(`ORDER BY`)에 자주 사용    | 추가 고려        |
| 유니크 제약 조건                 | UNIQUE INDEX   |
| 카디널리티 낮은 컬럼 (status 등) | 단독 인덱스 지양  |

- FK에는 `ON DELETE CASCADE` 사용 여부를 도메인 요구사항에 맞게 결정
- 복합 인덱스 컬럼 순서: 선택도(selectivity)가 높은 컬럼을 앞에 배치

---

## 13. 테스트

### 기본 구조

```java
@DataJpaTest
@Import(FreeBoardPostRepository.class)
@ActiveProfiles("test")
class FreeBoardPostRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired FreeBoardPostRepository repository;
    @Autowired TestEntityManager em;
}
```

### 테스트 데이터 빌더

```java
User user = new UserBuilder(em).build();
FreeBoardPost post = new FreeBoardPostBuilder(em).user(user).build();
em.flush();
em.clear();
```

- **파일 위치**: `testsupport/builder/`
- Fluent API 패턴 (`{Entity}Builder`)
- `TestEntityManager`를 생성자에서 받음

### 테스트 네이밍

```java
@Test
@DisplayName("게시글 작성자만 삭제할 수 있다")
void deletePost_notOwner_throwsForbiddenException() { ... }
```

- `@DisplayName`: 한국어로 명확한 설명
- 메서드명: `{메서드}_{조건}_{기대결과}` (영문)

---

## 14. 공통 패턴

### 페이지네이션

```java
// Pageable 인터페이스 구현
public record PostQueryCommand(Integer page, Integer size) implements Pageable {}

// 서비스에서 처리
List<Post> posts = repository.findAll(pageable.offset(), pageable.size());
boolean hasNext = posts.size() > pageable.size();
if (hasNext) posts.remove(posts.size() - 1);
```

- `page`와 `size` 모두 필수이거나 모두 null
- `size + 1` 조회 → `hasNext` 판단 → 초과분 제거

### 댓글 소프트 삭제

```java
// 대댓글이 있으면 소프트 삭제 (상태 변경)
if (commentRepository.hasReplies(commentId)) {
    comment.softDelete();  // status → DELETED
} else {
    commentRepository.delete(comment);  // 하드 삭제
}
```

### 사용자 프로필 이미지

```java
// 단건
String imageUrl = userProfileImageUrlService.getProfileImageUrl(userId);

// 배치 (N+1 방지)
Map<Long, String> imageUrls = userProfileImageUrlService.getProfileImageUrls(userIds);
```

### 좋아요 토글

```java
// 존재하면 삭제, 없으면 생성
if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
    likeRepository.deleteByUserIdAndPostId(userId, postId);
} else {
    likeRepository.save(Like.of(userId, postId));
}
```

### 알림 발송 (DSL)

```java
notifyAction
    .by(userId)
    .on(postId)
    .did(ActionKind.COMMENT)
    .comment()
    .to(targetUserId);
```

---

## 요약: 핵심 체크리스트

새로운 도메인이나 기능을 추가할 때 아래를 확인하세요:

### 아키텍처
- [ ] 레이어 의존성 규칙을 지키는가 (api → mapper → application → core)
- [ ] 다른 도메인의 리포지토리를 직접 참조하지 않는가 (서비스를 통해 접근)
- [ ] 서비스가 Command/Query로 분리되어 있는가

### 엔티티 & DB
- [ ] 엔티티가 `Auditable` / `CreatedAtOnly` / `SoftDeletableAuditable` 중 하나를 상속하는가
- [ ] ID 생성에 `GenerationType.SEQUENCE`를 사용하는가
- [ ] 리포지토리가 `EntityManager`를 직접 사용하는가 (Spring Data JPA X)
- [ ] 마이그레이션 시퀀스가 `INCREMENT BY 50`인가
- [ ] FK 컬럼에 인덱스를 추가했는가

### 쿼리 & 성능
- [ ] 쿼리에 `JOIN FETCH`로 N+1을 방지했는가
- [ ] 배치 조회 메서드가 요청된 모든 ID에 대해 기본값을 설정하는가
- [ ] 페이지네이션이 `size + 1` 패턴을 사용하는가

### API & DTO
- [ ] 모든 DTO 필드에 `@Schema(description, example)`가 있는가
- [ ] Request DTO에 Bean Validation 어노테이션(`@NotBlank`, `@Size` 등)이 있는가
- [ ] 모든 응답 시간이 서울 시간(`OffsetDateTimeLocalizer`)으로 변환되는가
- [ ] 매퍼가 `componentModel = SPRING`인 MapStruct 인터페이스인가

### 안정성
- [ ] 예외 메시지가 한국어 사용자 친화적인가
- [ ] 외부 시스템 호출이 `TransactionUtils.runAfterCommitOrNow()` 안에 있는가
- [ ] `@Slf4j`로 주요 비즈니스 이벤트와 에러를 로깅하는가
