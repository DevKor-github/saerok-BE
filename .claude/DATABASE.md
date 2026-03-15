# Saerok Backend 데이터베이스 가이드

> PostgreSQL + PostGIS | Flyway | Hibernate Sequence (INCREMENT BY 50)

---

## 목차

1. [Flyway 설정](#1-flyway-설정)
2. [스키마 전체 구조](#2-스키마-전체-구조)
3. [도메인별 테이블 상세](#3-도메인별-테이블-상세)
4. [컨벤션 정리](#4-컨벤션-정리)
5. [마이그레이션 작성 가이드](#5-마이그레이션-작성-가이드)
6. [주요 설계 패턴](#6-주요-설계-패턴)
7. [마이그레이션 이력 요약](#7-마이그레이션-이력-요약)

---

## 1. Flyway 설정

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
```

- **마이그레이션 위치**: `src/main/resources/db/migration/`
- **파일 네이밍**: `V{번호}__{설명}.sql` (이중 언더스코어 `__` 필수)
- **베이스라인**: V0 (빈 DB에서 시작해도 모든 마이그레이션 적용)
- **DDL 자동 생성 비활성**: `spring.jpa.hibernate.ddl-auto: none` — 스키마는 Flyway로만 관리

---

## 2. 스키마 전체 구조

### 확장 모듈

```sql
CREATE EXTENSION IF NOT EXISTS postgis;  -- 위치 기반 기능 (GEOMETRY, GIST 인덱스)
```

### 도메인 맵

```
┌─────────────────────────────────────────────────────────────────────┐
│                          users (중심 엔티티)                           │
├───────────┬──────────────┬──────────────┬──────────────┬────────────┤
│  Bird     │  Collection  │  Community   │ Notification │   Admin    │
│  Reference│  & Social    │  (Freeboard) │              │            │
├───────────┼──────────────┼──────────────┼──────────────┼────────────┤
│ bird      │ user_bird_   │ free_board_  │ notification │ admin_     │
│ bird_image│  collection  │  post        │ notification_│  audit_log │
│ bird_     │ .._image     │ free_board_  │  setting     │ permission │
│  habitat  │ .._like      │  post_       │ user_device  │ role       │
│ bird_     │ .._comment   │  comment     │              │ role_      │
│  residency│ .._comment_  │              │              │  permission│
│ rarity_   │  like        │              │              │ user_role  │
│  type     │ .._report    │              │              │ announce-  │
│ residency_│ .._comment_  │              │              │  ment      │
│  type     │  report      │              │              │ ad / ad_   │
│ bird_id_  │ user_bird_   │              │              │  slot /    │
│  suggestion│ bookmark    │              │              │  placement │
│           │ popular_     │              │              │            │
│           │  collection  │              │              │            │
│           │ bird_id_     │              │              │            │
│           │  request_    │              │              │            │
│           │  history     │              │              │            │
└───────────┴──────────────┴──────────────┴──────────────┴────────────┘
                              │
                        daily_stat (통계/분석)
                        user_activity_ping (활동 추적)
```

---

## 3. 도메인별 테이블 상세

### A. 사용자 (User)

#### `users`

| 컬럼                          | 타입           | 제약조건                    | 비고                         |
|------------------------------|---------------|---------------------------|------------------------------|
| `id`                         | BIGINT        | PK                        |                              |
| `name`                       | VARCHAR(50)   |                           |                              |
| `nickname`                   | VARCHAR(50)   | UNIQUE, nullable          | 회원가입 완료 후 설정           |
| `email`                      | VARCHAR(320)  | UNIQUE                    |                              |
| `phone`                      | VARCHAR(20)   | UNIQUE                    |                              |
| `gender`                     | VARCHAR(30)   |                           | 원래 ENUM → VARCHAR 전환 (V12) |
| `birth_date`                 | DATE          |                           |                              |
| `signup_status`              | VARCHAR       | NOT NULL                  | 가입 진행 상태                 |
| `signup_completed_at`        | TIMESTAMPTZ   |                           | 트리거로 자동 설정 (V62)        |
| `signup_source`              | VARCHAR(30)   |                           | 가입 경로 (V86)               |
| `default_profile_image_variant` | SMALLINT   | DEFAULT 0                 | 0~5 기본 아바타 인덱스 (V39)    |
| `is_super_admin`             | BOOLEAN       | DEFAULT false             | 슈퍼 관리자 플래그 (V73)        |
| `created_at`                 | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW     |                              |
| `updated_at`                 | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW     |                              |
| `deleted_at`                 | TIMESTAMPTZ   |                           | 소프트 삭제                    |

**인덱스**: `idx_users_signup_completed_at`, `idx_users_deleted_at`
**트리거**: `signup_status` → COMPLETED 전환 시 `signup_completed_at` 자동 설정 (1회성)
**시퀀스**: `users_seq`

#### `user_refresh_token`

| 컬럼                 | 타입           | 제약조건                         |
|---------------------|---------------|--------------------------------|
| `id`                | BIGINT        | PK                             |
| `user_id`           | BIGINT        | FK → users                     |
| `refresh_token_hash`| VARCHAR(100)  | UNIQUE (user_id, hash)         |
| `user_agent`        | VARCHAR(255)  |                                |
| `ip_address`        | VARCHAR(45)   |                                |
| `issued_at`         | TIMESTAMPTZ   |                                |
| `expires_at`        | TIMESTAMPTZ   |                                |
| `revoked_at`        | TIMESTAMPTZ   |                                |

**시퀀스**: `user_refresh_token_seq`

#### `user_device`

| 컬럼          | 타입           | 제약조건                                |
|--------------|---------------|----------------------------------------|
| `id`         | BIGINT        | PK                                     |
| `user_id`    | BIGINT        | FK → users (CASCADE)                   |
| `device_id`  | VARCHAR(256)  |                                        |
| `token`      | VARCHAR(512)  | FCM/APNs 토큰                           |
| `platform`   | VARCHAR(16)   | IOS / ANDROID (V87)                    |
| `created_at` | TIMESTAMPTZ   |                                        |
| `updated_at` | TIMESTAMPTZ   |                                        |

**유니크**: `(user_id, device_id, platform)` — V87에서 platform 추가
**인덱스**: `idx_user_device_user`, `idx_user_device_token`
**시퀀스**: `user_device_seq`

#### `user_profile_image`

| 컬럼          | 타입           | 제약조건                  |
|--------------|---------------|--------------------------|
| `id`         | BIGINT        | PK                       |
| `user_id`    | BIGINT        | FK → users, UNIQUE       |
| `object_key` | TEXT          | S3 경로                   |
| `content_type` | VARCHAR(50) |                          |
| `created_at` | TIMESTAMPTZ   |                          |

**시퀀스**: `user_profile_image_seq`

#### `user_activity_ping`

| 컬럼          | 타입           | 제약조건                  |
|--------------|---------------|--------------------------|
| `id`         | BIGINT        | PK                       |
| `user_id`    | BIGINT        | FK → users               |
| `occurred_at`| TIMESTAMPTZ   |                          |
| `source`     | TEXT          | DEFAULT 'refresh_token'  |

**인덱스**: `idx_uap_occurred_at`, `idx_uap_user_time(user_id, occurred_at)`
**트리거**: `user_refresh_token` INSERT 시 자동 삽입
**시퀀스**: `user_activity_ping_seq`

---

### B. 새 도감 (Bird Reference)

#### `bird`

| 컬럼                 | 타입           | 제약조건             | 비고                     |
|---------------------|---------------|---------------------|--------------------------|
| `id`                | BIGINT        | PK                  |                          |
| `korean_name`       | VARCHAR       |                     |                          |
| `scientific_name`   | VARCHAR       |                     |                          |
| `scientific_author` | VARCHAR       |                     |                          |
| `scientific_year`   | INTEGER       |                     |                          |
| `phylum_eng/kor`    | VARCHAR       |                     | 문(門)                    |
| `class_eng/kor`     | VARCHAR       |                     | 강(綱)                    |
| `order_eng/kor`     | VARCHAR       |                     | 목(目)                    |
| `family_eng/kor`    | VARCHAR       |                     | 과(科)                    |
| `genus_eng/kor`     | VARCHAR       |                     | 속(屬)                    |
| `species_eng/kor`   | VARCHAR       |                     | 종(種)                    |
| `body_length_cm`    | DOUBLE PRECISION |                  |                          |
| `nibr_url`          | TEXT          |                     | 국립생물자원관 URL           |
| `description`       | TEXT          |                     |                          |
| `description_source`| VARCHAR       |                     |                          |
| `description_is_ai_generated` | BOOLEAN |             |                          |
| `created_at`        | TIMESTAMPTZ   |                     |                          |
| `updated_at`        | TIMESTAMPTZ   |                     |                          |
| `deleted_at`        | TIMESTAMPTZ   |                     | 소프트 삭제 (V5)           |

**시퀀스**: `bird_seq`
**트리거**: `bird_habitat`, `bird_image`, `bird_residency` 변경 시 `bird.updated_at` 자동 갱신 (V4)

#### `bird_image`

| 컬럼          | 타입           | 제약조건              |
|--------------|---------------|----------------------|
| `id`         | BIGINT        | PK                   |
| `bird_id`    | BIGINT        | FK → bird            |
| `object_key` | TEXT          | S3 경로 (V23)         |
| `content_type` | VARCHAR     |                      |
| `is_thumb`   | BOOLEAN       |                      |
| `order_index`| INT           |                      |
| `created_at` | TIMESTAMPTZ   |                      |
| `updated_at` | TIMESTAMPTZ   |                      |

**인덱스**: `idx_bird_image_bird(bird_id)`

#### `bird_habitat`

| 컬럼          | 타입           | 제약조건              |
|--------------|---------------|----------------------|
| `id`         | BIGINT        | PK                   |
| `bird_id`    | BIGINT        | FK → bird            |
| `habitat_type` | VARCHAR     |                      |

#### `rarity_type` / `residency_type`

| 테이블          | 주요 컬럼                           | 비고                                     |
|----------------|------------------------------------|-----------------------------------------|
| `rarity_type`  | `code` (VARCHAR), `priority` (INT) | COMMON, UNSPECIFIED, RARE               |
| `residency_type`| `code`, `month_bitmask` (INT)     | 12비트 마스크 (CHECK: 0~4095)             |

#### `bird_residency`

- `bird_id`, `rarity_type_id`, `residency_type_id` (FK)
- `month_bitmask` (INT, nullable — `residency_type`의 마스크를 오버라이드 가능)

#### `bird_id_suggestion` (V31)

| 컬럼                      | 타입    | 제약조건                                         |
|--------------------------|--------|------------------------------------------------|
| `id`                     | BIGINT | PK                                             |
| `user_id`                | BIGINT      | FK → users                                |
| `user_bird_collection_id`| BIGINT     | FK → user_bird_collection (CASCADE)        |
| `bird_id`                | BIGINT      | FK → bird                                 |
| `type`                   | VARCHAR(20) | NOT NULL, DEFAULT 'SUGGEST' (V38)          |
| `created_at`             | TIMESTAMPTZ |                                            |

**유니크**: `(user_id, user_bird_collection_id, bird_id, type)` — V38에서 type 추가
**CHECK**: `type IN ('SUGGEST', 'AGREE', 'DISAGREE')`
**인덱스**: `idx_bird_id_suggestion_collection_bird_type`, `idx_bird_id_suggestion_type`

#### `bird_profile_mv` (Materialized View)

```sql
-- bird + residency/habitat/image를 조인한 종합 프로필 뷰
-- WHERE b.deleted_at IS NULL (소프트 삭제된 새 제외)
-- 집계: seasons_with_rarity (JSONB), habitats (ARRAY), images (JSONB)
```

- **유니크 인덱스**: `idx_bird_profile_mv_id`
- V3 생성 → V5/V12에서 재생성 (스키마 변경 반영)

---

### C. 컬렉션 및 소셜 (Collection & Social)

#### `user_bird_collection`

| 컬럼              | 타입                    | 제약조건          | 비고                      |
|------------------|------------------------|-------------------|--------------------------|
| `id`             | BIGINT                 | PK                |                          |
| `user_id`        | BIGINT                 | FK → users        |                          |
| `bird_id`        | BIGINT                 | FK → bird, nullable | 나중에 동정될 수 있음       |
| `discovered_date`| DATE                   |                   |                          |
| `access_level`   | VARCHAR                |                   | PUBLIC / PRIVATE         |
| `temp_bird_name` | VARCHAR                |                   | 동정 전 임시 이름           |
| `location_alias` | VARCHAR                |                   |                          |
| `note`           | VARCHAR(200)           |                   |                          |
| `address`        | VARCHAR                |                   | V11 추가                  |
| `location`       | GEOMETRY(Point, 4326)  | GIST 인덱스       | PostGIS 좌표              |
| `created_at`     | TIMESTAMPTZ            |                   |                          |
| `updated_at`     | TIMESTAMPTZ            |                   |                          |

**인덱스**: `idx_collection_user(user_id)`, `idx_user_collection_location` (GIST)

#### 소셜 테이블 (좋아요, 댓글, 신고)

| 테이블                                    | FK 대상                | 특이사항                           |
|------------------------------------------|------------------------|-----------------------------------|
| `user_bird_collection_like`              | user, collection       | UNIQUE(user, collection), CASCADE |
| `user_bird_collection_comment`           | user, collection       | status 소프트 삭제, parent_id 대댓글 |
| `user_bird_collection_comment_like`      | user, comment          | UNIQUE(user, comment)             |
| `user_bird_collection_report`            | reporter, reported, collection |                            |
| `user_bird_collection_comment_report`    | reporter, reported, comment | `comment_content` 스냅샷 저장   |
| `user_bird_bookmark`                     | user, bird             | UNIQUE(user, bird)                |

#### `popular_collection` (V57, V69, V80-V82)

| 컬럼                       | 타입              | 비고                |
|---------------------------|------------------|---------------------|
| `id`                      | BIGINT           | PK                  |
| `user_bird_collection_id` | BIGINT           | FK, UNIQUE, CASCADE |
| `trending_score`          | DOUBLE PRECISION | NOT NULL (V80)      |
| `popularity_score`        | DOUBLE PRECISION | NOT NULL (V81)      |
| `freshness_score`         | DOUBLE PRECISION | NOT NULL (V81)      |
| `display_order`           | INT              | V82                 |
| `calculated_at`           | TIMESTAMPTZ      | NOT NULL (V80)      |
| `created_at`              | TIMESTAMPTZ      |                     |

**인덱스**: `idx_popular_collection_trending_score(trending_score DESC, calculated_at DESC)`

#### `bird_id_request_history` (V59)

| 컬럼                | 타입           | 비고                              |
|--------------------|---------------|-----------------------------------|
| `id`               | BIGINT        | PK                                |
| `collection_id`    | BIGINT        | FK, nullable                      |
| `started_at`       | TIMESTAMPTZ   |                                   |
| `resolved_at`      | TIMESTAMPTZ   |                                   |
| `resolution_seconds` | BIGINT      |                                   |
| `resolution_kind`  | VARCHAR       | ADOPT / EDIT / NULL(진행중)         |
| `created_at`       | TIMESTAMPTZ   |                                   |

---

### D. 커뮤니티 - 자유게시판 (Freeboard, V88)

#### `free_board_post`

| 컬럼          | 타입           | 제약조건                  |
|--------------|---------------|--------------------------|
| `id`         | BIGINT        | PK                       |
| `user_id`    | BIGINT        | FK → users, NOT NULL     |
| `content`    | TEXT          | NOT NULL                 |
| `created_at` | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW    |
| `updated_at` | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW    |

**인덱스**: `idx_free_board_post_created_at(created_at DESC)`
**시퀀스**: `free_board_post_seq`

#### `free_board_post_comment`

| 컬럼                  | 타입           | 제약조건                             |
|----------------------|---------------|-------------------------------------|
| `id`                 | BIGINT        | PK                                  |
| `user_id`            | BIGINT        | FK → users, NOT NULL                |
| `free_board_post_id` | BIGINT        | FK → free_board_post (CASCADE)      |
| `content`            | TEXT          | NOT NULL                            |
| `status`             | VARCHAR(32)   | NOT NULL, DEFAULT 'ACTIVE'          |
| `parent_id`          | BIGINT        | FK → self (CASCADE), nullable       |
| `created_at`         | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW               |
| `updated_at`         | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW               |

**인덱스**: `idx_free_board_post_comment_post(free_board_post_id)`, `idx_free_board_post_comment_parent(parent_id)`
**시퀀스**: `free_board_post_comment_seq`

---

### E. 알림 (Notification)

#### `notification`

| 컬럼          | 타입           | 제약조건                              |
|--------------|---------------|--------------------------------------|
| `id`         | BIGINT        | PK                                   |
| `user_id`    | BIGINT        | FK → users (CASCADE)                 |
| `title`      | TEXT          |                                      |
| `body`       | TEXT          |                                      |
| `subject`    | VARCHAR       | 알림 대상 도메인 (COLLECTION 등)        |
| `action`     | VARCHAR       | 알림 행위 (LIKE, COMMENT 등)           |
| `payload`    | JSONB         | 유연한 데이터 저장 (`relatedId` 등)     |
| `sender_id`  | BIGINT        | FK → users (SET NULL)                |
| `is_read`    | BOOLEAN       |                                      |
| `created_at` | TIMESTAMPTZ   |                                      |

**인덱스**: `(user_id)`, `(user_id, is_read)`

#### `notification_setting`

| 컬럼            | 타입           | 제약조건                                     |
|----------------|---------------|---------------------------------------------|
| `id`           | BIGINT        | PK                                          |
| `user_device_id` | BIGINT     | FK → user_device (CASCADE)                  |
| `subject`      | VARCHAR(50)   | NOT NULL                                    |
| `action`       | VARCHAR(50)   | nullable (NULL이면 subject 전체 토글)          |
| `enabled`      | BOOLEAN       | DEFAULT true                                |
| `created_at`   | TIMESTAMPTZ   |                                             |
| `updated_at`   | TIMESTAMPTZ   |                                             |

**유니크**: `(user_device_id, subject, action)`
**계층 구조**: `action=NULL`로 subject 그룹 전체 토글, 개별 action 토글도 가능

---

### F. 관리자 (Admin)

#### `admin_audit_log` (V58)

| 컬럼            | 타입           | 비고                         |
|----------------|---------------|------------------------------|
| `id`           | BIGINT        | PK                           |
| `admin_user_id`| BIGINT        | FK → users                   |
| `action`       | VARCHAR(50)   | 수행한 관리 작업                |
| `target_type`  | VARCHAR(50)   | 대상 엔티티 타입                |
| `target_id`    | BIGINT        |                              |
| `report_id`    | BIGINT        | nullable, 관련 신고            |
| `metadata`     | JSONB         | 추가 컨텍스트                   |
| `created_at`   | TIMESTAMPTZ   |                              |

**인덱스**: `(admin_user_id, created_at)`, `(action)`, `(target_type, target_id)`

#### RBAC (Role-Based Access Control, V70-V79)

```
permission ←── role_permission ──→ role ←── user_role ──→ users
```

| 테이블             | 주요 컬럼                            | 유니크                    |
|-------------------|-------------------------------------|--------------------------|
| `permission`      | `key` (VARCHAR, UNIQUE), `description` |                       |
| `role`            | `code` (UNIQUE), `display_name`, `is_builtin` |              |
| `role_permission` | `role_id` (FK), `permission_id` (FK) | `(role_id, permission_id)` |
| `user_role`       | `user_id` (FK, CASCADE), `role_id` (FK) | `(user_id, role_id)` |

**시드 퍼미션**: `ADMIN_REPORT_READ/WRITE`, `ADMIN_AUDIT_READ`, `ADMIN_STAT_READ/WRITE`, `ADMIN_AD_READ/WRITE`, `ADMIN_SLOT_WRITE`, `ADMIN_ANNOUNCEMENT_READ/WRITE`, `ADMIN_LOGIN`
**시드 역할**: `USER`, `ADMIN_VIEWER`, `ADMIN_EDITOR`

#### 공지사항 (V83-V84)

| 테이블                | 주요 컬럼                                              |
|----------------------|-------------------------------------------------------|
| `announcement`       | title, content, status (DRAFT/SCHEDULED/PUBLISHED/ARCHIVED), scheduled_at, published_at |
| `announcement_image` | announcement_id (FK, CASCADE), object_key, content_type |

#### 광고 (V66-V68)

| 테이블           | 용도              | 주요 관계                           |
|-----------------|------------------|-------------------------------------|
| `ad`            | 광고 소재          | object_key (이미지), target_url      |
| `ad_slot`       | 광고 위치          | name (UNIQUE), fallback_ratio, ttl   |
| `ad_placement`  | 광고↔위치 배정      | ad_id, slot_id (CASCADE), weight     |
| `ad_event_log`  | 노출/클릭 이벤트 로그 | event_type, device_hash             |

---

### G. 통계 및 추적

#### `daily_stat` (V59)

| 컬럼          | 타입           | 제약조건                    |
|--------------|---------------|---------------------------|
| `id`         | BIGINT        | PK                        |
| `metric`     | VARCHAR(64)   | UNIQUE (metric, date)     |
| `date`       | DATE          |                           |
| `payload`    | JSONB         | DEFAULT '{}'              |
| `created_at` | TIMESTAMPTZ   |                           |

- 유연한 JSONB 기반 시계열 메트릭 저장
- 인덱스: `(metric, date)`

---

## 4. 컨벤션 정리

### 네이밍

| 대상           | 규칙                          | 예시                                      |
|---------------|------------------------------|-------------------------------------------|
| 테이블         | `snake_case`, 단수형           | `free_board_post`, `user_bird_collection`  |
| PK 컬럼       | `id`                         | `id BIGINT NOT NULL PRIMARY KEY`           |
| FK 컬럼       | `{참조_테이블}_id`             | `user_id`, `free_board_post_id`            |
| 시퀀스         | `{테이블명}_seq`               | `free_board_post_seq`                      |
| 인덱스         | `idx_{테이블}_{컬럼}`          | `idx_free_board_post_user_id`              |
| 유니크 인덱스   | `uq_{테이블}_{컬럼}` 또는 인라인 | `UNIQUE (user_id, bird_id)`                |
| FK 제약조건    | `fk_{테이블}_{참조대상}`        | `fk_free_board_post_comment_post`          |

### 타입 선택 기준

| 용도               | PostgreSQL 타입        | 비고                              |
|-------------------|-----------------------|-----------------------------------|
| PK / FK           | `BIGINT`              | Hibernate SEQUENCE 전략 사용        |
| 짧은 문자열 (이름 등) | `VARCHAR(N)`          | 길이 제한 명시                      |
| 긴 텍스트 (본문 등)  | `TEXT`                | 길이 무제한                         |
| Enum 값            | `VARCHAR(32)`         | `EnumType.STRING` 대응             |
| 타임스탬프           | `TIMESTAMPTZ`         | 시간대 포함, DEFAULT CURRENT_TIMESTAMP |
| 날짜만              | `DATE`                | `discovered_date`, `birth_date`    |
| 불리언              | `BOOLEAN`             | DEFAULT 명시                       |
| 유연한 데이터        | `JSONB`               | `payload`, `metadata`              |
| 좌표               | `GEOMETRY(Point,4326)` | PostGIS, GIST 인덱스 필수           |
| S3 경로             | `TEXT`                | `object_key` 컬럼                  |
| 실수 (측정값/점수)    | `DOUBLE PRECISION`    | `body_length_cm`, `trending_score` |
| 순서/인덱스          | `INT` / `SMALLINT`    | `order_index`, `weight`            |

### 시퀀스 규칙

```sql
CREATE SEQUENCE free_board_post_seq START WITH 1 INCREMENT BY 50;
```

- `INCREMENT BY 50` — Hibernate의 `allocationSize` 기본값과 일치 필수
- `START WITH 1`
- 테이블당 1개 시퀀스, 이름은 `{테이블명}_seq`

### 감사(Audit) 컬럼

```sql
-- 일반 엔티티 (Auditable)
created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP

-- 불변 엔티티 (CreatedAtOnly) — 좋아요, 북마크 등
created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP

-- 소프트 삭제 엔티티 (SoftDeletableAuditable) — 사용자 등
created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
deleted_at TIMESTAMPTZ
```

---

## 5. 마이그레이션 작성 가이드

### 새 도메인 추가 (V88 패턴 참고)

하나의 마이그레이션 파일에 도메인에 필요한 모든 DDL을 포함합니다.

```sql
-- V89__create_example_domain.sql

-- 1. 시퀀스 생성
CREATE SEQUENCE example_post_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE example_post_comment_seq START WITH 1 INCREMENT BY 50;

-- 2. 메인 테이블 생성
CREATE TABLE example_post (
    id         BIGINT      NOT NULL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    title      VARCHAR(100) NOT NULL,
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. 관련 테이블 생성
CREATE TABLE example_post_comment (
    id                BIGINT      NOT NULL PRIMARY KEY,
    user_id           BIGINT      NOT NULL,
    example_post_id   BIGINT      NOT NULL,
    content           TEXT        NOT NULL,
    status            VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    parent_id         BIGINT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. FK 제약조건 (같은 테이블의 FK는 콤마로 묶어서 하나의 ALTER TABLE로)
ALTER TABLE example_post
    ADD CONSTRAINT fk_example_post_user
        FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE example_post_comment
    ADD CONSTRAINT fk_example_post_comment_user   FOREIGN KEY (user_id)        REFERENCES users(id),
    ADD CONSTRAINT fk_example_post_comment_post   FOREIGN KEY (example_post_id) REFERENCES example_post(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_example_post_comment_parent FOREIGN KEY (parent_id)      REFERENCES example_post_comment(id) ON DELETE CASCADE;

-- 5. 인덱스
CREATE INDEX idx_example_post_created_at ON example_post(created_at DESC);
CREATE INDEX idx_example_post_comment_post ON example_post_comment(example_post_id);
CREATE INDEX idx_example_post_comment_parent ON example_post_comment(parent_id);
```

### 기존 테이블 변경

```sql
-- V89__add_title_to_free_board_post.sql

-- 컬럼 추가
ALTER TABLE free_board_post ADD COLUMN title VARCHAR(100);

-- 기존 데이터 마이그레이션 (필요 시)
UPDATE free_board_post SET title = '제목 없음' WHERE title IS NULL;

-- NOT NULL 제약 추가 (데이터 마이그레이션 후)
ALTER TABLE free_board_post ALTER COLUMN title SET NOT NULL;

-- 인덱스 추가
CREATE INDEX idx_free_board_post_title ON free_board_post(title);
```

### 인덱스 추가 기준

| 상황                             | 인덱스 종류           | 예시                                           |
|---------------------------------|---------------------|-------------------------------------------------|
| FK 컬럼                         | 단일 컬럼            | `CREATE INDEX idx_post_user ON post(user_id)`   |
| 목록 정렬                        | 단일 컬럼 (DESC)     | `CREATE INDEX idx_post_created ON post(created_at DESC)` |
| 복합 조건 검색                    | 복합 인덱스          | `CREATE INDEX idx_stat_metric_date ON stat(metric, date)` |
| 유일성 보장                       | UNIQUE              | `UNIQUE (user_id, post_id)`                     |
| 좌표 검색                        | GIST                | `USING GIST (location)`                         |
| 읽음/안읽음 필터                   | 복합 인덱스          | `(user_id, is_read)`                            |

**인덱스 컬럼 순서**: 선택도(selectivity)가 높은 컬럼을 앞에 배치

### FK CASCADE 결정 기준

| CASCADE 종류       | 사용 상황                              | 예시                              |
|-------------------|---------------------------------------|---------------------------------|
| `ON DELETE CASCADE`| 부모 삭제 시 자식도 반드시 삭제           | 게시글 삭제 → 댓글 삭제             |
| `ON DELETE SET NULL`| 부모 삭제 후에도 자식 레코드는 유지       | 알림의 sender 삭제 → sender=NULL  |
| (없음 — RESTRICT) | 자식이 있으면 부모 삭제를 막아야 할 때     | 새(bird) 삭제 방지                 |

### 주의사항

- **되돌릴 수 없음**: Flyway는 기본적으로 롤백을 지원하지 않으므로, ALTER/DROP 전 충분히 검토 (로컬 작업은 상관 없음)
- **데이터 마이그레이션**: NOT NULL 컬럼 추가 시 기존 데이터에 기본값을 먼저 채운 후 제약 추가
- **ENUM → VARCHAR**: PostgreSQL ENUM 타입 사용 지양, `VARCHAR(32)`로 통일 (V12 교훈)
- **대용량 테이블 ALTER**: 운영 중인 대용량 테이블의 ALTER는 락을 유발할 수 있으므로 주의

---

## 6. 주요 설계 패턴

### 소프트 삭제 (2가지 방식)

```
방식 1: status 컬럼 (댓글 계열)
├── ACTIVE   → 정상
├── DELETED  → 사용자 삭제
└── BANNED   → 관리자 제재

방식 2: deleted_at 컬럼 (사용자, 새)
├── NULL     → 정상
└── 타임스탬프 → 삭제됨
```

- 댓글은 **대댓글 유무에 따라** 소프트/하드 삭제 결정
- 사용자, 새(bird)는 `deleted_at` 기반 소프트 삭제

### 좋아요/북마크 (토글 패턴)

```sql
-- 유니크 제약으로 중복 방지
UNIQUE (user_id, user_bird_collection_id)

-- 엔티티는 CreatedAtOnly 상속 (updated_at 불필요)
created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
```

- INSERT로 좋아요, DELETE로 취소 (토글)
- 유니크 제약으로 동일 사용자의 중복 좋아요 방지

### 대댓글 (1단계 답글)

```sql
parent_id BIGINT REFERENCES self(id) ON DELETE CASCADE
```

- `parent_id = NULL`이면 원댓글, 값이 있으면 대댓글
- `ON DELETE CASCADE` — 원댓글 하드 삭제 시 대댓글도 삭제
- 깊이는 1단계로 제한 (애플리케이션 레벨에서 관리)

### 신고 (Report)

```sql
-- 댓글 신고 시 content 스냅샷 저장
comment_content TEXT  -- 삭제 후에도 신고 내용 확인 가능
```

- 신고 시점의 내용을 스냅샷으로 저장
- 원본이 삭제/수정되어도 신고 맥락 유지

### 알림 설정 (계층적 토글)

```
subject = COLLECTION, action = NULL     → 컬렉션 알림 전체 OFF
subject = COLLECTION, action = LIKE     → 컬렉션 좋아요만 OFF
subject = COLLECTION, action = COMMENT  → 컬렉션 댓글만 OFF
```

- `action = NULL`로 subject 그룹 전체 제어
- 개별 action으로 세밀한 제어

### JSONB 활용 (유연한 데이터)

```sql
-- 알림 페이로드 — 도메인별로 다른 데이터
payload JSONB  -- {"relatedId": 123, "type": "collection"}

-- 통계 — 메트릭별로 다른 구조
payload JSONB DEFAULT '{}'  -- {"count": 42, "delta": 5}

-- 관리 감사 로그 — 변경 사항 기록
metadata JSONB  -- {"before": {...}, "after": {...}}
```

- 스키마 변경 없이 유연하게 확장 가능
- 쿼리가 빈번하지 않은 부가 데이터에 적합

### 트리거 활용

| 트리거                         | 용도                                  |
|-------------------------------|--------------------------------------|
| `touch_bird_updated_at`       | 하위 테이블 변경 시 bird.updated_at 갱신 |
| `set_signup_completed_at`     | 가입 완료 시 타임스탬프 자동 설정 (1회성)  |
| `insert_activity_ping`        | 토큰 갱신 시 활동 기록 자동 삽입          |

- DB 레벨에서 데이터 정합성을 보장해야 할 때 사용
- 애플리케이션 로직으로 처리하기 어려운 교차 테이블 업데이트에 적합

---

## 7. 마이그레이션 이력 요약

| 버전       | 주요 내용                                         |
|-----------|--------------------------------------------------|
| V1        | 초기 스키마 (users, bird, collection, PostGIS)      |
| V3-V5     | Materialized View, 트리거, bird 소프트 삭제          |
| V9-V12    | S3 object_key 전환, ENUM → VARCHAR 전환            |
| V14-V20   | 닉네임 nullable/unique, RBAC 기초, refresh token    |
| V23       | bird_image S3 경로 전환                             |
| V26-V33   | 컬렉션 소셜 기능 (좋아요, 댓글, 신고, 동정 제안)       |
| V39-V44   | 프로필 이미지, 디바이스, 알림 시스템                    |
| V50-V52   | 알림 JSONB payload 전환                             |
| V57-V65   | 인기 컬렉션, 감사 로그, 통계, 활동 추적                |
| V66-V68   | 광고 시스템                                         |
| V69-V82   | 인기 컬렉션 점수 체계, RBAC 완성 (permission/role)    |
| V83-V84   | 공지사항                                            |
| V85       | 컬렉션 댓글: status 소프트 삭제 + 대댓글               |
| V86-V87   | 가입 경로, 디바이스 platform                         |
| **V88**   | **자유게시판 (freeboard) 도메인 추가**                |
