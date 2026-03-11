# 프론트엔드 구조 (`frontend/`)

## 개요

Operato WCS 프론트엔드는 **Things Factory** 프레임워크 기반의 **Lerna 모노레포** 구조로 구성되어 있습니다.

- **프레임워크**: Things Factory (Lit 기반 Web Components)
- **패키지 관리**: Lerna + Yarn Workspaces
- **빌드 도구**: Webpack
- **언어**: TypeScript

## 디렉토리 구조

```
frontend/
├── packages/                           # 모노레포 패키지 디렉토리
│   ├── operato-wcs-ui/                # 🎯 메인 WCS 애플리케이션
│   ├── operato-wcs-system-ui/         # 시스템 관리 UI (선택 사용)
│   ├── metapage/                      # 메타데이터 기반 페이지 자동 생성 모듈
│   └── operatofill/                   # Spring 백엔드 연동 모듈
├── node_modules/                      # 공통 의존성
├── package.json                       # 루트 package.json
├── lerna.json                         # Lerna 설정
├── yarn.lock                          # 의존성 잠금 파일
└── plopfile.js                        # 코드 생성기 설정
```

## 패키지 구성

### 1. operato-wcs-ui (메인 애플리케이션)

**패키지명**: `@things-factory/operato-wcs-ui`
**설명**: Operato WCS의 메인 사용자 인터페이스

```
operato-wcs-ui/
├── client/                            # 프론트엔드 소스
│   ├── pages/                        # 페이지 컴포넌트
│   ├── viewparts/                    # 뷰 파트 컴포넌트
│   ├── route.js                      # 라우팅 정의
│   └── index.js                      # 클라이언트 진입점
├── server/                            # 백엔드 로직 (Node.js)
│   ├── routes/                       # API 라우트
│   ├── entities/                     # TypeORM 엔티티
│   ├── migrations/                   # DB 마이그레이션
│   └── index.ts                      # 서버 진입점
├── config/                            # 환경별 설정
│   ├── config.development.js         # 개발 환경
│   └── config.production.js          # 운영 환경
├── dist-client/                       # 클라이언트 빌드 결과물
├── dist-server/                       # 서버 빌드 결과물
├── things-factory.config.js          # Things Factory 모듈 설정
└── package.json
```

**개발 환경 설정** (`config/config.development.js`):
```javascript
{
  subdomain: 'logisid',
  port: 5908,                           // 프론트엔드 개발 서버 포트
  accessTokenCookieKey: 'access_token',
  operato: {
    baseUrl: 'http://localhost:9190/rest'  // 백엔드 API 엔드포인트
  },
  ormconfig: {
    type: 'postgres',
    database: 'operato2',
    host: '60.196.69.234',
    port: 3298,
    username: 'operato2',
    password: 'dev!tools#'
  }
}
```

### 2. operato-wcs-system-ui (시스템 UI)

**패키지명**: `@things-factory/operato-wcs-system-ui`
**설명**: 시스템 관리 및 설정 UI (선택적 사용)
**상태**: 현재 비활성화 (config.development.js가 빈 객체)

### 3. metapage

**패키지명**: `@things-factory/metapage`
**설명**: 메타데이터 기반 화면 자동 생성 프레임워크 모듈

### 4. operatofill

**패키지명**: `@things-factory/operatofill`
**설명**: Spring Boot 백엔드와의 인터페이스 모듈

## 빌드 및 실행

### 전체 빌드

```bash
cd frontend
yarn install              # 의존성 설치
yarn build               # 모든 패키지 빌드
yarn build:server        # 서버 코드만 빌드 (TypeScript)
yarn build:client        # 클라이언트 코드만 빌드 (Webpack)
```

### 개발 서버 실행

```bash
# 루트에서 실행
yarn wcs:dev

# 또는 개별 패키지에서 실행
cd packages/operato-wcs-ui
npm run serve:dev
```

**접속 URL**: http://localhost:5908

### 개별 패키지 빌드

```bash
cd packages/operato-wcs-ui
npm run build              # 서버 빌드
npm run build:client       # 클라이언트 빌드
npm run clean              # 빌드 결과물 삭제
```

## 포트 구성

| 서비스 | 포트 | 용도 |
|--------|------|------|
| 프론트엔드 개발 서버 | **5908** | operato-wcs-ui 개발 서버 |
| 백엔드 API | **9190** | Spring Boot REST API |
| PostgreSQL | 3298 | 데이터베이스 |

## Things Factory 아키텍처

### 모듈 구조

각 Things Factory 모듈은 다음 구조를 따릅니다:

```
@things-factory/[module-name]/
├── client/                   # 클라이언트 코드
│   ├── pages/               # 페이지 컴포넌트 (Lit)
│   ├── viewparts/           # 재사용 가능한 뷰 컴포넌트
│   └── route.js             # 라우팅 정의
├── server/                   # 서버 코드
│   ├── routes/              # Express 라우트
│   ├── entities/            # TypeORM 엔티티
│   ├── migrations/          # DB 마이그레이션
│   └── graphql/             # GraphQL 스키마/리졸버
├── things-factory.config.js # 모듈 설정 (라우트, 메뉴 등록)
└── package.json             # "things-factory": true 필드 필수
```

### 의존성 관리

Things Factory는 모듈 간 의존성을 자동으로 해석하고 로드 순서를 결정합니다:

1. `package.json`의 `dependencies`에서 `"things-factory": true` 필드를 가진 패키지 스캔
2. 의존성 그래프 생성 (dependency-solver)
3. 순서대로 모듈 로드 및 초기화

### 워크스페이스 링킹

Yarn Workspaces가 `packages/*` 아래의 패키지를 `node_modules/@things-factory/`에 심볼릭 링크로 연결:

```bash
node_modules/@things-factory/
├── operato-wcs-ui -> ../../packages/operato-wcs-ui
├── operato-wcs-system-ui -> ../../packages/operato-wcs-system-ui
├── metapage -> ../../packages/metapage
└── operatofill -> ../../packages/operatofill
```

## 명명 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 패키지명 | `@things-factory/[name]` | `@things-factory/operato-wcs-ui` |
| 파일명 | kebab-case | `order-list-page.js`, `wcs-button.js` |
| 클래스명 | PascalCase | `OrderListPage`, `WcsButton` |
| Custom Element 태그 | kebab-case | `<order-list-page>`, `<wcs-button>` |
| 라우트 경로 | kebab-case | `/order-management`, `/inventory-status` |

## 빌드 결과물

### 개발 환경

- **클라이언트**: `packages/[name]/dist-client/` → Things Factory 개발 서버가 서빙
- **서버**: `packages/[name]/dist-server/` → TypeScript 컴파일 결과물

### 운영 환경

- **클라이언트**: Webpack으로 번들링 → Nginx 또는 Docker 이미지에 복사
- **서버**: TypeScript 컴파일 → Node.js로 실행

## 참고 사항

- **CLAUDE.md**에는 `client/` 디렉토리가 언급되어 있으나, 실제로는 `frontend/` 디렉토리 사용
- 백엔드(Spring Boot)는 프론트엔드와 별도로 실행 (REST API만 제공)
- 개발 시 프론트엔드와 백엔드를 동시에 실행해야 함
- Things Factory 모듈은 `"things-factory": true` 필드가 없으면 자동 로드되지 않음
