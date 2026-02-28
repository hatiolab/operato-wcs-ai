✅ 백엔드 Docker 컨테이너 생성

생성된 파일

파일	용도
Dockerfile	멀티스테이지 빌드 (Docker 내부에서 빌드)
Dockerfile.simple	사전 빌드된 JAR 사용 (빠른 빌드)
.dockerignore	Docker 이미지에서 제외할 파일 목록
docker-compose.yml	Docker Compose 설정 (간편 실행)
docs/DOCKER.md	상세 사용 가이드

빠른 시작
옵션 1: Docker Compose (가장 간단)

# 빌드 및 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f wcs-backend

# 중지
docker-compose down
옵션 2: Docker CLI (멀티스테이지 빌드)

# 이미지 빌드
docker build -t operato-wcs-ai:latest .

# 컨테이너 실행
docker run -d \
  --name operato-wcs \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  operato-wcs-ai:latest

# 로그 확인
docker logs -f operato-wcs
옵션 3: 사전 빌드된 JAR 사용 (빠름)

# 1. 로컬에서 JAR 빌드 (이미 완료됨)
# ./gradlew clean build -x test

# 2. Docker 이미지 빌드 (10-20초)
docker build -f Dockerfile.simple -t operato-wcs-ai:latest .

# 3. 컨테이너 실행
docker run -d \
  --name operato-wcs \
  -p 8080:8080 \
  operato-wcs-ai:latest
주요 특징
특징	설명
멀티스테이지 빌드	Docker 내부에서 빌드하여 일관된 환경 보장
비루트 사용자	보안을 위해 spring 사용자로 실행
헬스 체크	30초마다 자동 헬스 체크 수행
JVM 최적화	컨테이너 환경에 최적화된 JVM 옵션
경량 이미지	JRE만 포함하여 이미지 크기 최소화
환경 변수 설정 예시

docker run -d \
  --name operato-wcs \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/wcs \
  -e SPRING_DATASOURCE_USERNAME=wcs_user \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_RABBITMQ_HOST=rabbitmq \
  -e JAVA_OPTS="-Xmx1g -Xms512m" \
  -v ./logs:/app/logs \
  --restart=unless-stopped \
  operato-wcs-ai:latest
접속 확인
컨테이너 실행 후:


# 헬스 체크
curl http://localhost:8080/actuator/health

# 상태 확인
docker ps | grep operato-wcs

# 로그 확인
docker logs -f operato-wcs