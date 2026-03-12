# 개발 환경 구성 가이드

> Operato WCS 프로젝트 개발을 위한 환경 설정 가이드

## 목차

1. [필수 소프트웨어](#필수-소프트웨어)
2. [백엔드 환경 설정](#백엔드-환경-설정)
3. [프론트엔드 환경 설정](#프론트엔드-환경-설정)
4. [데이터베이스 설정](#데이터베이스-설정)
5. [메시지 브로커 설정](#메시지-브로커-설정)
6. [IDE 설정](#ide-설정)
7. [환경 변수 설정](#환경-변수-설정)
8. [개발 서버 실행](#개발-서버-실행)
9. [문제 해결](#문제-해결)

---

## 필수 소프트웨어

### 1. Java Development Kit (JDK)

**버전:** JDK 17 이상

```bash
# macOS (Homebrew)
brew install openjdk@17

# 설치 확인
java -version

# JAVA_HOME 설정 (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk

# 설치 확인
java -version

# JAVA_HOME 설정
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### 2. Gradle

**버전:** 최신 안정 버전 (프로젝트에 Gradle Wrapper 포함)

```bash
# Gradle Wrapper 사용 (권장)
./gradlew --version
```

### 3. Node.js & npm

**버전:** Node.js 18.x 이상

```bash
# macOS (Homebrew)
brew install node@18

# 설치 확인
node --version
npm --version
```

**Linux (Ubuntu/Debian):**
```bash
# NodeSource 저장소 추가
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -

# 설치
sudo apt install -y nodejs

# 설치 확인
node --version
npm --version
```

### 4. Yarn

**버전:** 1.x (Classic)

```bash
# npm을 통한 설치
npm install -g yarn

# 설치 확인
yarn --version
```

### 5. Git

```bash
# macOS (Homebrew)
brew install git

# Linux (Ubuntu/Debian)
sudo apt install git

# 설치 확인
git --version
```

### 6. Docker & Docker Compose (선택)

운영 환경 테스트 및 컨테이너 배포 시 필요

```bash
# macOS
brew install --cask docker

# Linux: Docker 공식 문서 참조
# https://docs.docker.com/engine/install/

# 설치 확인
docker --version
docker-compose --version
```

---

## 백엔드 환경 설정

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd operato-wcs-ai
```

### 2. Gradle 의존성 설치 및 빌드

```bash
# 프론트엔드 제외 빌드 (빠른 검증)
SKIP_FRONTEND=true ./gradlew clean build -x test

# 전체 빌드 (프론트엔드 포함)
./gradlew buildAll
```

### 3. 백엔드 설정 파일 구성

**`src/main/resources/application-dev.properties`** 확인 및 수정

```properties
# 서버 포트
server.port=9190

# 데이터베이스 연결
spring.datasource.url=jdbc:postgresql://localhost:5432/wcs_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# RabbitMQ 설정
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

---

## 프론트엔드 환경 설정

### 1. 의존성 설치

```bash
cd frontend
yarn install
```

### 2. Lerna Bootstrap

```bash
# 프로젝트 루트에서
./gradlew lernaBootstrap

# 또는 frontend 디렉토리에서 직접
cd frontend
yarn bootstrap
```

### 3. 프론트엔드 빌드

```bash
# 전체 빌드
yarn build

# 클라이언트만 빌드
yarn build:client
```

### 4. 개발 서버 실행

```bash
# operato-wcs-ui 개발 서버 (포트 5908)
yarn wcs:dev
```

---

## 데이터베이스 설정

### PostgreSQL

**Docker를 사용한 로컬 설정 (권장):**

```bash
docker run -d \
  --name wcs-postgres \
  -e POSTGRES_DB=wcs_db \
  -e POSTGRES_USER=wcs_user \
  -e POSTGRES_PASSWORD=wcs_pass \
  -p 5432:5432 \
  postgres:15
```

**직접 설치 (macOS):**

```bash
# Homebrew로 설치
brew install postgresql@15
brew services start postgresql@15

# 데이터베이스 생성
createdb wcs_db
```

**직접 설치 (Linux):**

```bash
sudo apt install postgresql-15
sudo systemctl start postgresql
sudo -u postgres createdb wcs_db
```

---

## 메시지 브로커 설정

### RabbitMQ

**Docker를 사용한 로컬 설정 (권장):**

```bash
docker run -d \
  --name wcs-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

**관리 콘솔 접속:**
- URL: http://localhost:15672
- Username: `guest`
- Password: `guest`

**직접 설치 (macOS):**

```bash
brew install rabbitmq
brew services start rabbitmq
```

**직접 설치 (Linux):**

```bash
sudo apt install rabbitmq-server
sudo systemctl start rabbitmq-server
sudo rabbitmq-plugins enable rabbitmq_management
```

---

## IDE 설정

### Visual Studio Code (백엔드 + 프론트엔드 통합 환경)

#### 1. 필수 확장 프로그램 설치

**백엔드 (Java/Spring Boot):**
- Extension Pack for Java (Microsoft)
- Spring Boot Extension Pack (VMware)
- Gradle for Java
- Debugger for Java
- Test Runner for Java

**프론트엔드 (JavaScript/TypeScript/Lit):**
- ESLint
- Prettier - Code formatter
- Lit Plugin
- JavaScript and TypeScript Nightly (또는 기본 TypeScript support)

**공통 유틸리티:**
- GitLens
- Docker
- YAML
- Markdown All in One

#### 2. VSCode 설정 (`.vscode/settings.json`)

프로젝트에 이미 구성된 설정:

```json
{
  "java.dependency.packagePresentation": "hierarchical",
  "java.project.sourcePaths": [
    "src/main/java"
  ],
  "java.project.referencedLibraries": [],
  "java.configuration.updateBuildConfiguration": "automatic",
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "typescript.tsdk": "frontend/node_modules/typescript/lib",
  "files.exclude": {
    "**/.gradle": true,
    "**/build": true,
    "**/dist-client": true,
    "**/dist-server": true,
    "**/dist-app": true
  }
}
```

#### 3. 디버그 설정 (`.vscode/launch.json`)

프로젝트에 이미 구성된 설정:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "WCS (dev)",
      "request": "launch",
      "mainClass": "operato.logis.OperatoWcsBootApplication",
      "projectName": "operato-wcs-ai",
      "args": "--spring.profiles.active=dev"
    }
  ]
}
```

**사용 방법:**
1. VSCode에서 `F5` 키를 누르거나 디버그 패널에서 "WCS (dev)" 선택
2. 개발 프로파일로 Spring Boot 애플리케이션이 실행됨
3. 브레이크포인트를 설정하여 디버깅 가능

#### 4. Java 환경 설정

**Java Home 자동 감지:**
VSCode의 Java Extension이 자동으로 JDK를 감지합니다. 수동 설정이 필요한 경우:

```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/usr/lib/jvm/java-17-openjdk-amd64",
      "default": true
    }
  ]
}
```

**macOS에서 Java Home 확인:**
```bash
/usr/libexec/java_home -v 17
```

#### 5. 작업 환경 구성

**터미널 사용:**
- VSCode 내장 터미널 사용 (`Ctrl+` ` 또는 View → Terminal)
- 백엔드 실행: `./gradlew bootRun --args='--spring.profiles.active=dev'`
- 프론트엔드 실행: `cd frontend && yarn wcs:dev`

**멀티 루트 워크스페이스 (선택):**
백엔드와 프론트엔드를 별도로 관리하려면:

```json
{
  "folders": [
    {
      "path": "."
    },
    {
      "path": "frontend"
    }
  ],
  "settings": {}
}
```

#### 6. 코드 스타일 설정

- 백엔드: [`backend-coding-conventions.md`](backend-coding-conventions.md) 참조
- 프론트엔드: [`frontend-coding-conventions.md`](frontend-coding-conventions.md) 참조

---

## 환경 변수 설정

### macOS/Linux (.bashrc 또는 .zshrc)

```bash
# Java
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# Node.js
export NODE_OPTIONS="--max-old-space-size=4096"

# 프로젝트 환경
export WCS_ENV=development
export WCS_PORT=9190
```

### 환경별 설정 파일

- **개발 환경:** `application-dev.properties`
- **운영 환경:** `application-prod.properties`
- **테스트 환경:** `application-test.properties`

---

## 개발 서버 실행

### 백엔드 실행

**방법 1: Gradle bootRun**
```bash
# 개발 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**방법 2: JAR 실행**
```bash
# 빌드
SKIP_FRONTEND=true ./gradlew clean build -x test

# 실행
java -jar -Dspring.profiles.active=dev build/libs/operato-wcs-ai-*.jar
```

**방법 3: Claude Code 스킬 사용**
```bash
/run-backend
```

**접속 확인:**
- API: http://localhost:9190
- Health Check: http://localhost:9190/actuator/health

### 프론트엔드 실행

```bash
cd frontend
yarn wcs:dev
```

**접속:**
- URL: http://localhost:5908

### 통합 실행 (Docker Compose)

```bash
# 개발 환경
docker-compose -f docker-compose.yml up

# 운영 환경
docker-compose -f docker-compose.prod.yml up
```

---

## 문제 해결

### 1. "copyfiles: command not found" 에러

**원인:** npm 의존성 미설치

**해결:**
```bash
cd frontend
yarn install
./gradlew lernaBootstrap
```

### 2. "No inputs were found in config file" (TypeScript)

**원인:** TypeScript 설정에 `allowJs` 옵션 누락

**해결:** 이미 프로젝트에 설정되어 있음. 최신 코드 pull 필요
```bash
git pull origin main
```

### 3. Java 버전 불일치

**에러:** `Unsupported class file major version`

**해결:**
```bash
# 현재 Java 버전 확인
java -version

# Java 17로 전환 (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Java 17로 전환 (Linux)
sudo update-alternatives --config java
```

### 4. 포트 이미 사용 중

**에러:** `Port 9190 is already in use`

**해결:**
```bash
# 사용 중인 프로세스 확인 (macOS/Linux)
lsof -i :9190

# 프로세스 종료
kill -9 <PID>

# 또는 다른 포트 사용
./gradlew bootRun --args='--server.port=9191'
```

### 5. Gradle 빌드 실패

**해결:**
```bash
# Gradle 캐시 삭제
./gradlew clean --no-daemon

# Gradle Wrapper 재설치
./gradlew wrapper --gradle-version=8.5

# 의존성 다운로드 강제
./gradlew build --refresh-dependencies
```

### 6. 프론트엔드 빌드 실패

**해결:**
```bash
# node_modules 삭제 및 재설치
cd frontend
rm -rf node_modules
rm -rf packages/*/node_modules
yarn cache clean
yarn install

# Lerna bootstrap 재실행
yarn bootstrap
```

---

## 추가 참고 문서

- **프로젝트 개요:** [`docs/overview/overview.md`](../overview/overview.md)
- **아키텍처:** [`docs/architecture/architecture.md`](../architecture/architecture.md)
- **백엔드 아키텍처:** [`docs/architecture/backend-architecture.md`](../architecture/backend-architecture.md)
- **프론트엔드 구조:** [`docs/architecture/frontend-structure.md`](../architecture/frontend-structure.md)
- **배포 가이드:** [`docs/operations/deployment.md`](../operations/deployment.md)
- **백엔드 코딩 컨벤션:** [`backend-coding-conventions.md`](backend-coding-conventions.md)
- **프론트엔드 코딩 컨벤션:** [`frontend-coding-conventions.md`](frontend-coding-conventions.md)
- **개발 시작 체크리스트:** [`docs/checklist/dev-start-checklist.md`](../checklist/dev-start-checklist.md)

---

## 도움말

문제가 지속되면 다음을 확인하세요:

1. **로그 확인**
   - 백엔드: `logs/spring.log`
   - 프론트엔드: 브라우저 콘솔 (F12)

2. **환경 변수 확인**
   ```bash
   echo $JAVA_HOME
   echo $NODE_VERSION
   ```

3. **포트 확인**
   ```bash
   netstat -an | grep LISTEN | grep -E '(9190|5908)'
   ```

4. **디스크 공간 확인**
   ```bash
   df -h
   ```

5. **메모리 확인**
   ```bash
   # macOS
   vm_stat

   # Linux
   free -h
   ```
