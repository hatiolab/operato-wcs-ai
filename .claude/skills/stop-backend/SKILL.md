# 백엔드 서버 중지

실행 중인 Spring Boot 백엔드 서버를 안전하게 중지합니다.

## 실행 절차

### 1. 서버 프로세스 확인

프로파일에 따른 포트를 확인하고 실행 중인 프로세스를 찾습니다.

```bash
# 프로파일별 포트 확인
PROFILE=${PROFILE:-default}

if [ "$PROFILE" != "default" ] && [ -f "src/main/resources/application-${PROFILE}.properties" ]; then
    PORT=$(grep "^server.port=" src/main/resources/application-${PROFILE}.properties | cut -d'=' -f2)
fi

if [ -z "$PORT" ]; then
    PORT=$(grep "^server.port=" src/main/resources/application.properties | cut -d'=' -f2)
fi

PORT=${PORT:-8080}

# 해당 포트에서 실행 중인 프로세스 확인
lsof -i :${PORT}
```

### 2. 인자 파싱

``로 전달된 옵션을 파싱:

- `--port=<port>` 또는 `-p <port>`: 특정 포트의 서버 중지
- `--all` 또는 `-a`: 모든 관련 프로세스 중지 (Gradle daemon 포함)
- 인자가 없으면 기본 동작: 프로파일 기반 포트(기본 9500)에서 실행 중인 서버 중지

### 3. 서버 중지

```bash
# 포트에서 실행 중인 프로세스 PID 조회
PIDS=$(lsof -i :${PORT} -t)

if [ -z "$PIDS" ]; then
    echo "포트 ${PORT}에서 실행 중인 서버가 없습니다."
    exit 0
fi

# 프로세스 종료 (SIGTERM - 정상 종료)
kill $PIDS

# 종료 확인 (최대 10초 대기)
for i in $(seq 1 10); do
    if ! lsof -i :${PORT} -t > /dev/null 2>&1; then
        break
    fi
    sleep 1
done

# 여전히 실행 중이면 강제 종료
if lsof -i :${PORT} -t > /dev/null 2>&1; then
    kill -9 $(lsof -i :${PORT} -t)
fi
```

### 4. --all 옵션 (Gradle daemon 포함 중지)

```bash
# Gradle daemon도 함께 중지
./gradlew --stop
```

### 5. 결과 보고

```
✅ **백엔드 서버 중지 완료!**

**중지된 프로세스:**
- PID: <프로세스 ID>
- 포트: <포트 번호>

**포트 상태:**
- 포트 <포트 번호> 해제 완료 ✅
```

### 서버가 없는 경우

```
ℹ️ **실행 중인 백엔드 서버가 없습니다.**

- 포트: <포트 번호>
- 상태: 프로세스 없음
```

## 사용 예시

```bash
# 기본 중지 (포트 9500)
/stop-backend

# 특정 포트 서버 중지
/stop-backend --port=8080

# 모든 관련 프로세스 중지 (Gradle daemon 포함)
/stop-backend --all
```

## 에러 처리

### 권한 부족

```
❌ 프로세스 종료 권한이 없습니다.

해결 방법:
sudo kill <PID>
```

### 강제 종료 필요

정상 종료(SIGTERM)가 10초 내에 완료되지 않으면 자동으로 강제 종료(SIGKILL)를 수행합니다.

## 관련 스킬

- `/run-backend` — 백엔드 서버 실행
- `/deploy-backend` — 백엔드 빌드 및 JAR 생성
- `/docker-backend` — Docker 컨테이너로 실행
