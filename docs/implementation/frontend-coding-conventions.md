# Operato WCS — 프론트엔드 코딩 컨벤션

## 문서 정보

| 항목 | 내용 |
|------|------|
| 작성일 | 2026-03-11 |
| 버전 | 1.0 |
| 대상 | 프론트엔드 (Things Factory 기반 Web Components) |
| 목적 | Things Factory/Lit 기반 프론트엔드 코딩 규칙 정립 |

---

## 1. 프로젝트 구조 개요

### 1.1 기술 스택

| 구분 | 기술 |
|------|------|
| 프레임워크 | Things Factory |
| 컴포넌트 라이브러리 | Lit (Web Components) |
| 언어 | TypeScript / JavaScript |
| 빌드 도구 | Rollup |
| 패키지 관리 | Lerna 모노레포 |
| 스타일 | CSS-in-JS (Lit 스타일) |
| 번들러 | Rollup |

### 1.2 디렉토리 구조

```
frontend/
├── packages/
│   ├── operato-wcs-ui/           # 메인 WCS UI (포트 5908)
│   │   ├── src/
│   │   │   ├── pages/            # 페이지 컴포넌트
│   │   │   ├── components/       # 재사용 컴포넌트
│   │   │   ├── layouts/          # 레이아웃 컴포넌트
│   │   │   └── styles/           # 공통 스타일
│   │   ├── dist-client/          # 클라이언트 빌드 결과물
│   │   └── package.json
│   ├── operato-wcs-system-ui/    # 시스템 UI
│   ├── metapage/                 # 메타페이지
│   └── operatofill/              # Spring 백엔드 연동
├── lerna.json
└── package.json
```

---

## 2. 파일 네이밍 규칙

### 2.1 컴포넌트 파일

| 유형 | 패턴 | 예시 |
|------|------|------|
| 페이지 컴포넌트 | `{기능}-page.js` | `gateway-page.js`, `job-batch-page.js` |
| 일반 컴포넌트 | `{기능}-component.js` | `data-grid.js`, `search-form.js` |
| 레이아웃 | `{기능}-layout.js` | `main-layout.js`, `sidebar-layout.js` |
| 다이얼로그/팝업 | `{기능}-popup.js` | `gateway-detail-popup.js` |

**규칙:**
- kebab-case 사용 (소문자 + 하이픈)
- 확장자는 `.js` 또는 `.ts` (TypeScript 사용 시)
- 컴포넌트 클래스명은 PascalCase

### 2.2 스타일 파일

```
{컴포넌트명}-styles.js    # Lit 스타일 모듈
common-styles.js          # 공통 스타일
```

### 2.3 유틸리티/헬퍼 파일

```
utils/
├── api-client.js         # API 클라이언트
├── validators.js         # 유효성 검증
└── formatters.js         # 데이터 포맷터
```

---

## 3. 컴포넌트 작성 규칙

### 3.1 Lit 컴포넌트 기본 구조

```javascript
import { LitElement, html, css } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';

/**
 * Gateway 관리 페이지
 *
 * @fires gateway-selected - 게이트웨이 선택 시 발생
 */
@customElement('gateway-page')
export class GatewayPage extends LitElement {

  // 1. 스타일 정의
  static styles = css`
    :host {
      display: block;
      padding: 20px;
    }

    .container {
      max-width: 1200px;
      margin: 0 auto;
    }
  `;

  // 2. 프로퍼티 (외부에서 전달받는 데이터)
  @property({ type: String })
  domainId = '';

  @property({ type: Number })
  pageSize = 50;

  // 3. 상태 (내부 상태 관리)
  @state()
  private gateways = [];

  @state()
  private loading = false;

  // 4. 라이프사이클 메서드
  connectedCallback() {
    super.connectedCallback();
    this.loadGateways();
  }

  // 5. 비즈니스 로직 메서드
  async loadGateways() {
    this.loading = true;
    try {
      const response = await fetch(`/rest/gateways?limit=${this.pageSize}`);
      this.gateways = await response.json();
    } catch (error) {
      console.error('Failed to load gateways:', error);
    } finally {
      this.loading = false;
    }
  }

  // 6. 이벤트 핸들러
  handleGatewayClick(gateway) {
    this.dispatchEvent(new CustomEvent('gateway-selected', {
      detail: gateway,
      bubbles: true,
      composed: true
    }));
  }

  // 7. 렌더링
  render() {
    return html`
      <div class="container">
        <h1>Gateway 관리</h1>

        ${this.loading ? html`
          <div class="loading">로딩 중...</div>
        ` : html`
          <div class="gateway-list">
            ${this.gateways.map(gw => html`
              <div class="gateway-item" @click=${() => this.handleGatewayClick(gw)}>
                <span class="gateway-code">${gw.gwCd}</span>
                <span class="gateway-name">${gw.gwNm}</span>
              </div>
            `)}
          </div>
        `}
      </div>
    `;
  }
}
```

### 3.2 컴포넌트 작성 규칙

#### 순서
1. import 구문
2. JSDoc 주석
3. `@customElement` 데코레이터
4. 클래스 정의
5. static styles
6. `@property` (외부 프로퍼티)
7. `@state` (내부 상태)
8. 라이프사이클 메서드
9. 비즈니스 로직 메서드
10. 이벤트 핸들러
11. render() 메서드

#### 프로퍼티 vs 상태
```javascript
// @property — 외부에서 전달받는 속성 (attribute로 노출됨)
@property({ type: String })
gatewayId = '';

// @state — 컴포넌트 내부 상태 (외부 노출 안 됨)
@state()
private selectedItems = [];
```

---

## 4. 네이밍 규칙

### 4.1 변수/함수 네이밍

```javascript
// camelCase 사용
const gatewayList = [];
const selectedGateway = null;

// 함수: 동사 + 명사
function loadGateways() { }
function handleGatewayClick() { }
function validateInput() { }

// Boolean: is/has/should 접두사
const isLoading = false;
const hasError = false;
const shouldUpdate = true;

// 상수: UPPER_SNAKE_CASE
const API_BASE_URL = '/rest';
const DEFAULT_PAGE_SIZE = 50;
```

### 4.2 클래스 네이밍

```javascript
// 컴포넌트: PascalCase
class GatewayPage extends LitElement { }
class DataGrid extends LitElement { }

// 서비스/유틸: PascalCase
class ApiClient { }
class ValidationUtil { }
```

### 4.3 커스텀 이벤트 네이밍

```javascript
// kebab-case, 과거형 또는 진행형
this.dispatchEvent(new CustomEvent('gateway-selected', { ... }));
this.dispatchEvent(new CustomEvent('data-changed', { ... }));
this.dispatchEvent(new CustomEvent('form-submitted', { ... }));
```

---

## 5. 스타일 작성 규칙

### 5.1 Lit CSS 스타일

```javascript
static styles = css`
  /* 1. 호스트 요소 스타일 */
  :host {
    display: block;
    box-sizing: border-box;
  }

  /* 2. 레이아웃 */
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }

  /* 3. 컴포넌트별 스타일 */
  .gateway-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 16px;
  }

  .gateway-item {
    padding: 16px;
    border: 1px solid var(--border-color);
    border-radius: 4px;
    cursor: pointer;
  }

  .gateway-item:hover {
    background-color: var(--hover-bg-color);
  }

  /* 4. 반응형 */
  @media (max-width: 768px) {
    .gateway-list {
      grid-template-columns: 1fr;
    }
  }
`;
```

### 5.2 CSS 변수 사용

```javascript
// 공통 스타일에서 정의
:host {
  --primary-color: #1976d2;
  --border-color: #e0e0e0;
  --hover-bg-color: #f5f5f5;
  --spacing-unit: 8px;
}

// 컴포넌트에서 사용
.button {
  background-color: var(--primary-color);
  padding: calc(var(--spacing-unit) * 2);
}
```

### 5.3 공통 스타일 공유

```javascript
// common-styles.js
import { css } from 'lit';

export const buttonStyles = css`
  .button {
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
  }
`;

// 컴포넌트에서 사용
import { buttonStyles } from './styles/common-styles.js';

static styles = [
  buttonStyles,
  css`
    /* 컴포넌트 고유 스타일 */
  `
];
```

---

## 6. API 연동 규칙

### 6.1 API 클라이언트 패턴

```javascript
// utils/api-client.js
export class ApiClient {
  constructor(baseUrl = '/rest') {
    this.baseUrl = baseUrl;
  }

  async get(endpoint, params = {}) {
    const queryString = new URLSearchParams(params).toString();
    const url = `${this.baseUrl}${endpoint}${queryString ? '?' + queryString : ''}`;

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`);
    }

    return response.json();
  }

  async post(endpoint, data) {
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`);
    }

    return response.json();
  }

  // put, delete 메서드도 동일 패턴으로 작성
}

export const apiClient = new ApiClient();
```

### 6.2 컴포넌트에서 사용

```javascript
import { apiClient } from '../utils/api-client.js';

export class GatewayPage extends LitElement {
  async loadGateways() {
    try {
      this.loading = true;
      const data = await apiClient.get('/gateways', {
        limit: this.pageSize
      });
      this.gateways = data.items || [];
    } catch (error) {
      console.error('Failed to load gateways:', error);
      this.showError('게이트웨이 목록을 불러오는데 실패했습니다.');
    } finally {
      this.loading = false;
    }
  }

  async saveGateway(gateway) {
    try {
      this.saving = true;
      const result = await apiClient.post('/gateways', gateway);
      this.showSuccess('게이트웨이가 저장되었습니다.');
      return result;
    } catch (error) {
      console.error('Failed to save gateway:', error);
      this.showError('게이트웨이 저장에 실패했습니다.');
    } finally {
      this.saving = false;
    }
  }
}
```

---

## 7. 이벤트 처리 규칙

### 7.1 커스텀 이벤트 발생

```javascript
// 이벤트 발생 (bubbles: true, composed: true로 섀도우 DOM 경계 통과)
this.dispatchEvent(new CustomEvent('gateway-selected', {
  detail: { gateway: this.selectedGateway },
  bubbles: true,
  composed: true
}));
```

### 7.2 이벤트 리스닝

```javascript
// 템플릿에서 직접 리스닝
render() {
  return html`
    <button @click=${this.handleClick}>클릭</button>
    <input @input=${this.handleInput} />
    <custom-component @gateway-selected=${this.handleGatewaySelected}></custom-component>
  `;
}

// 프로그래매틱 리스닝 (connectedCallback에서)
connectedCallback() {
  super.connectedCallback();
  this.addEventListener('custom-event', this.handleCustomEvent);
}

disconnectedCallback() {
  super.disconnectedCallback();
  this.removeEventListener('custom-event', this.handleCustomEvent);
}
```

---

## 8. 상태 관리 규칙

### 8.1 로컬 상태

```javascript
// 간단한 컴포넌트 상태는 @state 사용
@state()
private loading = false;

@state()
private items = [];
```

### 8.2 전역 상태 (Store 패턴)

```javascript
// store/gateway-store.js
import { reactive } from '@lit/reactive-element';

class GatewayStore {
  @reactive()
  gateways = [];

  @reactive()
  selectedGateway = null;

  async loadGateways() {
    // API 호출 및 상태 업데이트
  }

  selectGateway(gateway) {
    this.selectedGateway = gateway;
  }
}

export const gatewayStore = new GatewayStore();
```

---

## 9. TypeScript 사용 시 규칙

### 9.1 타입 정의

```typescript
// types/gateway.ts
export interface Gateway {
  id: string;
  gwCd: string;
  gwNm: string;
  gwIp: string;
  stageCd: string;
  status: 'active' | 'inactive';
}

export interface GatewayListResponse {
  items: Gateway[];
  total: number;
  page: number;
}
```

### 9.2 컴포넌트에서 타입 사용

```typescript
import { LitElement, html, css } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import type { Gateway } from '../types/gateway';

@customElement('gateway-page')
export class GatewayPage extends LitElement {

  @property({ type: String })
  domainId!: string;

  @state()
  private gateways: Gateway[] = [];

  @state()
  private selectedGateway: Gateway | null = null;

  async loadGateways(): Promise<void> {
    // ...
  }

  handleGatewayClick(gateway: Gateway): void {
    this.selectedGateway = gateway;
  }
}
```

---

## 10. 테스트 작성 규칙

### 10.1 컴포넌트 테스트 (Web Test Runner 사용)

```javascript
// gateway-page.test.js
import { expect, fixture, html } from '@open-wc/testing';
import './gateway-page.js';

describe('GatewayPage', () => {
  it('renders gateway list', async () => {
    const el = await fixture(html`
      <gateway-page></gateway-page>
    `);

    expect(el).to.exist;
    expect(el.shadowRoot.querySelector('.gateway-list')).to.exist;
  });

  it('loads gateways on connected', async () => {
    const el = await fixture(html`
      <gateway-page></gateway-page>
    `);

    // API 호출 대기
    await el.updateComplete;

    expect(el.gateways).to.be.an('array');
  });
});
```

---

## 11. 주석 작성 규칙

### 11.1 JSDoc 주석

```javascript
/**
 * Gateway 관리 페이지 컴포넌트
 *
 * @element gateway-page
 * @fires {CustomEvent} gateway-selected - 게이트웨이 선택 시 발생
 * @fires {CustomEvent} gateway-deleted - 게이트웨이 삭제 시 발생
 */
@customElement('gateway-page')
export class GatewayPage extends LitElement {

  /**
   * 도메인 ID
   * @type {string}
   */
  @property({ type: String })
  domainId = '';

  /**
   * 게이트웨이 목록을 서버에서 로드합니다.
   *
   * @returns {Promise<void>}
   */
  async loadGateways() {
    // ...
  }
}
```

### 11.2 인라인 주석

```javascript
render() {
  return html`
    <div class="container">
      <!-- 헤더 영역 -->
      <header class="page-header">
        <h1>Gateway 관리</h1>
      </header>

      <!-- 검색 폼 -->
      <search-form @search=${this.handleSearch}></search-form>

      <!-- 목록 영역 -->
      ${this.renderGatewayList()}
    </div>
  `;
}
```

---

## 12. 성능 최적화 규칙

### 12.1 불필요한 렌더링 방지

```javascript
// shouldUpdate로 렌더링 조건 제어
shouldUpdate(changedProperties) {
  // domainId가 변경될 때만 렌더링
  return changedProperties.has('domainId');
}

// 또는 @property에서 hasChanged 옵션 사용
@property({
  type: String,
  hasChanged: (newVal, oldVal) => newVal !== oldVal
})
gatewayId = '';
```

### 12.2 대량 데이터 렌더링

```javascript
// repeat 디렉티브 사용으로 효율적인 리스트 렌더링
import { repeat } from 'lit/directives/repeat.js';

render() {
  return html`
    <div class="gateway-list">
      ${repeat(
        this.gateways,
        (gateway) => gateway.id,  // 키 함수
        (gateway) => html`         // 템플릿 함수
          <gateway-item .gateway=${gateway}></gateway-item>
        `
      )}
    </div>
  `;
}
```

---

## 13. 접근성(A11y) 규칙

```javascript
render() {
  return html`
    <!-- 시맨틱 HTML 사용 -->
    <button
      aria-label="게이트웨이 추가"
      @click=${this.handleAdd}
    >
      <span class="icon">+</span>
    </button>

    <!-- role과 aria 속성 추가 -->
    <div
      role="listbox"
      aria-label="게이트웨이 목록"
    >
      ${this.gateways.map(gw => html`
        <div
          role="option"
          aria-selected=${gw.id === this.selectedGateway?.id}
          tabindex="0"
          @click=${() => this.handleSelect(gw)}
          @keydown=${(e) => this.handleKeydown(e, gw)}
        >
          ${gw.gwNm}
        </div>
      `)}
    </div>
  `;
}

// 키보드 네비게이션 지원
handleKeydown(e, gateway) {
  if (e.key === 'Enter' || e.key === ' ') {
    e.preventDefault();
    this.handleSelect(gateway);
  }
}
```

---

## 14. 코드 품질 도구

### 14.1 ESLint 설정

```json
{
  "extends": [
    "eslint:recommended",
    "@open-wc/eslint-config"
  ],
  "rules": {
    "indent": ["error", 2],
    "quotes": ["error", "single"],
    "semi": ["error", "always"],
    "no-console": ["warn", { "allow": ["warn", "error"] }]
  }
}
```

### 14.2 Prettier 설정

```json
{
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false,
  "semi": true,
  "singleQuote": true,
  "trailingComma": "es5",
  "bracketSpacing": true,
  "arrowParens": "avoid"
}
```

---

## 15. 개선 권장 사항

### 15.1 신규 컴포넌트 작성 시 (필수)

- [ ] Lit 컴포넌트 구조 준수 (스타일 → 프로퍼티 → 상태 → 메서드 → render 순서)
- [ ] `@customElement` 데코레이터로 커스텀 엘리먼트 등록
- [ ] 외부 프로퍼티는 `@property`, 내부 상태는 `@state` 사용
- [ ] 커스텀 이벤트는 `bubbles: true, composed: true` 옵션 사용
- [ ] JSDoc 주석 작성 (최소한 클래스, public 메서드)

### 15.2 코드 품질 (권장)

- [ ] ESLint + Prettier 적용
- [ ] TypeScript 사용 검토 (타입 안정성)
- [ ] 컴포넌트별 단위 테스트 작성
- [ ] 접근성 (ARIA 속성, 키보드 네비게이션) 고려
- [ ] 성능 최적화 (shouldUpdate, repeat 디렉티브)

### 15.3 향후 개선

- [ ] 공통 스타일 시스템 정립 (디자인 토큰)
- [ ] Storybook 도입으로 컴포넌트 문서화
- [ ] E2E 테스트 추가 (Playwright)
- [ ] 번들 최적화 (코드 스플리팅)

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 1.0 | 2026-03-11 | 초기 작성 — Things Factory/Lit 기반 프론트엔드 코딩 컨벤션 | Claude Code |
