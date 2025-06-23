# global 모듈 구조 안내

본 디렉터리는 전역 공통 코드, 인프라 설정, 보안, 예외, 유틸리티를 역할별로 분리해 관리합니다.

---

## 1. core

- **config/infra**  
  인프라 및 외부 서비스(Spring, AWS, WebClient 등) 관련 설정.
- **config/feature**  
  서비스 공통 로직(예: 금칙어, 닉네임 등) 관련 설정. (추후 각 도메인별 config로 이동 고려)
- **config/presentation**  
  API 문서 등 프레젠테이션 계층 설정.
- **properties**  
  @ConfigurationProperties 등 외부 설정 주입용 설정 객체.

## 2. security

- **config**  
  Spring Security 메인 설정.
- **jwt**  
  JWT 인증/인가 필터, 핸들러.
- **crypto**  
  암복호화 계층.
- **principal**  
  로그인 사용자 주체 표현.
- **token**  
  서비스 토큰 발급·관리(Access/Refresh 등).

## 3. shared

- **entity**  
  공통 엔티티 (Auditable 등).
- **exception**  
  글로벌 예외 및 에러 응답.
- **util**  
  범용 유틸리티, 변환기, 헬퍼 클래스.
- **util/dto**  
  유틸리티 전용 DTO.

---

## 네이밍 및 추가 규칙

- 각 파일은 담당 역할이 명확히 드러나는 디렉터리에만 추가
- 새로운 공통 설정, 유틸, 예외는 반드시 해당 구조에 맞춰 분류
- 실제 도메인 비즈니스 로직과의 경계 유지

---

## 참고

이 구조는 유지보수성과 확장성을 최우선으로 고려함  
새 파일 추가 시에도 이 구조를 참고할 것
