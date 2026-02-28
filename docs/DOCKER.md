# Docker 배포 가이드

Operato WCS 백엔드를 Docker 컨테이너로 빌드 및 실행하는 방법입니다.

## 사전 요구사항

- Docker 20.10 이상
- Docker Compose 2.0 이상 (선택)

## 빌드 방법

### 방법 1: 멀티스테이지 빌드 (권장)

Docker 내부에서 빌드부터 실행까지 모두 수행합니다.

```bash
# 이미지 빌드
docker build -t operato-wcs-ai:latest .

# 컨테이너 실행
docker run -d \
  --name operato-wcs \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  operato-wcs-ai:latest
```

**장점:**
- 로컬에 Gradle이나 Java 설치 불필요
- 일관된 빌드 환경 보장
- CI/CD 파이프라인에 적합

**단점:**
- 빌드 시간이 더 걸림 (첫 빌드 시 약 1-2분)

### 방법 2: 사전 빌드된 JAR 사용

로컬에서 JAR를 먼저 빌드한 후 Docker 이미지를 생성합니다.

```bash
# 1. 로컬에서 JAR 빌드
./gradlew clean build -x test

# 2. 간단한 Dockerfile로 이미지 빌드
docker build -f Dockerfile.simple -t operato-wcs-ai:latest .

# 3. 컨테이너 실행
docker run -d \
  --name operato-wcs \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  operato-wcs-ai:latest
```

**장점:**
- 빌드 시간이 빠름 (약 10-20초)
- 로컬 개발 환경과 동일한 JAR 사용

**단점:**
- 로컬에 Java 18 및 Gradle 필요

## Docker Compose 사용

가장 간단한 실행 방법입니다.

```bash
# 빌드 및 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f wcs-backend

# 중지
docker-compose down

# 중지 + 볼륨 삭제
docker-compose down -v
```

## 환경 변수 설정

### Spring Boot 프로파일

```bash
docker run -e SPRING_PROFILES_ACTIVE=prod ...
```

### 데이터베이스 연결

```bash
docker run \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/wcs \
  -e SPRING_DATASOURCE_USERNAME=wcs_user \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  ...
```

### Redis 연결

```bash
docker run \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_REDIS_PORT=6379 \
  ...
```

### RabbitMQ 연결

```bash
docker run \
  -e SPRING_RABBITMQ_HOST=rabbitmq \
  -e SPRING_RABBITMQ_PORT=5672 \
  -e SPRING_RABBITMQ_USERNAME=guest \
  -e SPRING_RABBITMQ_PASSWORD=guest \
  ...
```

### JVM 옵션

```bash
docker run \
  -e JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC" \
  ...
```

## 볼륨 마운트

### 로그 디렉토리

```bash
docker run -v ./logs:/app/logs ...
```

### 설정 파일

```bash
docker run -v ./config:/app/config ...
```

## 포트 매핑

```bash
# 기본 포트 (8080)
docker run -p 8080:8080 ...

# 다른 포트로 매핑
docker run -p 9090:8080 ...
```

## 헬스 체크

컨테이너는 자동으로 헬스 체크를 수행합니다.

```bash
# 헬스 상태 확인
docker inspect --format='{{.State.Health.Status}}' operato-wcs

# 헬스 체크 로그 확인
docker inspect --format='{{json .State.Health}}' operato-wcs | jq
```

## 네트워크 구성

### 단독 실행

```bash
docker run --network host ...
```

### 다른 서비스와 연결

```bash
# 네트워크 생성
docker network create wcs-network

# MySQL 실행
docker run -d \
  --name mysql \
  --network wcs-network \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=wcs \
  mysql:8.0

# WCS 실행 (MySQL과 연결)
docker run -d \
  --name operato-wcs \
  --network wcs-network \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/wcs \
  operato-wcs-ai:latest
```

## 로그 확인

```bash
# 실시간 로그
docker logs -f operato-wcs

# 최근 100줄
docker logs --tail 100 operato-wcs

# 특정 시간 이후 로그
docker logs --since 10m operato-wcs
```

## 컨테이너 관리

```bash
# 컨테이너 시작
docker start operato-wcs

# 컨테이너 중지
docker stop operato-wcs

# 컨테이너 재시작
docker restart operato-wcs

# 컨테이너 삭제
docker rm -f operato-wcs

# 이미지 삭제
docker rmi operato-wcs-ai:latest
```

## 성능 최적화

### 메모리 제한

```bash
docker run --memory="1g" --memory-swap="1g" ...
```

### CPU 제한

```bash
docker run --cpus="2.0" ...
```

### 리소스 제한 (docker-compose.yml)

```yaml
services:
  wcs-backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 1G
        reservations:
          cpus: '1.0'
          memory: 512M
```

## 프로덕션 배포 체크리스트

- [ ] 프로파일을 `prod`로 설정
- [ ] 데이터베이스 연결 정보 설정
- [ ] Redis/RabbitMQ 연결 정보 설정
- [ ] JVM 메모리 옵션 조정 (최소 512MB, 권장 1GB)
- [ ] 로그 볼륨 마운트 설정
- [ ] 헬스 체크 확인
- [ ] 방화벽/보안 그룹 설정
- [ ] SSL/TLS 인증서 설정 (리버스 프록시 사용 권장)
- [ ] 자동 재시작 정책 설정 (`--restart=unless-stopped`)

## 문제 해결

### 컨테이너가 시작되지 않음

```bash
# 상세 로그 확인
docker logs operato-wcs

# 컨테이너 상태 확인
docker inspect operato-wcs
```

### 데이터베이스 연결 실패

```bash
# 네트워크 연결 확인
docker network inspect wcs-network

# 데이터베이스 컨테이너 상태 확인
docker exec -it mysql mysql -u root -p
```

### 메모리 부족

```bash
# 메모리 사용량 확인
docker stats operato-wcs

# JVM 힙 크기 증가
docker run -e JAVA_OPTS="-Xmx2g -Xms1g" ...
```

## 이미지 배포

### Docker Hub에 푸시

```bash
# 태그 생성
docker tag operato-wcs-ai:latest your-registry/operato-wcs-ai:v1.0.0

# 푸시
docker push your-registry/operato-wcs-ai:v1.0.0
```

### Private Registry 사용

```bash
# 로그인
docker login your-registry.com

# 태그 및 푸시
docker tag operato-wcs-ai:latest your-registry.com/operato-wcs-ai:latest
docker push your-registry.com/operato-wcs-ai:latest
```

## 참고 자료

- [Spring Boot Docker 공식 가이드](https://spring.io/guides/topicals/spring-boot-docker/)
- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
