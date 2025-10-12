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

이제는 IDE에서 Shift + F10으로 실행하지 않고, Docker 컨테이너로 실행합니다.
실행 전 Docker Desktop을 켜 두어야 합니다.

```bash
# 서버 올리기
./up.sh

# 서버 내리기
./down.sh
```

---

## 🌐 배포 환경, 인프라

- Docker 기반 컨테이너 배포를 사용하고 있습니다.
  - GitHub Actions를 통해 브랜치 푸시 시 자동 배포
    - develop push -> 개발 서버 배포 (Docker compose)
    - main push -> 운영 서버 배포 (Docker compose)
- 사용 중인 AWS 서비스
  - EC2: 서버를 돌리는 컴퓨터
  - RDS: DB를 제공하는 컴퓨터
  - S3: 클라우드 스토리지 서비스 (이미지, 파일 등 저장 및 관리)
  - CloudFront: CDN
    - S3 리소스 조회 시 CloudFront 도메인을 통해 접근하도록 운영하고 있습니다
  - KMS: 양방향 암호화에 쓰이는 키를 제공해주는 서비스
  - Route 53: DNS

---

## 🚀 브랜치 전략

- `main`: 운영 브랜치 (운영용 EC2에 배포)
- `develop`: 개발 브랜치 (개발용 EC2에 배포)

### 새로 기능을 개발할 때
- develop에서 새로 브랜치 파서 기능 개발합니다. `feat/***` `chore/***`
- 기능 개발해서 develop에 배포했는데 문제가 생기면 고칩니다. `fix/***`
- develop으로 PR merge하는 방식은 `squash` 방식을 사용하고, 원래 작업하던 브랜치는 삭제합니다.

### 개발 서버 내용을 운영 서버로 배포할 때
- develop을 main으로 PR merge하는 경우인데, 이때는 일반 `merge commit` 방식을 사용합니다.
- 운영 서버에 배포했는데 뭔가 문제가 생겼을 경우, main 브랜치에서 `hotfix/***` 브랜치를 파서 고쳐서 main으로 `merge commit`하고 원래 작업하던 브랜치는 삭제합니다.
- develop이 아닌 브랜치에서 main으로 PR merge했을 경우, 자동으로 main -> develop으로 병합하는 PR이 생성되고 merge 처리됩니다. (GitHub Actions 설정)
  - 예를 들어 hotfix 브랜치를 main에 병합했을 경우에 해당합니다
  - 이렇게 함으로써 develop -> main 병합할 때 충돌 생길 일을 최대한 미연에 방지할 수 있을 걸로 기대
  - 뭐 충돌 생기면 develop에서 main merge한 다음 병합하면 되긴 하는데 하여튼 그렇습니다

---

## 🧪 테스트 구성

- JUnit 기반 단위 테스트 및 통합 테스트를 구성하고 싶은데, 지금 테스트가 현저히 부족한 상황
- Jacoco를 도입해 코드 커버리지를 측정하고 테스트를 추가해 소프트웨어 품질을 높여 봅시다
- 테스트가 잘 돼 있어야 미리 소프트웨어 결함을 발견할 수 있고, 리팩터링도 안전하게 할 수 있고, 나중에 배포하고 나서 "어 서비스가 안 열리는데요?" 이런 사태를 최대한 예방할 수 있어요

### 테스트를 돌리고 코드 커버리지 확인하기
터미널에서 다음 명령어를 실행:
```bash
./gradlew test
```
또는 이전 빌드 결과를 삭제하고(`clean`) 테스트를 실행:
```bash
./gradlew clean test
```
모든 테스트가 통과하면 마지막에 이런 식으로 출력됩니다:
```
────────────────────────────────────────────
 JaCoCo Total Instruction Coverage: 7.08%
   covered = 682, missed = 8945
────────────────────────────────────────────


[Incubating] Problems report is available at: .../problems-report.html

BUILD SUCCESSFUL in 34s
7 actionable tasks: 7 executed
```
여기서 JaCoCo Total Instruction Coverage가 현재 측정된 우리 프로젝트의 코드 커버리지(우리가 짠 코드가 얼마나 테스트되었는지)를 나타냅니다

만약 테스트가 실패한다면 BUILD FAILED라고 뜰 것입니다. 원인을 찾아 해결하면 됩니다. 원인은 그때그때 다를 수 있어요:
- Docker Desktop을 안 켰거나 (테스트 중 Docker 컨테이너를 돌리는 게 있어서 테스트하기 전 Docker Desktop을 한번 켜주세요)
- 코드에 결함이 발견되어서 고쳐야 하거나
- 때로는 테스트 자체의 결함 때문으로, 테스트를 수정해야 할 수도 있음

### 코드 커버리지 자세히 확인하기
- `./gradle test`를 돌린 뒤 `./build/reports/jacoco/html/index.html`을 열어 자세한 리포트를 볼 수 있습니다
- 우리가 짠 코드에서 테스트가 된 부분, 안 된 부분을 라인 단위까지 확인 가능합니다. 이걸 토대로 다음에 어디에 테스트를 추가할지도 볼 수 있겠죠
- 참고: https://techblog.woowahan.com/2661/

---

## 📂 프로젝트 디렉터리 구조

DDD라는 걸 잘 이용해보려고 노력하고 있습니다.

- domain: 이 아래에서 서비스의 큼직한 도메인들을 정의하고, 각 도메인 안에서
  - api 계층: presentation 계층이라고도 하는데, 애플리케이션 가장 바깥쪽에서 HTTP 요청을 응대하는 쪽 (Controller)
  - application 계층: 각각의 use case를 트랜잭션 단위로 처리하는 쪽 (Application Service)
  - domain 계층: 도메인 비즈니스 로직 (실제 폴더명은 core를 쓰고 있고, 이 안에서 해당 도메인의 Entity, Domain Service 등이 존재)
  - infra 계층: 외부 API와의 통신, DB 등 일처리를 위해 외부 요소와 연락하는 쪽 (Repository, 외부 API Client)
  - 이런 DDD에서 제안하는 layered structure를 잘 녹여서 유지보수하기 좋은 구조를 만드려고 노력하고 있습니다
  - 완벽하진 않지만 점진적으로 개선하기
- global: 전역적으로 쓰이는 설정, 유틸, 기타 객체들

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
