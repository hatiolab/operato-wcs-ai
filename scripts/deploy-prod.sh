#!/bin/bash

# Operato WCS Production Deployment Script
# 프론트엔드 + 백엔드 운영 환경 배포 자동화

set -e  # 에러 발생 시 즉시 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 프로젝트 루트 디렉토리로 이동
cd "$(dirname "$0")/.."

log_info "=== Operato WCS 운영 환경 배포 시작 ==="

# Step 1: 프론트엔드 빌드
log_info "Step 1/5: 프론트엔드 빌드 중..."
if [ ! -d "frontend" ]; then
    log_error "frontend 디렉토리가 없습니다."
    exit 1
fi

cd frontend

# 의존성 설치 확인
if [ ! -d "node_modules" ]; then
    log_warn "node_modules가 없습니다. yarn install을 실행합니다..."
    yarn install
fi

# 프론트엔드 빌드
log_info "프론트엔드 클라이언트 빌드 중..."
yarn build:client

# 빌드 결과 확인
if [ ! -d "packages/operato-wcs-ui/dist-client" ]; then
    log_error "프론트엔드 빌드 실패: dist-client 디렉토리가 생성되지 않았습니다."
    exit 1
fi

log_info "프론트엔드 빌드 완료 ✓"
cd ..

# Step 2: 백엔드 빌드
log_info "Step 2/5: 백엔드 빌드 중..."
./gradlew clean build -x test

# JAR 파일 확인
if [ ! -f "build/libs/operato-wcs-ai.jar" ]; then
    log_error "백엔드 빌드 실패: JAR 파일이 생성되지 않았습니다."
    exit 1
fi

log_info "백엔드 빌드 완료 ✓"

# Step 3: 기존 컨테이너 중지
log_info "Step 3/5: 기존 컨테이너 중지 중..."
if docker-compose -f docker-compose.prod.yml ps -q 2>/dev/null | grep -q .; then
    docker-compose -f docker-compose.prod.yml down
    log_info "기존 컨테이너 중지 완료 ✓"
else
    log_info "실행 중인 컨테이너가 없습니다."
fi

# Step 4: Docker 이미지 빌드 및 컨테이너 시작
log_info "Step 4/5: Docker 이미지 빌드 및 컨테이너 시작 중..."
docker-compose -f docker-compose.prod.yml up -d --build

log_info "컨테이너 시작 완료 ✓"

# Step 5: 헬스 체크
log_info "Step 5/5: 서비스 헬스 체크 중..."

# 백엔드 헬스 체크 (최대 60초 대기)
log_info "백엔드 서비스 준비 대기 중..."
for i in {1..12}; do
    if docker exec operato-wcs-backend wget -qO- http://localhost:9190/actuator/health 2>/dev/null | grep -q "UP"; then
        log_info "백엔드 서비스 정상 ✓"
        break
    fi

    if [ $i -eq 12 ]; then
        log_error "백엔드 서비스 시작 실패 (타임아웃)"
        docker-compose -f docker-compose.prod.yml logs wcs-backend
        exit 1
    fi

    echo -n "."
    sleep 5
done

# 프론트엔드 헬스 체크
log_info "프론트엔드 서비스 확인 중..."
sleep 3

if curl -f http://localhost/health >/dev/null 2>&1; then
    log_info "프론트엔드 서비스 정상 ✓"
else
    log_error "프론트엔드 서비스 헬스 체크 실패"
    docker-compose -f docker-compose.prod.yml logs wcs-frontend
    exit 1
fi

# 배포 완료
log_info ""
log_info "=== 배포 완료! ==="
log_info ""
log_info "접속 정보:"
log_info "  - 프론트엔드: http://localhost"
log_info "  - 백엔드 API: http://localhost/rest"
log_info "  - 헬스 체크: http://localhost/health"
log_info ""
log_info "컨테이너 상태:"
docker-compose -f docker-compose.prod.yml ps
log_info ""
log_info "로그 확인: docker-compose -f docker-compose.prod.yml logs -f"
