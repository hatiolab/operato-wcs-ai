# 프론트엔드 디렉토리 구조 (`client/`)

Lit + TypeScript + Vite 기준 권장 구조입니다.

```
client/
├── src/
│   ├── components/              # 재사용 가능한 공통 Lit 컴포넌트
│   │   ├── common/              # 버튼, 입력, 뱃지 등 기본 UI 요소
│   │   └── layout/              # 헤더, 사이드바, 푸터 등 레이아웃
│   │
│   ├── pages/                   # 페이지 단위 컴포넌트 (라우팅 단위)
│   │   ├── dashboard/           # 대시보드
│   │   ├── order/               # 주문 관리
│   │   ├── inventory/           # 재고 관리
│   │   ├── equipment/           # 설비 관리
│   │   └── report/              # 리포트
│   │
│   ├── services/                # API 호출 및 비즈니스 로직
│   │   ├── api/                 # fetch wrapper, endpoint 정의
│   │   └── auth/                # 인증/인가 로직
│   │
│   ├── stores/                  # 전역 상태 관리
│   │
│   ├── router/                  # 클라이언트 사이드 라우터
│   │
│   ├── styles/                  # 전역 스타일
│   │   ├── global.css
│   │   └── variables.css        # CSS Custom Properties (디자인 토큰)
│   │
│   ├── utils/                   # 공통 유틸리티 함수
│   ├── types/                   # TypeScript 타입/인터페이스 정의
│   └── index.ts                 # 앱 진입점
│
├── public/                      # 정적 에셋 (빌드 시 그대로 복사)
│   ├── favicon.ico
│   └── assets/
│       ├── images/
│       └── icons/
│
├── test/                        # 테스트
│   ├── unit/
│   └── e2e/
│
├── index.html                   # HTML 진입점
├── package.json
├── tsconfig.json
├── vite.config.ts               # Vite 빌드 설정
└── .env.example                 # 환경변수 예시
```

## 명명 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 파일명 | kebab-case | `order-list-page.ts`, `wcs-button.ts` |
| 클래스명 | PascalCase | `OrderListPage`, `WcsButton` |
| Custom Element 태그 | kebab-case (하이픈 필수) | `<order-list-page>`, `<wcs-button>` |
| CSS Custom Property | `--wcs-` 접두사 | `--wcs-color-primary`, `--wcs-spacing-md` |
| 서비스/유틸 파일 | kebab-case | `order-api.ts`, `date-utils.ts` |

## 빌드 출력 경로

- 개발: `client/dist/` → Spring Boot가 `src/main/resources/static/`에서 서빙
- 운영: `client/dist/` → Nginx `html/` 또는 Docker 이미지에 복사
