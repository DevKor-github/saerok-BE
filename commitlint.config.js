module.exports = {
    extends: ['@commitlint/config-conventional'],
    rules: {
        "type-enum": [2, "always", [
            "feat",
            "fix",
            "docs",
            "chore",
            "style",
            "refactor",
            "test",
            "perf",
            "ci",
            "revert"
        ]],
        "scope-enum": [2, "always", [
            "user",
            "dex",
            "coll",
            "map",
            "entity",
            "infra",
            "db"
        ]],
        "footer-empty": [2, "always"],
        "subject-case": [0]
    },
    prompt: {
        settings: {},
        messages: {
            skip: ':건너뛰기 가능',
            max: '최대 %d자까지 입력할 수 있어요',
            min: '최소 %d자 이상 입력해주세요',
            emptyWarning: '내용을 비워둘 수는 없어요',
            upperLimitWarning: '입력한 내용이 너무 길어요',
            lowerLimitWarning: '입력한 내용이 너무 짧아요',
        },
        questions: {
            type: {
                description: '[Type] 이번 커밋의 변경 유형을 선택해주세요:',
                enum: {
                    feat: {
                        description: '✨  새로운 기능 추가',
                        title: '기능',
                        emoji: '✨',
                    },
                    fix: {
                        description: '🐛 버그 수정',
                        title: '버그 수정',
                        emoji: '🐛',
                    },
                    docs: {
                        description: '📚 문서 관련 변경사항',
                        title: '문서',
                        emoji: '📚',
                    },
                    style: {
                        description: '💎 코드 의미에는 영향을 주지 않는 스타일 변경 (공백, 포맷, 세미콜론 등)',
                        title: '스타일',
                        emoji: '💎',
                    },
                    refactor: {
                        description: '📦 버그 수정이나 기능 추가가 아닌 코드 리팩토링',
                        title: '리팩토링',
                        emoji: '📦',
                    },
                    perf: {
                        description: '🚀 성능 개선을 위한 코드 변경',
                        title: '성능',
                        emoji: '🚀',
                    },
                    test: {
                        description: '🚨 테스트 추가 또는 기존 테스트 보완',
                        title: '테스트',
                        emoji: '🚨',
                    },
                    ci: {
                        description: '⚙️ CI 설정 또는 관련 스크립트 변경 (예: GitHub Actions 워크플로 수정)',
                        title: 'CI 설정',
                        emoji: '⚙️',
                    },
                    chore: {
                        description: '♻️ 소스 코드나 테스트 외의 기타 작업 (예: 디렉토리 구조 변경, 패키지 설치, .gitignore 수정)',
                        title: '기타 작업',
                        emoji: '♻️',
                    },
                    revert: {
                        description: '🗑 이전 커밋 되돌리기',
                        title: '되돌리기',
                        emoji: '🗑',
                    },
                },
            },
            scope: {
                description: '[Scope] 이번 변경이 적용된 범위를 선택해주세요 (범위 생략하려면 empty 선택)',
                enum: {
                    user: {
                        description: '🙋‍♂️ 사용자 기능 (예: 로그인, 프로필 수정)'
                    },
                    dex: {
                        description: '📘 도감 기능 (예: 새 목록 조회, 새 스크랩)'
                    },
                    coll: {
                        description: '🗂️ 컬렉션 기능 (예: 컬렉션 등록, 수정)'
                    },
                    map: {
                        description: '🗺️ 지도 기능 (예: 주변 버드스팟 조회)'
                    },
                    entity: {
                        description: '🏗️ 엔티티 구조 변경 (예: 필드 추가, 매핑 수정)'
                    },
                    infra: {
                        description: '🛠️ 설정/인프라 관련 변경 (예: 스크립트, 환경 설정)'
                    },
                    db: {
                        description: '🗄️ DB 관련 변경 (예: Flyway 마이그레이션 추가)'
                    }
                }
            },
            subject: {
                description: '[Subject] 핵심 변경 내용을 간결하게 적어주세요 (예: 로그인 오류 수정, UI 마진 조정)',
            },
            body: {
                description: '[Body] 필요하다면, 무엇을 변경했고 왜 변경했는지 써주세요 (예: 컬렉션 필드 정리 – 더 이상 사용되지 않아서)',
            },
        },
    }
};
