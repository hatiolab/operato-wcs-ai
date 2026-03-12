# 프론트엔드 테스트 가이드

> 작성일: 2026-03-12 | 대상: operato-wcs-ai 프론트엔드 (Things Factory)

---

## 1. 현재 상태

| 항목 | 상태 |
|------|------|
| 단위 테스트 | 0개 |
| E2E 테스트 | 0개 |
| 테스트 디렉토리 | `frontend/test/` 존재 (Things Factory 자체 테스트만) |
| 비즈니스 로직 테스트 | ❌ 없음 |
| 테스트 커버리지 | 0% |

**현재 상태**: 비즈니스 로직에 대한 테스트 코드가 전무하여 **가장 시급한 품질 개선 항목**입니다.

---

## 2. 테스트 환경 구성

### 2.1 테스트 프레임워크 선택

Things Factory/Lit Element 기반 프로젝트에 적합한 테스트 스택:

```json
// package.json
{
  "devDependencies": {
    "@open-wc/testing": "^3.2.0",
    "@web/test-runner": "^0.17.0",
    "@esm-bundle/chai": "^4.3.4",
    "sinon": "^15.0.0",
    "playwright": "^1.40.0"
  },
  "scripts": {
    "test": "web-test-runner --coverage",
    "test:watch": "web-test-runner --watch",
    "test:e2e": "playwright test"
  }
}
```

**주요 도구**:
- **@open-wc/testing**: Lit Element 공식 테스트 라이브러리
- **@web/test-runner**: 빠른 브라우저 기반 테스트 러너
- **Chai**: Assertion 라이브러리
- **Sinon**: Mock/Stub/Spy
- **Playwright**: E2E 테스트

### 2.2 테스트 디렉토리 구조

```
frontend/
├── packages/
│   ├── operato-wcs-ui/
│   │   ├── client/
│   │   │   ├── pages/
│   │   │   │   └── wcs/
│   │   │   │       └── wcs-home.js
│   │   │   └── bootstrap.js
│   │   └── test/                    # 신규 생성
│   │       ├── unit/
│   │       │   ├── pages/
│   │       │   │   └── wcs-home.test.js
│   │       │   └── bootstrap.test.js
│   │       └── integration/
│   │           └── routing.test.js
│   ├── metapage/
│   │   └── test/                    # 신규 생성
│   └── operatofill/
│       └── test/                    # 신규 생성
├── test/                            # E2E 테스트
│   ├── e2e/
│   │   ├── login.spec.ts
│   │   └── navigation.spec.ts
│   └── playwright.config.ts
└── web-test-runner.config.mjs       # 신규 생성
```

### 2.3 설정 파일

**web-test-runner.config.mjs**:
```javascript
import { playwrightLauncher } from '@web/test-runner-playwright';

export default {
  files: 'packages/*/test/**/*.test.js',
  nodeResolve: true,
  coverage: true,
  coverageConfig: {
    threshold: {
      statements: 50,
      branches: 40,
      functions: 50,
      lines: 50
    }
  },
  browsers: [
    playwrightLauncher({ product: 'chromium' }),
    playwrightLauncher({ product: 'firefox' }),
  ],
};
```

**playwright.config.ts**:
```typescript
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './test/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  use: {
    baseURL: 'http://localhost:5908',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'yarn wcs:dev',
    url: 'http://localhost:5908',
    reuseExistingServer: !process.env.CI,
  },
});
```

---

## 3. 테스트 전략

### 3.1 우선순위별 테스트 대상

#### P0 — 핵심 컴포넌트 (단위 테스트)

| 대상 | 테스트 유형 | 이유 |
|------|-----------|------|
| wcs-home.js | 컴포넌트 테스트 | 홈 페이지 렌더링 검증 |
| das-home.js | 컴포넌트 테스트 | DAS 홈 페이지 검증 |
| dps-home.js | 컴포넌트 테스트 | DPS 홈 페이지 검증 |
| bootstrap.js | 유닛 테스트 | 초기화 로직 검증 |

#### P1 — 라우팅 및 상태 관리 (통합 테스트)

| 대상 | 테스트 유형 | 이유 |
|------|-----------|------|
| 동적 라우팅 | 통합 테스트 | 페이지 로딩 검증 |
| Redux 액션/리듀서 | 유닛 테스트 | 상태 변경 검증 |
| dynamicImport() | 유닛 테스트 | 모듈 로딩 검증 |

#### P2 — 사용자 워크플로우 (E2E 테스트)

| 시나리오 | 테스트 유형 | 이유 |
|---------|-----------|------|
| 로그인 → 메뉴 네비게이션 | E2E | 전체 흐름 검증 |
| WCS 홈 → DAS 홈 이동 | E2E | 라우팅 검증 |
| 설비 현황 조회 | E2E | 데이터 표시 검증 |

### 3.2 테스트 패턴

#### 3.2.1 Lit Element 컴포넌트 테스트

**wcs-home.test.js**:
```javascript
import { fixture, html, expect } from '@open-wc/testing';
import '../client/pages/wcs/wcs-home.js';

describe('WcsHome', () => {
  it('should render with default properties', async () => {
    const el = await fixture(html`<wcs-home></wcs-home>`);
    expect(el).to.exist;
  });

  it('should display WCS title', async () => {
    const el = await fixture(html`<wcs-home></wcs-home>`);
    const title = el.shadowRoot.querySelector('h2');
    expect(title.textContent).to.equal('WCS');
  });

  it('should display page description', async () => {
    const el = await fixture(html`<wcs-home></wcs-home>`);
    const description = el.shadowRoot.querySelector('[page-description]');
    expect(description).to.exist;
    expect(description.textContent).to.include('Operato Logistics WCS');
  });

  it('should have responsive image', async () => {
    const el = await fixture(html`<wcs-home></wcs-home>`);
    const img = el.shadowRoot.querySelector('img');
    expect(img).to.exist;
    expect(img.getAttribute('src')).to.equal('/assets/images/wcs/wcs-home.png');
  });

  it('should call pageInitialized on first activation', async () => {
    const el = await fixture(html`<wcs-home></wcs-home>`);
    const spy = sinon.spy(el, 'pageInitialized');

    el.pageUpdate({ active: true }, true);

    expect(spy).to.have.been.called;
  });
});
```

#### 3.2.2 순수 함수 테스트

**bootstrap.test.js**:
```javascript
import { expect } from '@open-wc/testing';
import sinon from 'sinon';

describe('bootstrap', () => {
  describe('dynamicImport', () => {
    it('should import metapage module', async () => {
      const importStub = sinon.stub(global, 'import').resolves();

      await dynamicImport('metapage', 'pages/basic-grist-page.js');

      expect(importStub).to.have.been.calledWith(
        '@things-factory/metapage/client/pages/basic-grist-page.js'
      );

      importStub.restore();
    });

    it('should throw error for invalid module', async () => {
      try {
        await dynamicImport('invalid-module', 'test.js');
        expect.fail('Should have thrown an error');
      } catch (error) {
        expect(error.message).to.include('Unknown module');
      }
    });

    it('should validate URL pattern', async () => {
      try {
        await dynamicImport('metapage', '../../../etc/passwd');
        expect.fail('Should have thrown an error');
      } catch (error) {
        expect(error.message).to.include('Invalid path');
      }
    });
  });

  describe('filterLogisRoutes', () => {
    it('should filter non-system routes when system menu exists', () => {
      const mappings = [
        { id: '1', tagname: 'system-home', parent: true },
        { id: '2', parent_id: '1', routing_type: 'STATIC' },
        { id: '3', parent_id: null, routing_type: 'STATIC' }
      ];
      const systemMenu = mappings[0];

      const result = filterLogisRoutes(mappings, systemMenu);

      expect(result).to.have.lengthOf(1);
      expect(result[0].id).to.equal('3');
    });

    it('should filter static routes when no system menu', () => {
      const mappings = [
        { id: '1', routing_type: 'STATIC' },
        { id: '2', routing_type: 'DYNAMIC' }
      ];

      const result = filterLogisRoutes(mappings, null);

      expect(result).to.have.lengthOf(1);
      expect(result[0].id).to.equal('1');
    });
  });
});
```

#### 3.2.3 Redux 테스트

**main.test.js** (actions):
```javascript
import { expect } from '@open-wc/testing';
import { UPDATE_CONTEXT } from '../client/actions/main.js';

describe('Redux Actions', () => {
  it('should create UPDATE_CONTEXT action', () => {
    const context = { title: 'Test Page' };
    const action = {
      type: UPDATE_CONTEXT,
      context
    };

    expect(action.type).to.equal(UPDATE_CONTEXT);
    expect(action.context.title).to.equal('Test Page');
  });
});
```

**main.test.js** (reducers):
```javascript
import { expect } from '@open-wc/testing';
import reducer from '../client/reducers/main.js';
import { UPDATE_CONTEXT } from '../client/actions/main.js';

describe('Redux Reducers', () => {
  it('should return initial state', () => {
    const state = reducer(undefined, {});
    expect(state).to.exist;
  });

  it('should handle UPDATE_CONTEXT', () => {
    const initialState = { context: {} };
    const action = {
      type: UPDATE_CONTEXT,
      context: { title: 'New Title' }
    };

    const newState = reducer(initialState, action);

    expect(newState.context.title).to.equal('New Title');
  });
});
```

#### 3.2.4 E2E 테스트 (Playwright)

**test/e2e/navigation.spec.ts**:
```typescript
import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('should navigate from WCS home to DAS home', async ({ page }) => {
    // 로그인 (필요시)
    await page.goto('/');

    // WCS 홈으로 이동
    await page.click('text=WCS');
    await expect(page).toHaveURL(/.*wcs-home/);

    // 페이지 제목 확인
    const title = page.locator('h2');
    await expect(title).toHaveText('WCS');

    // DAS 홈으로 이동
    await page.click('text=DAS');
    await expect(page).toHaveURL(/.*das-home/);

    // DAS 페이지 확인
    const dasTitle = page.locator('h2');
    await expect(dasTitle).toHaveText('DAS');
  });

  test('should display equipment status', async ({ page }) => {
    await page.goto('/equipment-home');

    // 설비 목록 로딩 대기
    await page.waitForSelector('[data-testid="equipment-list"]');

    // 최소 1개 이상의 설비 표시
    const equipmentItems = page.locator('.equipment-item');
    await expect(equipmentItems).toHaveCountGreaterThan(0);
  });
});
```

**test/e2e/auth.spec.ts**:
```typescript
import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('should login successfully', async ({ page }) => {
    await page.goto('/');

    // 로그인 폼 입력
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'password');

    // 로그인 버튼 클릭
    await page.click('button[type="submit"]');

    // 로그인 성공 확인 (메인 페이지로 리다이렉트)
    await expect(page).toHaveURL(/.*main/);

    // 사용자 메뉴 표시 확인
    const userMenu = page.locator('[data-testid="user-menu"]');
    await expect(userMenu).toBeVisible();
  });

  test('should show error on invalid credentials', async ({ page }) => {
    await page.goto('/');

    await page.fill('input[name="username"]', 'invalid');
    await page.fill('input[name="password"]', 'wrong');
    await page.click('button[type="submit"]');

    // 에러 메시지 확인
    const error = page.locator('[role="alert"]');
    await expect(error).toBeVisible();
    await expect(error).toContainText('로그인 실패');
  });
});
```

---

## 4. 테스트 모범 사례

### 4.1 Shadow DOM 접근

Lit Element는 Shadow DOM을 사용하므로 `shadowRoot`를 통해 접근:

```javascript
// ❌ 잘못된 방법
const title = el.querySelector('h2');

// ✅ 올바른 방법
const title = el.shadowRoot.querySelector('h2');
```

### 4.2 비동기 렌더링 대기

Lit Element는 비동기로 렌더링되므로 `updateComplete` 사용:

```javascript
it('should update when property changes', async () => {
  const el = await fixture(html`<wcs-home></wcs-home>`);

  el.bizplaces = [{ id: 1, name: 'Test' }];
  await el.updateComplete;  // 렌더링 완료 대기

  const item = el.shadowRoot.querySelector('.bizplace-item');
  expect(item).to.exist;
});
```

### 4.3 이벤트 테스트

```javascript
it('should dispatch custom event on button click', async () => {
  const el = await fixture(html`<wcs-home></wcs-home>`);

  const eventSpy = sinon.spy();
  el.addEventListener('custom-event', eventSpy);

  const button = el.shadowRoot.querySelector('button');
  button.click();

  expect(eventSpy).to.have.been.calledOnce;
});
```

### 4.4 API 모킹

```javascript
import sinon from 'sinon';

it('should fetch data from API', async () => {
  const fetchStub = sinon.stub(window, 'fetch');
  fetchStub.resolves({
    ok: true,
    json: async () => ({ data: [{ id: 1 }] })
  });

  const el = await fixture(html`<data-component></data-component>`);
  await el.fetchData();

  expect(el.data).to.have.lengthOf(1);
  fetchStub.restore();
});
```

---

## 5. 목표 커버리지

| 단계 | 목표 | 대상 | 기간 |
|------|------|------|------|
| 1단계 | 30% | 핵심 페이지 컴포넌트 (wcs-home, das-home, dps-home) | 1주 |
| 2단계 | 50% | 라우팅, Redux, 유틸리티 함수 | 2주 |
| 3단계 | 70% | Mixin, 이벤트 핸들러, 전체 컴포넌트 | 1개월 |

---

## 6. 테스트 실행

### 6.1 단위/통합 테스트

```bash
# 전체 테스트 실행
yarn test

# Watch 모드
yarn test:watch

# 특정 파일 테스트
yarn test packages/operato-wcs-ui/test/unit/pages/wcs-home.test.js

# 커버리지 확인
yarn test --coverage
open coverage/index.html
```

### 6.2 E2E 테스트

```bash
# E2E 테스트 실행
yarn test:e2e

# UI 모드로 실행 (디버깅)
yarn playwright test --ui

# 특정 브라우저에서만 실행
yarn playwright test --project=chromium

# 헤드풀 모드로 실행 (브라우저 보이기)
yarn playwright test --headed
```

---

## 7. CI/CD 통합

### 7.1 GitHub Actions 워크플로우

**.github/workflows/frontend-test.yml**:
```yaml
name: Frontend Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'yarn'

    - name: Install dependencies
      run: |
        cd frontend
        yarn install

    - name: Run unit tests
      run: |
        cd frontend
        yarn test --coverage

    - name: Upload coverage
      uses: codecov/codecov-action@v3
      with:
        files: ./frontend/coverage/lcov.info

    - name: Install Playwright browsers
      run: |
        cd frontend
        yarn playwright install --with-deps

    - name: Run E2E tests
      run: |
        cd frontend
        yarn test:e2e

    - name: Upload Playwright report
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: playwright-report
        path: frontend/playwright-report/
```

---

## 8. 테스트 우선순위 로드맵

### Week 1-2: 기본 인프라
- [x] 테스트 프레임워크 설정 (@open-wc/testing, Playwright)
- [ ] 핵심 페이지 컴포넌트 테스트 (wcs-home, das-home, dps-home)
- [ ] bootstrap.js 주요 함수 테스트

### Week 3-4: 확장
- [ ] 모든 홈 페이지 컴포넌트 테스트 (master, equipment, config)
- [ ] Redux 액션/리듀서 테스트
- [ ] 라우팅 통합 테스트

### Week 5-8: E2E 및 완성
- [ ] 주요 사용자 워크플로우 E2E 테스트 (로그인, 네비게이션, 데이터 조회)
- [ ] Mixin 클래스 테스트
- [ ] CI/CD 통합
- [ ] 코드 커버리지 50% 달성

---

## 9. 현재 제약 사항 및 해결 방안

### 9.1 Things Factory 프레임워크 의존성

**문제**: Things Factory 내부 모듈(@things-factory/*)을 모킹하기 어려움

**해결 방안**:
- 핵심 비즈니스 로직과 프레임워크 로직 분리
- 의존성 주입 패턴 사용
- Integration 테스트에서 실제 프레임워크 사용

### 9.2 Redux Store 접근

**문제**: 컴포넌트가 전역 Redux Store에 의존

**해결 방안**:
```javascript
import { store } from '@things-factory/shell';

// 테스트 전용 Store 생성
import { createStore } from 'redux';
const testStore = createStore(reducer, initialState);

// Store 교체 (테스트용)
beforeEach(() => {
  // store를 testStore로 모킹
});
```

### 9.3 동적 Import

**문제**: `import()` 함수를 모킹하기 어려움

**해결 방안**:
- Import wrapper 함수 생성하여 모킹 가능하게 만들기
```javascript
// 현재
import(`@things-factory/metapage/client/${url}`)

// 개선
const moduleLoader = {
  load: (path) => import(path)
};

// 테스트에서 모킹
sinon.stub(moduleLoader, 'load').resolves({ default: MockComponent });
```

---

## 10. 참고 자료

### 공식 문서
- [Open WC Testing](https://open-wc.org/docs/testing/testing-package/)
- [Lit Testing Guide](https://lit.dev/docs/tools/testing/)
- [Playwright Documentation](https://playwright.dev/)
- [Web Test Runner](https://modern-web.dev/docs/test-runner/overview/)

### 예제 저장소
- [Lit Element Testing Examples](https://github.com/open-wc/open-wc/tree/master/packages/testing)
- [Things Factory Test Examples](https://github.com/things-factory) (프레임워크 자체 테스트 참고)

### 도구
- [Chai Assertion Library](https://www.chaijs.com/)
- [Sinon.js - Mocking](https://sinonjs.org/)
- [Codecov](https://about.codecov.io/) - 커버리지 추적
