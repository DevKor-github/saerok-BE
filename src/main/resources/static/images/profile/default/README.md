# 기본 프로필 이미지

이 디렉토리에는 사용자의 기본 프로필 이미지 파일들이 위치합니다.

## 파일 구조
```
default/
├── default-1.png
├── default-2.png
├── default-3.png
├── default-4.png
├── default-5.png
└── default-6.png
```

## 자동 업로드
- 애플리케이션 시작 시 `DefaultProfileImageInitializer`가 자동으로 이 파일들을 S3에 업로드합니다
- S3 경로: `profile-images/default/default-{n}.png`
- 이미 S3에 존재하는 파일은 중복 업로드하지 않습니다

## 이미지 요구사항
- 형식: PNG
- 권장 크기: 100x100px 이하
- 파일 크기: 100KB 이하 권장
- 배경: 투명 또는 단색
