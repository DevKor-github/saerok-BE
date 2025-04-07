# 🌿 Saerok Server

Spring Boot 기반의 백엔드 서버입니다.

---

## 💠 기술 스택

- Java 21
- Spring Boot 3.4.4
- PostgreSQL
- JPA (Hibernate)
- Gradle
- GitHub Actions + AWS EC2 (CI/CD)

---

## ⚙️ 로컬 개발 환경 설정

### 1. GitHub 저장소 클론 및 npm 패키지 설치

```bash
git clone https://github.com/your-id/saerok-BE.git
cd saerok-BE
npm install # 커밋 컨벤션 강제를 위한 도구 설치
```
npm 명령어 실행이 안 될 경우 Node.js를 먼저 설치해야 합니다.

### 2. IntelliJ에서 프로젝트 열기

- IntelliJ 실행 → "Open" → `saerok-BE` 폴더 선택
- 또는 `build.gradle` 파일을 선택하면 "Open as Project" 옵션이 나타나므로 해당 옵션 선택
- Gradle 자동 import가 되지 않을 경우 하단 알림창에서 수동 import 실행

### 3. JDK 설치 및 설정

- JDK 21이 설치되어 있어야 합니다.
- IntelliJ 메뉴에서 `File > Project Structure > Project` 탭으로 이동
- SDK 항목에서 `Download JDK` 선택 → Version: 21, Vendor: Amazon Corretto 21.0.6 선택 후 다운로드 및 설정

### 4. 환경 변수 파일 설정

`.env` 파일을 프로젝트 루트에 생성하고 다음과 같이 작성합니다:

```
DB_URL=jdbc:postgresql://localhost:5432/your_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### 5. 애플리케이션 실행

- `SaerokServerApplication.java` 우클릭 → `Run`
- 또는 `Shift + F10` 단축키로 실행

---

## 🌐 배포 환경 (예정)

- EC2 서버에 `application-dev.yml` 설정을 기반으로 배포 예정
- GitHub Actions를 통해 브랜치 푸시 시 자동 배포 예정

---

## 🚀 브랜치 전략 (GitHub Flow)

- `main`: 운영 브랜치 (운영용 EC2에 배포)
- `develop`: 개발 브랜치 (개발용 EC2에 배포)
- `feat/*`, `fix/*`: 기능 추가 및 버그 수정 브랜치
  → 완료 후 `develop` 브랜치로 Pull Request 생성 및 병합

---

## 🧪 테스트 구성 (예정)

- JUnit 기반 단위 테스트 및 통합 테스트 구성 예정

---

## 📂 프로젝트 디렉터리 구조 (예정)

```
추가 예정
```

---

# 📝 커밋 컨벤션 가이드

우리 팀은 커밋 메시지의 일관성과 가독성을 위해 **Conventional Commit** 기반 커밋 컨벤션을 사용합니다.  
아래 가이드를 참고하여 다음 두 가지 방법 중 하나로 커밋 메시지를 작성할 수 있습니다.

---

## 🛠 커밋 작성 방법

### 1. 스스로 커밋 메시지 작성
직접 커밋 메시지를 입력하는 방식입니다. 커밋 컨벤션에 익숙할 경우 이 방법이 더 빠릅니다.

- 터미널에서 직접 입력:  
  `Alt + F12` → `git commit -m "type(scope): subject"`

- IntelliJ Git 툴 창에서 커밋 메시지 작성 후 Commit

※ 이 경우에도 메시지가 컨벤션을 어기면 커밋이 거부됩니다.

### 2. 프롬프트를 통해 커밋 메시지 작성
질문에 답하면서 커밋 메시지가 자동으로 완성되는 방식입니다.

- 터미널에서 실행:  
  `Alt + F12` → `npm run commit` 또는 `npx cz`

> 이 방법은 커밋 컨벤션을 자동으로 따를 수 있어 실수 없이 작성할 수 있습니다.

---

## ✅ 커밋 메시지 형식

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
```

> `footer`는 현재 사용하지 않습니다.

### 🔍 커밋 예시

```bash
feat(user): 회원가입 시 이메일 중복 확인 기능 추가
```

```bash
chore(infra): commitlint + husky 설정 추가

팀 전체 커밋 메시지 규칙 강제 적용을 위해 설정.
pre-commit 훅으로 lint 검사도 함께 수행.
```

```bash
refactor(entity): Collection 엔티티 구조 개선

isTemporary 필드 제거 및 공개 여부 필드 추가.
불필요한 데이터 제거로 구조 단순화.
```

---

## 1. type (무엇을 했는가)

| 타입 | 설명 |
|------|------|
| ✨ `feat` | 새로운 기능 추가 |
| 🐛 `fix` | 버그 수정 |
| 📚 `docs` | 문서 관련 변경 (예: README, 주석) |
| 💎 `style` | 코드 의미에는 영향을 주지 않는 스타일 변경 (포맷, 세미콜론 등) |
| 📦 `refactor` | 리팩토링 (기능 변경 없이 구조 개선) |
| 🚀 `perf` | 성능 개선 |
| 🚨 `test` | 테스트 코드 추가/보완 |
| ⚙️ `ci` | CI 설정 및 관련 스크립트 변경 |
| ♻️ `chore` | 기타 작업 (예: 패키지 설치, 설정 파일 변경, 디렉토리 정리 등) |
| 🗑 `revert` | 이전 커밋 되돌리기 |

---

## 2. scope (어디를 바꿨는가)

| 스코프 | 설명 |
|--------|------|
| 🙋‍♂️ `user` | 사용자 기능 (로그인, 프로필 수정 등) |
| 📘 `dex` | 도감 기능 (새 목록 조회, 새 스크랩 등) |
| 🗂️ `coll` | 컬렉션 기능 (등록, 수정 등) |
| 🗺️ `map` | 지도 기능 (위치 기반 조회 등) |
| 🏗️ `entity` | 엔티티 구조 변경 (필드 추가, 매핑 수정 등) |
| 🛠️ `infra` | 설정/인프라 변경 (스크립트, .env, husky, commitlint 등) |

✅ 범위 지정이 어렵거나 필요 없을 경우 `scope`는 생략 가능합니다.

---

## 3. subject (무슨 일이 일어났는가)

- **한 줄 요약**으로 변경 내용을 간결하게 작성합니다.
- 원활한 소통을 위해 한국어로 작성합니다.
- 마침표(.)는 붙이지 않습니다.

> 예: `로그인 오류 수정`, `컬렉션 정렬 기능 추가`, `CI 워크플로 최적화`

---

## 4. body (왜 그렇게 했는가)

- 선택 사항이지만 **기능 수정/리팩토링 등 복잡한 변경에는 작성 권장**
- "무엇을 바꿨고, 왜 바꿨는지"에 집중해서 작성하세요.

> 예:  
> 컬렉션 목록이 정렬되지 않아 UX 저하 발생.  
> createdAt 기준 내림차순 정렬 적용.

---

## 🛡 유효성 검사 규칙 요약

커밋 컨벤션 규칙은 `commitlint.config.js`에 의해 설정됩니다.

| 항목 | 규칙 |
|------|------|
| type | 반드시 `type-enum` 목록 내에서 선택해야 함 |
| scope | 반드시 `scope-enum` 목록 내에서 선택하거나 생략 가능 (`empty`) |
| footer | 항상 비워야 함 (`BREAKING CHANGE`, `Issue` 등 사용하지 않음) |

---

## 📌 참고

- 커밋 컨벤션 강제는 `husky`, `commitlint`, `commitizen` 등의 도구로 구성되어 있습니다.

---
