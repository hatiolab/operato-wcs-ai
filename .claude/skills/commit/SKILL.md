---
name: commit
description: Git에 변경 사항을 커밋합니다. 커밋 메시지는 변경 내용을 분석하여 자동 생성하거나 사용자가 지정할 수 있습니다.
disable-model-invocation: true
argument-hint: "[커밋 메시지 (선택)]"
---

# Git 커밋

Git에 현재 변경 사항을 커밋합니다. CLAUDE.md의 Git 규칙을 준수하여 안전하게 커밋을 생성합니다.

## 실행 절차

### 1. 사전 확인

```bash
# 현재 브랜치 및 상태 확인
git status --short
git branch --show-current

# 변경 파일 및 diff 확인
git diff --stat
git diff
```

### 2. 변경 내용 분석

- 변경된 파일 목록을 확인한다
- 각 파일의 변경 사항(추가/수정/삭제)을 파악한다
- 최근 커밋 메시지 스타일을 참고한다 (`git log -5 --oneline`)

### 3. 커밋 메시지 작성

사용자가 `$ARGUMENTS`로 커밋 메시지를 제공한 경우:
- 해당 메시지를 그대로 사용한다

사용자가 메시지를 제공하지 않은 경우:
- 변경 내용을 분석하여 **간결하고 명확한** 커밋 메시지를 자동 생성한다
- 형식: `동사 + 목적어` (예: "Add user authentication", "Update README", "Fix typo in CLAUDE.md")
- 한국어 프로젝트는 한국어로, 영어 프로젝트는 영어로 작성한다

### 4. 파일 스테이징 및 커밋

```bash
# 특정 파일만 스테이징 (민감한 파일 제외)
git add <변경된_파일들>

# 커밋 생성 (Co-Authored-By 태그 포함)
git commit -m "$(cat <<'EOF'
<커밋_메시지>

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"

# 커밋 결과 확인
git status
git log -1 --stat
```

### 5. 스테이징 규칙

**포함할 파일:**
- 소스 코드 (`.java`, `.ts`, `.md` 등)
- 설정 파일 (`build.gradle`, `package.json`, `CLAUDE.md` 등)
- 문서 파일 (`docs/` 디렉토리 내 파일)

**제외할 파일 (절대 커밋하지 않음):**
- 민감한 정보가 포함된 파일 (`.env`, `credentials.json`, `secrets.yml` 등)
- 빌드 산출물 (`build/`, `dist/`, `target/`, `node_modules/` 등)
- IDE 설정 (`.idea/`, `.vscode/` — 프로젝트에 필요한 경우만 예외)

### 6. Git 안전 규칙 (CLAUDE.md 준수)

**절대 금지:**
- ❌ 사용자의 명시적 지시 없이 자동 커밋
- ❌ `git push --force` (사용자가 명시적으로 요청하지 않는 한)
- ❌ `git reset --hard`, `git clean -f` 등 파괴적 명령
- ❌ `--no-verify`, `--no-gpg-sign` 등 훅 스킵 옵션
- ❌ 기존 커밋 수정 (`--amend`) — 새 커밋 생성 원칙

**필수 확인:**
- ✅ 커밋 전 빌드 및 테스트 통과 여부 확인 (가능한 경우)
- ✅ pre-commit 훅 실패 시 문제 수정 후 **새 커밋** 생성 (--amend 금지)
- ✅ 민감한 파일이 스테이징되지 않았는지 확인

## 커밋 메시지 예시

### 신규 기능 추가
```
Add WCS function definition documents

- Create WCS-기능정의.md (Manager features)
- Create ECS-모듈-기능정의.md (4 ECS modules)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### 문서 업데이트
```
Update CLAUDE.md with project structure

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### 버그 수정
```
Fix typo in requirements document

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

## 출력 형식

커밋 완료 후 다음 정보를 사용자에게 보고한다:

```
✅ 커밋 완료

📝 커밋 메시지:
<커밋_메시지>

📂 변경 파일:
- path/to/file1.md (신규)
- path/to/file2.java (수정)

🔗 커밋 해시: <commit_hash>
```

## 주의사항

- **사용자가 명시적으로 커밋을 요청한 경우에만** 이 Skill을 실행한다
- 커밋 전 반드시 `git status`와 `git diff`로 변경 내용을 확인한다
- 민감한 파일(`.env` 등)이 포함되어 있으면 경고하고 커밋하지 않는다
- CLAUDE.md에 명시된 Git 안전 규칙을 엄격히 준수한다
