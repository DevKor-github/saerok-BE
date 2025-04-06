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

### 1. GitHub 저장소 클론

```bash
git clone https://github.com/your-id/saerok-BE.git
cd saerok-BE
```

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

## 📌 커밋 메시지 컨벤션 (임시)

- `feat`: 기능 추가
- `fix`: 버그 수정
- `chore`: 설정, 환경, 빌드 관련 변경

