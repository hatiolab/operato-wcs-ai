---
name: docker-backend
description: Spring Boot 백엔드를 Docker 이미지로 빌드하고 컨테이너로 실행합니다. 멀티스테이지 빌드 또는 사전 빌드된 JAR 사용 옵션을 지원합니다.
disable-model-invocation: false
argument-hint: "[옵션: --simple | --build-only | --compose | --clean]"
---

# 백엔드 Docker 빌드 및 실행

Spring Boot 애플리케이션을 Docker 이미지로 빌드하고 컨테이너로 실행합니다.

## 실행 절차

### 1. 필수 파일 확인

다음 파일들이 존재하는지 확인하고, 없으면 생성합니다:

- `Dockerfile` — 멀티스테이지 빌드용
- `Dockerfile.simple` — 사전 빌드된 JAR 사용
- `.dockerignore` — 불필요한 파일 제외
- `docker-compose.yml` — Docker Compose 설정
- `docs/DOCKER.md` — 상세 사용 가이드

파일이 없으면 표준 템플릿으로 생성합니다.

### 2. 인자 파싱

`$ARGUMENTS`로 전달된 옵션을 파싱:

- `--simple` 또는 `-s`: Dockerfile.simple 사용 (빠른 빌드)
- `--build-only` 또는 `-b`: 이미지 빌드만 수행, 실행 안 함
- `--compose` 또는 `-c`: Docker Compose 사용 (기본값)
- `--clean` 또는 `-cl`: 기존 컨테이너 및 이미지 삭제 후 재빌드
- 인자가 없으면 기본 동작: Docker Compose로 빌드 및 실행

### 3. 빌드 방법 선택

#### 방법 A: Docker Compose (기본값, --compose)

```bash
# 기존 컨테이너 중지 및 삭제 (있는 경우)
docker-compose down

# 빌드 및 실행
docker-compose up -d --build

# 로그 확인 (30초 정도)
docker-compose logs -f wcs-backend
```

**장점:**
- 가장 간단하고 관리하기 쉬움
- 설정 파일로 모든 옵션 관리
- 여러 서비스 통합 관리 가능

#### 방법 B: 멀티스테이지 빌드 (기본 Dockerfile)

```bash
# 이미지 빌드
docker build -t operato-wcs-ai:latest .

# 컨테이너 실행
docker run -d \
  --name operato-wcs-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --restart unless-stopped \
  operato-wcs-ai:latest
```

**장점:**
- Docker 내부에서 빌드, 일관된 환경 보장
- CI/CD 파이프라인에 적합

**단점:**
- 빌드 시간 약 1-2분

#### 방법 C: 사전 빌드된 JAR 사용 (--simple)

```bash
# 1. JAR 파일 존재 확인
if [ ! -f build/libs/operato-wcs-ai.jar ]; then
  echo "JAR 파일이 없습니다. /deploy-backend 스킬로 먼저 빌드하세요."
  exit 1
fi

# 2. Docker 이미지 빌드
docker build -f Dockerfile.simple -t operato-wcs-ai:latest .

# 3. 컨테이너 실행
docker run -d \
  --name operato-wcs-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --restart unless-stopped \
  operato-wcs-ai:latest
```

**장점:**
- 빌드 시간 약 10-20초로 매우 빠름
- 로컬에서 테스트한 JAR와 동일

**단점:**
- 사전에 로컬 빌드 필요 (`/deploy-backend` 스킬 사용)

### 4. 빌드 전 정리 (--clean 옵션)

```bash
# 실행 중인 컨테이너 중지 및 삭제
docker stop operato-wcs-backend 2>/dev/null || true
docker rm operato-wcs-backend 2>/dev/null || true

# 기존 이미지 삭제
docker rmi operato-wcs-ai:latest 2>/dev/null || true

# Docker Compose 사용 시
docker-compose down -v
```

### 5. 빌드 실행 및 검증

#### 이미지 빌드 확인

```bash
# 이미지 목록 확인
docker images | grep operato-wcs-ai

# 이미지 상세 정보
docker inspect operato-wcs-ai:latest
```

#### 컨테이너 실행 확인

```bash
# 컨테이너 상태 확인
docker ps | grep operato-wcs

# 로그 확인 (최근 50줄)
docker logs --tail 50 operato-wcs-backend

# 헬스 체크
curl -f http://localhost:8080/actuator/health || echo "헬스 체크 실패"
```

### 6. 결과 보고

사용자에게 다음 정보를 제공:

```
✅ **Docker 이미지 빌드 성공!**

**생성된 이미지:**
- operato-wcs-ai:latest — 157MB (빌드 방법에 따라 다름)

**실행 중인 컨테이너:**
- operato-wcs-backend — UP (healthy)
- 포트: 8080:8080
- 상태: Running

**접속 정보:**
- 애플리케이션: http://localhost:8080
- 헬스 체크: http://localhost:8080/actuator/health

**로그 확인:**
docker logs -f operato-wcs-backend

**컨테이너 관리:**
docker stop operato-wcs-backend    # 중지
docker start operato-wcs-backend   # 시작
docker restart operato-wcs-backend # 재시작

**Docker Compose 사용 시:**
docker-compose logs -f             # 로그 확인
docker-compose down                # 중지 및 삭제
docker-compose restart             # 재시작
```

## 옵션별 실행 예시

### 기본 실행 (Docker Compose)

```bash
/docker-backend
```

실행 내용:
1. docker-compose.yml 파일 확인
2. `docker-compose down` 실행
3. `docker-compose up -d --build` 실행
4. 로그 확인 및 헬스 체크
5. 결과 보고

### 빠른 빌드 (사전 빌드된 JAR)

```bash
/docker-backend --simple
```

실행 내용:
1. build/libs/operato-wcs-ai.jar 파일 확인
2. Dockerfile.simple로 이미지 빌드
3. 컨테이너 실행
4. 결과 보고

### 빌드만 수행 (실행 안 함)

```bash
/docker-backend --build-only
```

실행 내용:
1. Dockerfile로 이미지 빌드
2. 이미지 정보 출력
3. 실행은 하지 않음

### 전체 재빌드 (정리 후 빌드)

```bash
/docker-backend --clean
```

실행 내용:
1. 기존 컨테이너 중지 및 삭제
2. 기존 이미지 삭제
3. Docker Compose로 재빌드 및 실행
4. 결과 보고

## 에러 처리

### JAR 파일 없음 (--simple 사용 시)

```
❌ JAR 파일이 없습니다.

다음 명령어로 먼저 빌드하세요:
/deploy-backend
```

### 포트 충돌

```
❌ 포트 8080이 이미 사용 중입니다.

기존 프로세스 확인:
lsof -i :8080

또는 다른 포트로 실행:
docker run -p 9090:8080 ...
```

### Docker 데몬 미실행

```
❌ Docker 데몬이 실행되지 않았습니다.

Docker Desktop을 실행하거나 다음 명령어로 시작:
open -a Docker
```

### 빌드 실패

```
❌ Docker 이미지 빌드 실패

에러 로그:
[에러 메시지 전체 출력]

해결 방법:
1. 의존성 문제: --refresh-dependencies 옵션 사용
2. 메모리 부족: Docker 메모리 할당 증가
3. 네트워크 문제: 프록시 설정 확인
```

## 주의사항

1. **사전 빌드 필요 (--simple 사용 시)**
   - `/deploy-backend` 스킬로 먼저 JAR 파일 빌드 필요
   - JAR 파일 경로: `build/libs/operato-wcs-ai.jar`

2. **포트 충돌 확인**
   - 기본 포트 8080이 사용 중이면 실패
   - docker-compose.yml에서 포트 변경 가능

3. **메모리 요구사항**
   - 최소: 512MB
   - 권장: 1GB
   - Docker Desktop 메모리 설정 확인

4. **볼륨 마운트**
   - 로그 디렉토리: `./logs:/app/logs`
   - docker-compose.yml에서 설정

5. **네트워크 설정**
   - 다른 서비스(MySQL, Redis 등)와 연동 시 네트워크 설정 필요
   - docker-compose.yml에서 depends_on 설정

## 프로덕션 배포 체크리스트

빌드 전 확인:
- [ ] SPRING_PROFILES_ACTIVE=prod 설정
- [ ] 데이터베이스 연결 정보 설정
- [ ] Redis/RabbitMQ 연결 정보 설정
- [ ] JVM 메모리 옵션 조정
- [ ] 로그 볼륨 마운트 설정
- [ ] SSL/TLS 설정 (리버스 프록시)
- [ ] 자동 재시작 정책 설정
- [ ] 헬스 체크 엔드포인트 활성화

## 사용 흐름

### 개발 환경

```bash
# 1. 백엔드 빌드
/deploy-backend

# 2. Docker 이미지 빌드 및 실행 (빠른 방법)
/docker-backend --simple

# 3. 로그 확인
docker logs -f operato-wcs-backend
```

### 프로덕션 환경

```bash
# 1. Docker Compose로 빌드 및 실행
/docker-backend

# 2. 헬스 체크
curl http://localhost:8080/actuator/health

# 3. 모니터링
docker stats operato-wcs-backend
```

## 관련 파일

- `Dockerfile` — 멀티스테이지 빌드 정의
- `Dockerfile.simple` — 사전 빌드된 JAR 사용
- `.dockerignore` — Docker 빌드 제외 파일
- `docker-compose.yml` — Docker Compose 설정
- `docs/DOCKER.md` — 상세 사용 가이드

## 다른 스킬과의 연계

- `/deploy-backend` → `/docker-backend --simple`: 빌드 후 Docker 이미지 생성
- `/docker-backend` → `/log`: Docker 빌드 결과 로그 기록
- `/docker-backend` → `/commit`: Docker 설정 파일 커밋

## 향후 확장

다음 스킬 추가 예정:
- `/docker-frontend`: 프론트엔드 Docker 빌드 및 실행
- `/docker-full`: 백엔드 + 프론트엔드 통합 Docker Compose
- `/docker-deploy`: Docker 이미지를 원격 레지스트리에 푸시
