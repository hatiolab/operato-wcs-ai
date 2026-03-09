# 개발 vs 운영 환경 배포 가이드

## 개발 환경

- Spring Boot 내장 웹 서버가 API와 정적 파일(Lit 클라이언트)을 함께 서빙
- 정적 파일 경로: `src/main/resources/static/`
- 실행 방법: `./gradlew bootRun`

---

## 운영 환경 (옵션 1 — bootJar 직접 실행)

백엔드 JAR과 프론트엔드를 각각 빌드하여 서버에 직접 배포하는 방식입니다.

**배포 흐름**
```
1. 프론트엔드 빌드  →  client/dist/ 생성
2. 백엔드 빌드      →  build/libs/operato-wcs-ai.jar 생성
3. JAR + 프론트엔드 빌드 결과물을 서버에 업로드
4. Nginx가 정적 파일(Lit 클라이언트) 서빙 및 API 요청 프록시
5. Java로 JAR 직접 실행
```

**실행 명령어**
```bash
# 백엔드 빌드
./gradlew bootJar

# 운영 서버에서 실행
java -jar -Dspring.profiles.active=prod build/libs/operato-wcs-ai.jar
```

**Nginx 역할**
- Lit 클라이언트 정적 파일 서빙 (`/`)
- Spring Boot API 요청 프록시 (`/api/**` → `localhost:8080`)

---

## 운영 환경 (옵션 2 — Docker 배포)

백엔드와 Nginx를 Docker 컨테이너로 패키징하여 배포하는 방식입니다.

**배포 흐름**
```
1. 프론트엔드 빌드  →  client/dist/ 생성
2. Docker 이미지 빌드 (Spring Boot JAR + Nginx 포함)
3. docker-compose로 컨테이너 실행
```

**실행 명령어**
```bash
# Docker 이미지 빌드 및 실행
docker-compose up -d --build

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

**컨테이너 구성** (`docker-compose.yml`)
- `app` 컨테이너: Spring Boot JAR 실행 (포트 8080)
- `nginx` 컨테이너: 정적 파일 서빙 + API 프록시 (포트 80/443)

**관련 파일**
- `docker/Dockerfile` — Spring Boot 앱 이미지 정의
- `docker/nginx/nginx.conf` — Nginx 설정
- `docker-compose.yml` — 컨테이너 오케스트레이션
