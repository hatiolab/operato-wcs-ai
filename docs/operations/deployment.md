# Operato WCS 배포 가이드

완전한 개발 및 운영 환경 배포 방법을 설명합니다.

---

## 목차

1. [개발 환경](#개발-환경)
2. [운영 환경 옵션 1 — JAR + Nginx 직접 실행](#운영-환경-옵션-1--jar--nginx-직접-실행)
3. [운영 환경 옵션 2 — Docker 배포 (권장)](#운영-환경-옵션-2--docker-배포-권장)
   - [배포 전 준비사항](#배포-전-준비사항)
   - [배포 방법](#배포-방법)
   - [배포 확인](#배포-확인)
   - [운영 관리](#운영-관리)
   - [커스터마이징](#커스터마이징)
   - [트러블슈팅](#트러블슈팅)

---

## 개발 환경

개발 환경에서는 **백엔드와 프론트엔드를 별도로 실행**하여 개발합니다.

### 아키텍처

```
┌─────────────────────────────────────┐
│   프론트엔드 개발 서버               │
│   Things Factory (포트 5908)        │
│   - HMR (Hot Module Replacement)    │
│   - 실시간 리로드                    │
└───────────┬─────────────────────────┘
            │ API 요청: /rest/**
            ▼
┌─────────────────────────────────────┐
│   백엔드 개발 서버                   │
│   Spring Boot (포트 9190)           │
│   - REST API 제공                   │
└─────────────────────────────────────┘
```

### 백엔드 실행

```bash
# 방법 1: Gradle bootRun
./gradlew bootRun

# 방법 2: 스킬 사용
/run-backend

# 접속 URL
# - API: http://localhost:9190/rest
# - Actuator: http://localhost:9190/actuator/health
```

### 프론트엔드 실행

```bash
# 프론트엔드 디렉토리로 이동
cd frontend

# 의존성 설치 (처음 한 번만)
yarn install

# 개발 서버 실행
yarn wcs:dev

# 접속 URL
# - UI: http://localhost:5908
```

### 환경 설정

**프론트엔드 → 백엔드 API 연결**

`frontend/packages/operato-wcs-ui/config/config.development.js`:
```javascript
module.exports = {
  subdomain: "logisid",
  port: 5908,
  operato: {
    baseUrl: 'http://localhost:9190/rest',  // 백엔드 API URL
  }
}
```

### 개발 워크플로우

1. **백엔드 실행** (포트 9190)
   ```bash
   ./gradlew bootRun
   ```

2. **프론트엔드 실행** (포트 5908)
   ```bash
   cd frontend
   yarn wcs:dev
   ```

3. **브라우저 접속**
   - http://localhost:5908

4. **코드 수정 시**
   - 프론트엔드: 자동 리로드 (HMR)
   - 백엔드: 서버 재시작 필요 (또는 Spring DevTools 사용)

### 개발 환경의 장점

- ✅ 프론트엔드 HMR (Hot Module Replacement) 지원
- ✅ 백엔드/프론트엔드 독립적 개발
- ✅ 빠른 개발 사이클
- ✅ 실시간 코드 변경 반영

---

## 운영 환경 (옵션 1 — JAR + Nginx 직접 실행)

백엔드 JAR과 프론트엔드를 각각 빌드하여 서버에 직접 배포하는 방식입니다.

### 배포 흐름

```
1. 프론트엔드 빌드  →  frontend/packages/operato-wcs-ui/dist-client/ 생성
2. 백엔드 빌드      →  build/libs/operato-wcs-ai.jar 생성
3. JAR + 프론트엔드 빌드 결과물을 서버에 업로드
4. Nginx가 정적 파일(Things Factory 클라이언트) 서빙 및 API 요청 프록시
5. Java로 JAR 직접 실행
```

### 실행 명령어

#### 방법 1: Gradle 통합 빌드 (권장)

```bash
# 프론트엔드 + 백엔드 전체 빌드
./gradlew buildAll

# 운영 서버에서 백엔드 실행
java -jar -Dspring.profiles.active=prod build/libs/operato-wcs-ai.jar

# Nginx 설정 및 실행 (별도)
# - dist-client 디렉토리를 Nginx 루트로 설정
# - API 프록시 설정
```

#### 방법 2: 개별 빌드

```bash
# 1. 프론트엔드 빌드 (Gradle 사용)
./gradlew lernaBootstrap buildFrontend

# 또는 Yarn으로 직접 빌드
cd frontend
yarn build:client
cd ..

# 2. 백엔드만 빌드 (프론트엔드 스킵)
SKIP_FRONTEND=true ./gradlew build

# 또는
./gradlew bootJar

# 3. 운영 서버에서 백엔드 실행
java -jar -Dspring.profiles.active=prod build/libs/operato-wcs-ai.jar
```

### Nginx 역할

- Things Factory 클라이언트 정적 파일 서빙 (`/`)
- Spring Boot API 요청 프록시 (`/rest/**` → `localhost:9190`)

---

## 운영 환경 (옵션 2 — Docker 배포) (권장)

백엔드와 Nginx를 Docker 컨테이너로 패키징하여 배포하는 방식입니다.

### 배포 아키텍처

```
외부 접속 (포트 80/443)
         │
         ▼
┌─────────────────────────────────────────┐
│   Nginx Container (wcs-frontend)        │
│   - 프론트엔드 정적 파일 서빙            │
│   - API 요청 프록시                      │
└─────────────┬───────────────────────────┘
              │ /rest/** → :9190
              ▼
┌─────────────────────────────────────────┐
│   Backend Container (wcs-backend)       │
│   - Spring Boot API (:9190)             │
└─────────────────────────────────────────┘
```

### 배포 전 준비사항

#### 방법 1: Gradle 통합 빌드 (권장)

```bash
# 프론트엔드 + 백엔드 한 번에 빌드
./gradlew clean buildAll -x test
```

빌드 결과물:
- 프론트엔드: `frontend/packages/operato-wcs-ui/dist-client/`
- 백엔드: `build/libs/operato-wcs-ai.jar`

#### 방법 2: 개별 빌드

**프론트엔드 빌드**:

```bash
# Gradle 사용
./gradlew lernaBootstrap buildFrontend

# 또는 Yarn으로 직접 빌드
cd frontend
yarn install  # 처음 한 번만
yarn build:client
cd ..
```

빌드 결과물: `frontend/packages/operato-wcs-ui/dist-client/`

**백엔드 빌드** (프론트엔드 스킵):

```bash
# 환경 변수로 프론트엔드 스킵
SKIP_FRONTEND=true ./gradlew clean build -x test

# 또는 bootJar 사용 (더 빠름)
./gradlew clean bootJar -x test
```

빌드 결과물: `build/libs/operato-wcs-ai.jar`

#### 3. 필수 파일 확인

- [ ] `docker-compose.prod.yml` — 운영 환경 Docker Compose 설정
- [ ] `docker/nginx/nginx.conf` — Nginx 설정
- [ ] `Dockerfile` — 백엔드 이미지 빌드 파일
- [ ] `frontend/packages/operato-wcs-ui/dist-client/` — 프론트엔드 빌드 결과물

### 배포 방법

#### 방법 1: 자동 배포 스크립트 (권장)

```bash
# 프론트엔드 + 백엔드 자동 빌드 및 배포
./scripts/deploy-prod.sh
```

스크립트가 자동으로 수행하는 작업:
1. 프론트엔드 빌드
2. 백엔드 빌드
3. 기존 컨테이너 중지
4. Docker 이미지 빌드 및 컨테이너 시작
5. 헬스 체크

#### 방법 2: Docker Compose 직접 실행

```bash
# 1. 프론트엔드 빌드
cd frontend
yarn build:client
cd ..

# 2. 백엔드 + 프론트엔드 컨테이너 실행
docker-compose -f docker-compose.prod.yml up -d --build

# 3. 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 4. 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

#### 방법 3: 단계별 배포

```bash
# Step 1: 백엔드 컨테이너 실행
docker-compose -f docker-compose.prod.yml up -d wcs-backend

# 헬스 체크 (백엔드가 준비될 때까지 대기)
until curl -f http://localhost:9190/actuator/health; do
  echo "Waiting for backend..."
  sleep 5
done

# Step 2: 프론트엔드 컨테이너 실행
docker-compose -f docker-compose.prod.yml up -d wcs-frontend
```

### 배포 확인

#### 1. 컨테이너 상태 확인

```bash
# 모든 컨테이너 확인
docker-compose -f docker-compose.prod.yml ps

# 출력 예시:
# NAME                      STATUS              PORTS
# operato-wcs-backend       Up 2 minutes        9190/tcp
# operato-wcs-frontend      Up 1 minute         0.0.0.0:80->80/tcp
```

#### 2. 헬스 체크

```bash
# Nginx 헬스 체크
curl http://localhost/health

# 백엔드 헬스 체크 (Nginx 경유)
curl http://localhost/actuator/health

# 백엔드 직접 헬스 체크
docker exec operato-wcs-backend wget -qO- http://localhost:9190/actuator/health
```

#### 3. 프론트엔드 접속 테스트

브라우저에서 접속:
- **메인 페이지**: http://localhost
- **API 테스트**: http://localhost/rest/...

#### 4. 로그 확인

```bash
# 프론트엔드 (Nginx) 로그
docker-compose -f docker-compose.prod.yml logs -f wcs-frontend

# 백엔드 로그
docker-compose -f docker-compose.prod.yml logs -f wcs-backend

# 전체 로그
docker-compose -f docker-compose.prod.yml logs -f

# Nginx 접근 로그 (실시간)
tail -f logs/nginx/access.log

# Nginx 에러 로그
tail -f logs/nginx/error.log
```

### 운영 관리

#### 컨테이너 관리

```bash
# 전체 시작
docker-compose -f docker-compose.prod.yml start

# 전체 중지
docker-compose -f docker-compose.prod.yml stop

# 전체 재시작
docker-compose -f docker-compose.prod.yml restart

# 특정 서비스만 재시작
docker-compose -f docker-compose.prod.yml restart wcs-frontend
docker-compose -f docker-compose.prod.yml restart wcs-backend

# 전체 중지 및 삭제
docker-compose -f docker-compose.prod.yml down

# 전체 중지 + 볼륨 삭제
docker-compose -f docker-compose.prod.yml down -v
```

#### 프론트엔드 업데이트

```bash
# 1. 프론트엔드 재빌드
# Gradle 사용 (권장)
./gradlew lernaBootstrap buildFrontend

# 또는 Yarn 직접 사용
cd frontend && yarn build:client && cd ..

# 2. Nginx 컨테이너 재시작 (빌드 결과물 볼륨 마운트 갱신)
docker-compose -f docker-compose.prod.yml restart wcs-frontend

# 또는 컨테이너 재생성
docker-compose -f docker-compose.prod.yml up -d wcs-frontend
```

#### 백엔드 업데이트

```bash
# 1. 백엔드 재빌드 (프론트엔드 스킵)
SKIP_FRONTEND=true ./gradlew clean build -x test

# 또는 bootJar 사용
./gradlew clean bootJar -x test

# 2. 백엔드 이미지 재빌드 및 컨테이너 재시작
docker-compose -f docker-compose.prod.yml up -d --build wcs-backend
```

#### 무중단 배포 (Blue-Green)

```bash
# 1. 새 버전 이미지 빌드
docker-compose -f docker-compose.prod.yml build

# 2. 헬스 체크 준비 상태 확인
docker-compose -f docker-compose.prod.yml up -d wcs-backend

# 3. 백엔드 정상 동작 확인
curl http://localhost:9190/actuator/health

# 4. Nginx 재시작 (트래픽 전환)
docker-compose -f docker-compose.prod.yml restart wcs-frontend
```

#### 리소스 모니터링

```bash
# 실시간 모니터링
docker stats operato-wcs-backend operato-wcs-frontend

# CPU/메모리 사용량
docker stats --no-stream
```

### 커스터마이징

#### 포트 변경

`docker-compose.prod.yml` 수정:

```yaml
wcs-frontend:
  ports:
    - "5908:80"  # 외부 포트를 5908으로 변경
```

#### HTTPS 활성화

**1. SSL 인증서 준비**

```bash
# Let's Encrypt 인증서 예시
mkdir -p docker/nginx/certs
# certbot으로 인증서 발급 또는 기존 인증서 복사
```

**2. docker-compose.prod.yml 수정**

```yaml
wcs-frontend:
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
    - ./frontend/packages/operato-wcs-ui/dist-client:/usr/share/nginx/html:ro
    - ./docker/nginx/certs:/etc/nginx/certs:ro  # 추가
```

**3. nginx.conf에 HTTPS 설정 추가**

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/certs/fullchain.pem;
    ssl_certificate_key /etc/nginx/certs/privkey.pem;

    # ... 기존 설정 ...
}

# HTTP → HTTPS 리다이렉트
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

#### API 엔드포인트 변경

`docker/nginx/nginx.conf`에서 프록시 경로 수정:

```nginx
# /api/** 대신 /rest/** 사용 예시
location /api/ {
    proxy_pass http://wcs_backend/rest/;  # 백엔드는 /rest
    # ... 설정 ...
}
```

### 트러블슈팅

#### 1. 프론트엔드가 로드되지 않음

**증상**: 브라우저에서 빈 화면 또는 404 에러

**해결 방법**:
```bash
# 프론트엔드 빌드 확인
ls -la frontend/packages/operato-wcs-ui/dist-client/

# Nginx 컨테이너 내부 파일 확인
docker exec operato-wcs-frontend ls -la /usr/share/nginx/html/

# Nginx 에러 로그 확인
docker logs operato-wcs-frontend
tail -f logs/nginx/error.log
```

#### 2. API 요청 실패 (502 Bad Gateway)

**증상**: API 요청 시 502 에러

**해결 방법**:
```bash
# 백엔드 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps wcs-backend

# 백엔드 헬스 체크
docker exec operato-wcs-backend wget -qO- http://localhost:9190/actuator/health

# 네트워크 연결 확인
docker exec operato-wcs-frontend ping -c 3 wcs-backend

# 백엔드 로그 확인
docker logs operato-wcs-backend
```

#### 3. CORS 에러

**증상**: 브라우저 콘솔에 CORS 에러

**해결 방법**:
- Nginx가 프록시 역할을 하므로 CORS 문제가 발생하지 않아야 합니다
- 백엔드 직접 접속 시 CORS 발생 가능 → Nginx 경유하도록 수정

#### 4. 정적 파일 캐시 문제

**증상**: 프론트엔드 업데이트 후 이전 버전이 로드됨

**해결 방법**:
```bash
# 브라우저 캐시 강제 새로고침: Ctrl+Shift+R (또는 Cmd+Shift+R)

# Nginx 캐시 삭제
docker exec operato-wcs-frontend sh -c "rm -rf /var/cache/nginx/*"
docker-compose -f docker-compose.prod.yml restart wcs-frontend
```

#### 5. 포트 충돌

**증상**: "port is already allocated" 에러

**해결 방법**:
```bash
# 포트 사용 확인
lsof -i :80
lsof -i :9190

# 기존 프로세스 종료 또는 포트 변경
```

### 보안 설정

#### 1. 방화벽 설정

```bash
# 필요한 포트만 오픈
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw deny 9190/tcp   # 백엔드 포트는 외부 차단
```

#### 2. Nginx 보안 헤더 추가

`docker/nginx/nginx.conf`에 추가:

```nginx
server {
    # ... 기존 설정 ...

    # 보안 헤더
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
}
```

### 백업 및 복구

#### 설정 파일 백업

```bash
# 백업
tar -czf wcs-config-$(date +%Y%m%d).tar.gz \
    docker-compose.prod.yml \
    docker/nginx/nginx.conf \
    frontend/packages/operato-wcs-ui/config/

# 복구
tar -xzf wcs-config-YYYYMMDD.tar.gz
```

---

## 컨테이너 구성

**`docker-compose.prod.yml`**
- `wcs-backend` 컨테이너: Spring Boot JAR 실행 (포트 9190, 내부 전용)
- `wcs-frontend` 컨테이너: Nginx — 정적 파일 서빙 + API 프록시 (포트 80/443)

## 관련 파일

| 파일 | 용도 |
|------|------|
| `Dockerfile` | Spring Boot 앱 이미지 정의 |
| `docker/nginx/nginx.conf` | Nginx 설정 |
| `docker-compose.prod.yml` | 운영 환경 컨테이너 오케스트레이션 |
| `scripts/deploy-prod.sh` | 자동 배포 스크립트 |

## 관련 문서

- [backend-docker.md](backend-docker.md) — 백엔드 Docker 배포 상세 가이드
- [../architecture/frontend-structure.md](../architecture/frontend-structure.md) — 프론트엔드 아키텍처
- [../architecture/backend-architecture.md](../architecture/backend-architecture.md) — 백엔드 아키텍처

---

**작성일**: 2026-03-11
**버전**: 2.0
**작성자**: Claude Sonnet 4.5
