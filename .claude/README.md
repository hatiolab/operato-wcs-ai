# .claude/ 디렉토리

Claude Code 프로젝트 레벨 설정 및 스킬 디렉토리.

## 파일 구조

```
.claude/
├── settings.json          # 프로젝트 레벨 Claude Code 설정
├── skills/                # Claude Code 스킬 (반복 작업 자동화)
│   └── README.md          # 스킬 목록 및 사용법
└── README.md              # 이 파일
```

## settings.json

토큰 사용량 절감을 위한 프로젝트 레벨 환경 변수 설정.

| 환경 변수 | 값 | 설명 |
|----------|-----|------|
| `CLAUDE_CODE_AUTOCOMPACT_PCT_OVERRIDE` | `70` | 컨텍스트 70% 도달 시 자동 압축 (기본 ~95%) |
| `DISABLE_NON_ESSENTIAL_MODEL_CALLS` | `1` | 제안·부가 텍스트 등 비필수 모델 호출 제거 |

### 추가 가능한 환경 변수

| 환경 변수 | 설명 |
|----------|------|
| `MAX_THINKING_TOKENS` | 추론(thinking)에 사용하는 토큰 상한 설정 |
| `CLAUDE_CODE_MAX_OUTPUT_TOKENS` | 응답 당 최대 출력 토큰 제한 (기본 32,000) |
| `CLAUDE_CODE_FILE_READ_MAX_OUTPUT_TOKENS` | 파일 읽기 시 토큰 제한 |

## 토큰 절감 관련 파일

| 파일 | 위치 | 역할 |
|------|------|------|
| `.claudeignore` | 프로젝트 루트 | Claude가 자동 수집하지 않을 파일/디렉토리 지정 |
| `settings.json` | `.claude/` | 환경 변수 기반 토큰 절감 설정 |
| `CLAUDE.md` | 프로젝트 루트 | 매 대화마다 로드 — 간결할수록 토큰 절감 |

## 사용 시 토큰 절감 팁

### 대화 관리

- `/compact` — 대화가 길어지면 컨텍스트 수동 압축
- `/clear` — 작업 주제가 바뀌면 새 대화 시작 (가장 효과 큼)
- 하나의 대화에 여러 주제를 섞지 말 것

### 프롬프트 작성

- 파일명·함수명을 구체적으로 명시 (탐색 토큰 절감)
  - Bad: "서비스 클래스 수정해줘"
  - Good: "OrderService.java의 findById에 null 체크 추가해줘"
- 한 번에 하나의 작업만 요청
- "전체 파일 보여줘" 대신 "변경된 부분만 보여줘"

### 모델 선택

- `/fast` — 같은 모델이지만 출력 속도 우선 (단순 작업에 적합)
- `claude --model sonnet` — 단순 작업(검색, 간단한 수정)에 저비용 모델 사용
- `claude --print "질문"` — 단발성 질문은 one-shot 모드로 세션 오버헤드 제거

### CLAUDE.md 관리

- 상세 내용은 `docs/`로 분리하고 CLAUDE.md에는 링크만 유지
- 빈 섹션("추후 추가 예정")은 제거
- 코드블록보다 인라인 코드가 토큰 효율적
