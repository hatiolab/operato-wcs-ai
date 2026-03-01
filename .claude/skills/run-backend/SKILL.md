---
name: run-backend
description: Spring Boot 백엔드 서버를 실행합니다. Gradle bootRun 또는 사전 빌드된 JAR 파일 실행을 지원하며, 프로파일 및 포트 설정이 가능합니다.
disable-model-invocation: false
argument-hint: "[옵션: --jar | --profile=<profile> | --port=<port> | --debug]"
---

# 백엔드 서버 실행

Spring Boot 애플리케이션을 로컬 환경에서 실행합니다.

## 실행 절차

### 1. 사전 확인

1. **Java 버전 확인**
   ```bash
   java -version
   # 또는
   ./gradlew --version
   ```
   - Spring Boot 3.2.4는 Java 17+ 필요
   - gradle.properties에 설정된 Java 버전 사용

2. **포트 충돌 확인**
   ```bash
   # 프로파일에 따른 포트 확인
   PROFILE=${PROFILE:-default}

   if [ "$PROFILE" != "default" ] && [ -f "src/main/resources/application-${PROFILE}.properties" ]; then
       # 프로파일별 properties 먼저 확인
       PORT=$(grep "^server.port=" src/main/resources/application-${PROFILE}.properties | cut -d'=' -f2)
   fi

   # 프로파일에 없으면 기본 properties 확인
   if [ -z "$PORT" ]; then
       PORT=$(grep "^server.port=" src/main/resources/application.properties | cut -d'=' -f2)
   fi

   # 그래도 없으면 Spring Boot 기본값
   PORT=${PORT:-8080}

   # 해당 포트가 사용 중인지 확인
   lsof -i :${PORT}
   ```
   - **프로파일별 포트 우선순위**:
     1. `application-{profile}.properties`의 server.port
     2. `application.properties`의 server.port
     3. Spring Boot 기본값 (8080)
   - 해당 포트가 사용 중이면 경고
   - 다른 포트로 실행하거나 기존 프로세스 종료 제안

3. **JAR 모드인 경우 파일 존재 확인**
   ```bash
   ls -lh build/libs/operato-wcs-ai.jar
   ```
   - JAR 파일이 없으면 `/deploy-backend` 스킬 실행 제안

### 2. 인자 파싱

`$ARGUMENTS`로 전달된 옵션을 파싱:

- `--jar` 또는 `-j`: JAR 파일 직접 실행 (기본값: Gradle bootRun)
- `--profile=<profile>` 또는 `-p <profile>`: Spring 프로파일 지정 (dev, prod, factory 등)
  - 프로파일 지정 시 `application-{profile}.properties`의 설정 우선 적용
- `--port=<port>`: 서버 포트 지정 (미지정 시 프로파일별 properties의 server.port 사용)
- `--debug` 또는 `-d`: 디버그 모드 활성화 (포트 5005)
- 인자가 없으면 기본 동작: Gradle bootRun으로 실행

### 3. 실행 모드 선택

#### 모드 A: Gradle bootRun (기본값)

```bash
# 기본 실행
./gradlew bootRun

# 프로파일 지정
./gradlew bootRun --args='--spring.profiles.active=dev'

# 포트 지정
./gradlew bootRun --args='--server.port=9090'

# 디버그 모드
./gradlew bootRun --args='--spring.profiles.active=dev' --debug-jvm
```

**장점:**
- 소스 코드 수정 시 재시작 쉬움
- Gradle 캐시 활용으로 빠른 재시작
- 개발 환경에 최적화

**단점:**
- 초기 시작 시간이 JAR 직접 실행보다 느림

#### 모드 B: JAR 직접 실행 (--jar)

```bash
# 기본 실행
java -jar build/libs/operato-wcs-ai.jar

# 프로파일 지정
java -jar -Dspring.profiles.active=prod build/libs/operato-wcs-ai.jar

# 포트 지정
java -jar -Dserver.port=9090 build/libs/operato-wcs-ai.jar

# 디버그 모드
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
  -jar build/libs/operato-wcs-ai.jar
```

**장점:**
- 빠른 시작 시간
- 운영 환경과 동일한 방식
- 메모리 사용량 예측 가능

**단점:**
- 사전에 빌드 필요 (`/deploy-backend` 스킬 사용)
- 소스 수정 시 재빌드 필요

### 4. 실행 및 모니터링

```bash
# 서버 실행 (포그라운드)
<실행_명령어>

# 로그 출력 확인
# - Spring Boot 배너 출력
# - 애플리케이션 시작 로그
# - 포트 바인딩 확인
# - 초기화 완료 메시지
```

**로그에서 확인할 항목:**
- `Started [ApplicationName] in X.XXX seconds` — 시작 완료
- `Tomcat started on port(s): <포트> (http)` — 포트 확인 (application.properties 설정 확인)
- 에러 메시지 유무

### 5. 헬스 체크

서버 시작 후 헬스 체크 수행:

```bash
# 30초 대기 후 헬스 체크
sleep 30

# 프로파일에 따른 포트 확인
PROFILE=${PROFILE:-default}

if [ "$PROFILE" != "default" ] && [ -f "src/main/resources/application-${PROFILE}.properties" ]; then
    # 프로파일별 properties 먼저 확인
    PORT=$(grep "^server.port=" src/main/resources/application-${PROFILE}.properties | cut -d'=' -f2)
fi

# 프로파일에 없으면 기본 properties 확인
if [ -z "$PORT" ]; then
    PORT=$(grep "^server.port=" src/main/resources/application.properties | cut -d'=' -f2)
fi

# 그래도 없으면 Spring Boot 기본값
PORT=${PORT:-8080}

# --port 옵션으로 오버라이드한 경우
if [ -n "$CUSTOM_PORT" ]; then
    PORT=$CUSTOM_PORT
fi

# Actuator 헬스 체크
curl -f http://localhost:${PORT}/actuator/health || echo "❌ 헬스 체크 실패"
```

**헬스 체크 응답 예시:**
```json
{
  "status": "UP"
}
```

### 6. 결과 보고

서버 실행 후 다음 정보를 사용자에게 보고:

```
✅ **백엔드 서버 실행 중!**

**실행 모드:**
- Gradle bootRun (또는 JAR 직접 실행)

**서버 정보:**
- URL: http://localhost:<포트>
- 프로파일: dev (또는 지정된 프로파일)
- 포트: <application.properties의 server.port 또는 지정된 포트>
- 디버그 포트: 5005 (디버그 모드인 경우)

**헬스 체크:**
- Status: UP ✅

**유용한 명령어:**
- 헬스 체크: curl http://localhost:<포트>/actuator/health
- 서버 종료: Ctrl+C (포그라운드 실행 시)

**다음 단계:**
- API 테스트: http://localhost:<포트>/api/...
- Swagger UI: http://localhost:<포트>/swagger-ui.html (활성화된 경우)

**참고:**
- **포트 결정 우선순위** (Spring Boot 표준):
  1. `--port` 옵션 (최우선)
  2. `application-{profile}.properties`의 server.port
  3. `application.properties`의 server.port (현재: 9500)
  4. Spring Boot 기본값 (8080)
```

## 옵션별 실행 예시

### 기본 실행 (Gradle bootRun)

```bash
/run-backend
```

실행 명령어:
```bash
./gradlew bootRun
```

### JAR 직접 실행

```bash
/run-backend --jar
```

실행 명령어:
```bash
java -jar build/libs/operato-wcs-ai.jar
```

### 프로파일 지정

**개발 환경 (dev)**
```bash
/run-backend --profile=dev
```
실행 명령어:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```
포트: `application-dev.properties`의 server.port (현재: 9500)

**운영 환경 (prod) - JAR 실행**
```bash
/run-backend --jar --profile=prod
```
실행 명령어:
```bash
java -jar -Dspring.profiles.active=prod build/libs/operato-wcs-ai.jar
```
포트: `application-prod.properties`의 server.port (없으면 application.properties 사용)

**사용 가능한 프로파일**: dev, prod, factory, demobox, ildong, operato2, postgres

### 포트 변경

```bash
/run-backend --port=9090
```

실행 명령어:
```bash
./gradlew bootRun --args='--server.port=9090'
```

### 디버그 모드

```bash
/run-backend --debug
```

실행 명령어:
```bash
./gradlew bootRun --debug-jvm
```

**디버거 연결:**
- IntelliJ IDEA: Run → Attach to Process → localhost:5005
- VSCode: Debug → Attach to Remote

### 복합 옵션

```bash
/run-backend --jar --profile=dev --port=9090 --debug
```

실행 명령어:
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
  -jar -Dspring.profiles.active=dev -Dserver.port=9090 \
  build/libs/operato-wcs-ai.jar
```

## 에러 처리

### Java 버전 불일치

```
❌ Java 버전이 요구사항을 만족하지 않습니다.

현재: Java 8
필요: Java 17+

해결 방법:
export JAVA_HOME=$(/usr/libexec/java_home -v 18)
```

### 포트 충돌

```
❌ 포트 8080이 이미 사용 중입니다.

현재 사용 중인 프로세스:
java    12345 user   123u  IPv6 0x... TCP *:8080 (LISTEN)

해결 방법:
1. 기존 프로세스 종료: kill 12345
2. 다른 포트 사용: /run-backend --port=9090
```

### JAR 파일 없음 (--jar 사용 시)

```
❌ JAR 파일이 없습니다.

경로: build/libs/operato-wcs-ai.jar

해결 방법:
/deploy-backend
```

### 애플리케이션 시작 실패

```
❌ 애플리케이션 시작 실패

에러 로그:
[에러 메시지 전체 출력]

일반적인 원인:
1. 데이터베이스 연결 실패 — application.yml 설정 확인
2. RabbitMQ 연결 실패 — RabbitMQ 서버 실행 확인
3. 포트 충돌 — lsof -i :8080 확인
4. 설정 파일 오류 — application.yml 문법 확인
```

### 헬스 체크 실패

```
❌ 서버가 시작되었으나 헬스 체크 실패

가능한 원인:
1. Actuator 미활성화 — build.gradle에 spring-boot-starter-actuator 추가
2. 보안 설정으로 엔드포인트 차단 — SecurityConfig 확인
3. 서버 초기화 미완료 — 1분 후 재시도

수동 확인:
curl http://localhost:8080/actuator/health
```

## 주의사항

1. **포그라운드 실행**
   - 기본적으로 포그라운드에서 실행됨
   - 터미널 종료 시 서버도 함께 종료
   - 백그라운드 실행은 Docker 또는 systemd 사용 권장

2. **개발 vs 운영 환경**
   - 개발: `./gradlew bootRun` 권장 (빠른 재시작)
   - 운영: Docker 또는 JAR 직접 실행 권장 (`/docker-backend`)

3. **메모리 설정**
   - JVM 메모리 옵션은 기본값 사용
   - 필요 시 JAVA_OPTS 환경변수로 조정:
     ```bash
     export JAVA_OPTS="-Xms512m -Xmx2g"
     ./gradlew bootRun
     ```

4. **프로파일 설정**
   - `application.yml`, `application-dev.yml`, `application-prod.yml` 확인
   - 프로파일별로 다른 설정 사용 (DB, RabbitMQ 등)

5. **디버그 모드**
   - 디버그 포트 5005는 기본값
   - 포트 충돌 시 다른 포트 사용:
     ```bash
     -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006
     ```

## 일반적인 워크플로우

### 개발 중 (소스 수정 → 재시작)

```bash
# 1. 코드 수정
# (에디터에서 작업)

# 2. 서버 재시작
# Ctrl+C로 기존 서버 종료
/run-backend

# 3. 테스트
curl http://localhost:8080/api/...
```

### 빌드 후 JAR 실행

```bash
# 1. 백엔드 빌드
/deploy-backend

# 2. JAR 실행
/run-backend --jar --profile=prod

# 3. 헬스 체크
curl http://localhost:8080/actuator/health
```

### 디버깅

```bash
# 1. 디버그 모드로 실행
/run-backend --debug

# 2. IDE에서 디버거 연결
# IntelliJ: Run → Attach to Process → localhost:5005

# 3. 브레이크포인트 설정 및 디버깅
```

## 관련 스킬

- `/deploy-backend` — JAR 파일 빌드 후 실행
- `/docker-backend` — Docker 컨테이너로 실행 (운영 환경)
- `/log` — 실행 결과 로그 기록
- `/commit` — 설정 파일 변경 시 커밋

## 다른 방법

### 백그라운드 실행 (비권장)

```bash
# nohup으로 백그라운드 실행
nohup ./gradlew bootRun > app.log 2>&1 &

# 프로세스 확인
ps aux | grep gradle

# 종료
kill <pid>
```

**주의**: 개발 환경에서는 포그라운드 실행 권장. 운영 환경에서는 Docker 또는 systemd 사용.

## 사용 흐름

### 시나리오 1: 빠른 개발 및 테스트

```bash
# Gradle bootRun으로 실행 (기본)
/run-backend

# 브라우저에서 테스트
# http://localhost:8080
```

### 시나리오 2: 운영 환경 테스트

```bash
# 빌드
/deploy-backend

# JAR 실행 (prod 프로파일)
/run-backend --jar --profile=prod

# 헬스 체크
curl http://localhost:8080/actuator/health
```

### 시나리오 3: 디버깅

```bash
# 디버그 모드로 실행
/run-backend --debug

# IDE에서 디버거 연결 후 디버깅
```
