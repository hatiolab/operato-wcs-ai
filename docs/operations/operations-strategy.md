# Operato WCS 운영 전략 및 방안

## 문서 정보

- **프로젝트**: Operato WCS (Warehouse Control System)
- **버전**: 1.0.0
- **작성일**: 2026-03-01
- **작성자**: Claude Sonnet 4.5
- **대상**: DevOps 엔지니어, 시스템 운영자, 현장 관리자

---

## 목차

1. [운영 전략 개요](#1-운영-전략-개요)
2. [배포 전략](#2-배포-전략)
3. [모니터링 및 알림](#3-모니터링-및-알림)
4. [백업 및 복구](#4-백업-및-복구)
5. [보안 관리](#5-보안-관리)
6. [성능 최적화](#6-성능-최적화)
7. [장애 대응](#7-장애-대응)
8. [일상 운영](#8-일상-운영)
9. [스케일링 전략](#9-스케일링-전략)
10. [운영 자동화](#10-운영-자동화)

---

## 1. 운영 전략 개요

### 1.1 운영 목표

| 목표 | 목표치 | 측정 지표 |
|------|--------|----------|
| **가용성** | 99.9% | Uptime |
| **성능** | 10,000건/시 주문 처리 | TPS |
| **응답 시간** | 평균 100ms 이하 | API Response Time |
| **장애 복구** | 1시간 이내 | MTTR (Mean Time To Recovery) |
| **데이터 손실** | 0건 | RPO (Recovery Point Objective) |

### 1.2 운영 원칙

1. **무중단 서비스**: Blue-Green 배포로 서비스 중단 최소화
2. **사전 예방**: 프로액티브 모니터링으로 장애 사전 감지
3. **신속한 대응**: 자동화된 알림 및 Runbook 기반 대응
4. **지속적 개선**: 사후 분석(Post-mortem) 및 개선 조치
5. **문서화**: 모든 운영 절차 및 장애 이력 문서화

### 1.3 운영 조직

| 역할 | 인원 | 책임 |
|------|------|------|
| DevOps Lead | 1명 | 운영 전략, 인프라 설계 |
| DevOps Engineer | 2명 | 배포, 모니터링, 자동화 |
| DB Administrator | 1명 | 데이터베이스 관리, 백업 |
| Security Engineer | 1명 | 보안 모니터링, 취약점 관리 |
| On-call Engineer | 순환 | 24/7 장애 대응 (교대제) |

---

## 2. 배포 전략

### 2.1 배포 방식 비교

| 방식 | 장점 | 단점 | 권장 용도 |
|------|------|------|----------|
| **Blue-Green** | 무중단, 빠른 롤백 | 리소스 2배 필요 | **운영 환경 (권장)** |
| **Canary** | 점진적 배포, 위험 최소화 | 복잡한 설정 | 대규모 업데이트 |
| **Rolling** | 리소스 효율적 | 버전 혼재 가능 | 개발/스테이징 |

### 2.2 Blue-Green 배포 절차

#### 사전 준비
```bash
# 1. 배포 체크리스트 확인
- [ ] 코드 리뷰 완료
- [ ] 테스트 통과 (Unit, Integration, E2E)
- [ ] 스테이징 환경 검증 완료
- [ ] 데이터베이스 마이그레이션 스크립트 준비
- [ ] 롤백 계획 수립
- [ ] 운영팀 배포 공지 (최소 1일 전)
```

#### 배포 단계

**Step 1: Green 환경 준비**
```bash
# Green 환경에 새 버전 배포
docker pull operato-wcs:1.2.0
docker-compose -f docker-compose.green.yml up -d

# 헬스 체크 (최소 5분 모니터링)
watch -n 5 'curl -f http://green.internal:9500/actuator/health'
```

**Step 2: 스모크 테스트**
```bash
# Green 환경 스모크 테스트
./scripts/smoke-test.sh green.internal:9500

# 주요 API 엔드포인트 검증
curl -X POST http://green.internal:9500/api/orders/receive
curl -X GET http://green.internal:9500/api/batches
```

**Step 3: 트래픽 전환 (Nginx)**
```bash
# Nginx 설정 변경 (Blue → Green)
sudo vi /etc/nginx/conf.d/wcs.conf

# upstream backend {
#     server blue.internal:9500;   # 기존
#     server green.internal:9500;  # 신규
# }

# Nginx 설정 테스트
sudo nginx -t

# Nginx 리로드 (무중단)
sudo nginx -s reload
```

**Step 4: 모니터링 (10분)**
```bash
# Green 환경 모니터링
- 에러 로그 확인
- CPU/메모리 사용률
- API 응답 시간
- 데이터베이스 커넥션 풀
```

**Step 5: Blue 환경 종료**
```bash
# 문제 없으면 Blue 환경 종료
docker-compose -f docker-compose.blue.yml down

# 다음 배포를 위해 Blue를 대기 상태로 유지
```

#### 롤백 절차 (문제 발생 시)

```bash
# 1. 즉시 트래픽을 Blue로 복구
sudo vi /etc/nginx/conf.d/wcs.conf
# upstream을 blue.internal:9500으로 변경
sudo nginx -s reload

# 2. Green 환경 로그 수집
docker logs operato-wcs-green > rollback-$(date +%Y%m%d_%H%M%S).log

# 3. Green 환경 종료
docker-compose -f docker-compose.green.yml down

# 4. 사후 분석 (Post-mortem)
```

### 2.3 배포 자동화 (GitHub Actions)

```yaml
# .github/workflows/deploy-production.yml
name: Deploy to Production

on:
  push:
    tags:
      - 'v*.*.*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Build JAR
        run: ./gradlew clean build -x test

      - name: Build Docker image
        run: docker build -t operato-wcs:${{ github.ref_name }} .

      - name: Push to registry
        run: docker push operato-wcs:${{ github.ref_name }}

      - name: Deploy to Green
        run: |
          ssh deploy@prod-server "
            docker pull operato-wcs:${{ github.ref_name }}
            docker-compose -f docker-compose.green.yml up -d
          "

      - name: Health check
        run: ./scripts/health-check.sh green.internal:9500

      - name: Switch traffic (manual approval)
        uses: trstringer/manual-approval@v1
        with:
          approvers: devops-team

      - name: Update Nginx
        run: ssh deploy@prod-server "./scripts/switch-to-green.sh"
```

---

## 3. 모니터링 및 알림

### 3.1 모니터링 아키텍처

```
┌─────────────────────────────────────────────────┐
│         Operato WCS Application                 │
│  ┌──────────────┐  ┌──────────────┐            │
│  │ Spring Boot  │  │  RabbitMQ    │            │
│  │  Actuator    │  │  Metrics     │            │
│  └──────┬───────┘  └──────┬───────┘            │
│         │ metrics          │ metrics            │
└─────────┼──────────────────┼────────────────────┘
          │                  │
          ▼                  ▼
   ┌─────────────────────────────┐
   │      Prometheus              │ ← 메트릭 수집
   │  (Time-series DB)            │
   └──────────┬──────────────────┘
              │
              ▼
   ┌─────────────────────────────┐
   │       Grafana                │ ← 시각화 대시보드
   │  (Visualization)             │
   └──────────┬──────────────────┘
              │
              ▼
   ┌─────────────────────────────┐
   │    AlertManager              │ ← 알림 발송
   │  (Slack, Email, PagerDuty)   │
   └─────────────────────────────┘
```

### 3.2 모니터링 지표

#### 3.2.1 애플리케이션 메트릭

| 지표 | 설명 | 임계값 | 알림 |
|------|------|--------|------|
| **HTTP Request Rate** | 초당 요청 수 | - | Info |
| **HTTP Error Rate** | 4xx/5xx 에러 비율 | > 5% | Critical |
| **Response Time (P95)** | 응답 시간 95 백분위수 | > 200ms | Warning |
| **Response Time (P99)** | 응답 시간 99 백분위수 | > 500ms | Critical |
| **JVM Heap Usage** | JVM 힙 메모리 사용률 | > 80% | Warning |
| **JVM GC Time** | GC 소요 시간 | > 1초 | Warning |
| **DB Connection Pool** | 데이터베이스 커넥션 사용률 | > 90% | Critical |
| **Thread Pool** | 스레드 풀 사용률 | > 90% | Warning |

#### 3.2.2 비즈니스 메트릭

| 지표 | 설명 | 임계값 | 알림 |
|------|------|--------|------|
| **작업 배치 처리량** | 시간당 처리 배치 수 | < 100 | Warning |
| **주문 처리 성공률** | 주문 처리 성공 비율 | < 95% | Critical |
| **재고 동기화 지연** | 재고 업데이트 지연 시간 | > 10초 | Warning |
| **설비 가동률** | 설비 가동 시간 비율 | < 80% | Info |
| **평균 UPH** | 시간당 분류 PCS | < 800 | Warning |

#### 3.2.3 인프라 메트릭

| 지표 | 설명 | 임계값 | 알림 |
|------|------|--------|------|
| **CPU Usage** | CPU 사용률 | > 80% | Warning |
| **Memory Usage** | 메모리 사용률 | > 85% | Warning |
| **Disk I/O** | 디스크 I/O 대기 시간 | > 100ms | Warning |
| **Network Traffic** | 네트워크 대역폭 사용률 | > 80% | Info |
| **Container Restart** | 컨테이너 재시작 횟수 | > 3회/시간 | Critical |

### 3.3 Grafana 대시보드 구성

#### Dashboard 1: System Overview
```
┌─────────────────────────────────────────────────────┐
│  Operato WCS - System Overview                      │
├──────────────┬──────────────┬──────────────────────┤
│ Uptime       │ Request/sec  │ Error Rate           │
│ 99.95%       │ 245          │ 0.12%                │
├──────────────┴──────────────┴──────────────────────┤
│  Request Rate (24h)                                 │
│  [그래프: 시간대별 요청 수]                           │
├─────────────────────────────────────────────────────┤
│  Response Time (P95/P99)                            │
│  [그래프: 응답 시간 추이]                             │
├─────────────────────────────────────────────────────┤
│  Top 10 Slow APIs                                   │
│  [테이블: 가장 느린 API 엔드포인트]                    │
└─────────────────────────────────────────────────────┘
```

#### Dashboard 2: Business Metrics
```
┌─────────────────────────────────────────────────────┐
│  Operato WCS - Business Metrics                     │
├──────────────┬──────────────┬──────────────────────┤
│ 처리 배치    │ 처리 주문     │ 평균 UPH             │
│ 1,234        │ 8,567        │ 1,245                │
├──────────────┴──────────────┴──────────────────────┤
│  시간대별 배치 처리량                                │
│  [그래프: 배치 처리 추이]                             │
├─────────────────────────────────────────────────────┤
│  설비별 가동률                                       │
│  [히트맵: 설비 가동 상태]                             │
├─────────────────────────────────────────────────────┤
│  주문 상태 분포                                      │
│  [파이 차트: wait/picking/picked/boxed/cancel]       │
└─────────────────────────────────────────────────────┘
```

### 3.4 알림 규칙

#### 알림 채널별 라우팅

| 심각도 | 채널 | 대상 | 대응 시간 |
|--------|------|------|----------|
| **Critical** | PagerDuty + Slack | On-call Engineer | 즉시 (5분 이내) |
| **Warning** | Slack | DevOps Team | 30분 이내 |
| **Info** | Slack (요약) | DevOps Team | 당일 확인 |

#### AlertManager 설정 예시

```yaml
# alertmanager.yml
groups:
  - name: wcs_alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} (> 5%)"

      - alert: HighResponseTime
        expr: histogram_quantile(0.95, http_request_duration_seconds_bucket) > 0.2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time (P95)"
          description: "P95 response time is {{ $value }}s"

      - alert: DatabaseConnectionPoolExhaustion
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool nearly exhausted"
          description: "Pool usage is {{ $value | humanizePercentage }}"
```

---

## 4. 백업 및 복구

### 4.1 백업 전략

#### 4.1.1 백업 유형 및 주기

| 백업 유형 | 대상 | 주기 | 보관 기간 | 저장 위치 |
|----------|------|------|----------|----------|
| **Full Backup** | PostgreSQL DB | 매일 00:00 | 30일 | AWS S3 / NAS |
| **Incremental** | PostgreSQL WAL | 매 10분 | 7일 | AWS S3 |
| **Config Backup** | 설정 파일 | 배포 시 | 90일 | Git + S3 |
| **Application Backup** | Docker Image | 릴리즈 시 | 영구 | Docker Registry |
| **Log Backup** | 애플리케이션 로그 | 매일 | 90일 | AWS S3 / ELK |

#### 4.1.2 백업 자동화 스크립트

**PostgreSQL 전체 백업**
```bash
#!/bin/bash
# scripts/backup-db.sh

BACKUP_DIR="/backups/postgresql"
DB_NAME="operato_wcs"
DB_USER="wcs_admin"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${DATE}.sql.gz"
S3_BUCKET="s3://operato-wcs-backups/database"

# 백업 실행
echo "[$(date)] Starting database backup..."
PGPASSWORD=$DB_PASSWORD pg_dump -h localhost -U $DB_USER -d $DB_NAME | gzip > $BACKUP_FILE

# 백업 검증
if [ $? -eq 0 ]; then
    echo "[$(date)] Backup completed: $BACKUP_FILE"

    # S3 업로드
    aws s3 cp $BACKUP_FILE $S3_BUCKET/

    # 30일 이상 오래된 로컬 백업 삭제
    find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

    echo "[$(date)] Backup uploaded to S3"
else
    echo "[$(date)] Backup failed!"
    exit 1
fi
```

**Cron 등록**
```bash
# /etc/cron.d/wcs-backup
# PostgreSQL 전체 백업 (매일 00:00)
0 0 * * * root /opt/operato-wcs/scripts/backup-db.sh >> /var/log/wcs-backup.log 2>&1

# WAL 아카이빙 (매 10분)
*/10 * * * * root /opt/operato-wcs/scripts/archive-wal.sh >> /var/log/wcs-wal.log 2>&1

# 설정 파일 백업 (매일 01:00)
0 1 * * * root /opt/operato-wcs/scripts/backup-config.sh >> /var/log/wcs-config-backup.log 2>&1
```

### 4.2 복구 절차

#### 4.2.1 데이터베이스 복구 (Point-in-Time Recovery)

**시나리오**: 2026-03-15 14:30에 발생한 데이터 손상을 14:00 시점으로 복구

```bash
# 1. PostgreSQL 서비스 중지
sudo systemctl stop postgresql

# 2. 기존 데이터 디렉토리 백업
sudo mv /var/lib/postgresql/14/main /var/lib/postgresql/14/main.corrupted

# 3. 최근 Full Backup 복원 (2026-03-15 00:00)
sudo -u postgres mkdir /var/lib/postgresql/14/main
sudo -u postgres pg_restore -d operato_wcs /backups/postgresql/operato_wcs_20260315_000000.sql.gz

# 4. WAL 아카이브 적용 (00:00 → 14:00)
sudo -u postgres cp /backups/wal/000000010000000000000001 /var/lib/postgresql/14/main/pg_wal/

# 5. recovery.conf 설정
sudo -u postgres cat > /var/lib/postgresql/14/main/recovery.conf <<EOF
restore_command = 'cp /backups/wal/%f %p'
recovery_target_time = '2026-03-15 14:00:00'
EOF

# 6. PostgreSQL 시작
sudo systemctl start postgresql

# 7. 복구 검증
psql -U wcs_admin -d operato_wcs -c "SELECT COUNT(*) FROM job_batches WHERE created_at < '2026-03-15 14:00:00';"
```

#### 4.2.2 전체 시스템 복구 (Disaster Recovery)

**시나리오**: 서버 전체 장애 발생, 새 서버에서 복구

```bash
# 1. 새 서버 환경 구성
sudo apt update && sudo apt install -y docker.io docker-compose postgresql-client

# 2. Docker 이미지 복원
docker pull operato-wcs:1.2.0

# 3. 데이터베이스 복원
aws s3 cp s3://operato-wcs-backups/database/operato_wcs_latest.sql.gz /tmp/
gunzip < /tmp/operato_wcs_latest.sql.gz | psql -U wcs_admin -d operato_wcs

# 4. 설정 파일 복원
aws s3 cp s3://operato-wcs-backups/config/config_latest.tar.gz /tmp/
tar -xzf /tmp/config_latest.tar.gz -C /opt/operato-wcs/

# 5. 애플리케이션 시작
docker-compose up -d

# 6. 헬스 체크
curl -f http://localhost:9500/actuator/health

# 7. 스모크 테스트
./scripts/smoke-test.sh localhost:9500
```

### 4.3 백업 검증

**월간 백업 복원 테스트 (매월 1일)**
```bash
#!/bin/bash
# scripts/test-backup-restore.sh

# 테스트 환경에 최신 백업 복원
LATEST_BACKUP=$(aws s3 ls s3://operato-wcs-backups/database/ | sort | tail -1 | awk '{print $4}')

echo "Testing restore of $LATEST_BACKUP"
aws s3 cp s3://operato-wcs-backups/database/$LATEST_BACKUP /tmp/

# 테스트 DB에 복원
gunzip < /tmp/$LATEST_BACKUP | psql -U wcs_admin -d operato_wcs_test

# 데이터 무결성 검증
psql -U wcs_admin -d operato_wcs_test -c "SELECT COUNT(*) FROM job_batches;"
psql -U wcs_admin -d operato_wcs_test -c "SELECT COUNT(*) FROM orders;"

# 테스트 결과 리포트
echo "Backup restore test completed: $(date)" >> /var/log/backup-test.log
```

---

## 5. 보안 관리

### 5.1 보안 체크리스트

#### 5.1.1 네트워크 보안

- [ ] **방화벽 설정**
  ```bash
  # 필요한 포트만 오픈
  sudo ufw allow 22/tcp    # SSH
  sudo ufw allow 80/tcp    # HTTP
  sudo ufw allow 443/tcp   # HTTPS
  sudo ufw deny 9500/tcp   # WCS 내부 포트 (외부 차단)
  sudo ufw enable
  ```

- [ ] **SSL/TLS 인증서**
  ```bash
  # Let's Encrypt 인증서 발급
  sudo certbot --nginx -d wcs.operato.com

  # 자동 갱신 설정
  sudo crontab -e
  # 0 0 1 * * certbot renew --quiet
  ```

- [ ] **VPN 또는 Private Network**
  - DB, RabbitMQ는 Private Subnet에 배치
  - Bastion Host를 통한 접근만 허용

#### 5.1.2 애플리케이션 보안

- [ ] **인증 및 권한**
  - JWT 토큰 만료 시간: 1시간
  - Refresh Token: 7일
  - 비밀번호 정책: 최소 8자, 영문+숫자+특수문자
  - 비밀번호 만료: 90일

- [ ] **API 보안**
  ```yaml
  # application.yml
  security:
    rate-limiting:
      enabled: true
      max-requests: 100  # 분당 최대 요청 수
    cors:
      allowed-origins:
        - https://wcs.operato.com
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
    headers:
      content-security-policy: "default-src 'self'"
      x-frame-options: DENY
      x-content-type-options: nosniff
  ```

- [ ] **민감 정보 관리**
  ```bash
  # AWS Secrets Manager 사용
  aws secretsmanager create-secret \
    --name wcs/database/password \
    --secret-string "your-password"

  # 애플리케이션에서 참조
  export DB_PASSWORD=$(aws secretsmanager get-secret-value \
    --secret-id wcs/database/password \
    --query SecretString --output text)
  ```

#### 5.1.3 취약점 관리

**정기 보안 스캔 (주 1회)**
```bash
#!/bin/bash
# scripts/security-scan.sh

# Docker 이미지 스캔
trivy image operato-wcs:latest

# 의존성 취약점 스캔
./gradlew dependencyCheckAnalyze

# OWASP ZAP 스캔 (스테이징 환경)
docker run -t owasp/zap2docker-stable zap-baseline.py \
  -t https://staging.wcs.operato.com \
  -r zap-report-$(date +%Y%m%d).html

# 결과 리포트
echo "Security scan completed: $(date)" >> /var/log/security-scan.log
```

### 5.2 접근 제어

#### 5.2.1 사용자 권한 관리

| 역할 | 권한 | 접근 범위 |
|------|------|----------|
| **Super Admin** | 모든 권한 | 모든 도메인 |
| **Domain Admin** | 도메인 관리 | 특정 도메인 |
| **Operator** | 작업 실행 | 작업 조회/실행 |
| **Viewer** | 읽기 전용 | 조회만 가능 |
| **API User** | API 접근 | API 엔드포인트 |

#### 5.2.2 감사 로그

```sql
-- 모든 중요 작업 기록
CREATE TABLE audit_logs (
    id VARCHAR(40) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    action VARCHAR(50) NOT NULL,  -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    resource_type VARCHAR(50),    -- ORDER, BATCH, SKU, USER
    resource_id VARCHAR(40),
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX ix_audit_logs_0 ON audit_logs (user_id, created_at);
CREATE INDEX ix_audit_logs_1 ON audit_logs (action, resource_type);
```

---

## 6. 성능 최적화

### 6.1 데이터베이스 최적화

#### 6.1.1 인덱스 최적화

**정기 인덱스 분석 (월 1회)**
```sql
-- 사용되지 않는 인덱스 찾기
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND indexrelname NOT LIKE '%_pkey'
ORDER BY schemaname, tablename;

-- 중복 인덱스 찾기
SELECT a.indrelid::regclass, a.indexrelid::regclass, b.indexrelid::regclass
FROM pg_index a
JOIN pg_index b ON a.indrelid = b.indrelid
WHERE a.indexrelid <> b.indexrelid
  AND a.indkey::text = b.indkey::text;
```

#### 6.1.2 쿼리 최적화

**슬로우 쿼리 로그 분석**
```bash
# postgresql.conf
log_min_duration_statement = 200  # 200ms 이상 쿼리 로그

# 슬로우 쿼리 분석
sudo pgbadger /var/log/postgresql/postgresql-*.log -o /tmp/pgbadger-report.html
```

#### 6.1.3 커넥션 풀 튜닝

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # 최대 커넥션 수
      minimum-idle: 5              # 최소 유휴 커넥션
      connection-timeout: 30000    # 커넥션 타임아웃 (30초)
      idle-timeout: 600000         # 유휴 타임아웃 (10분)
      max-lifetime: 1800000        # 최대 생명 주기 (30분)
```

### 6.2 애플리케이션 최적화

#### 6.2.1 JVM 튜닝

```bash
# docker-compose.yml
services:
  wcs-app:
    environment:
      JAVA_OPTS: >-
        -Xms2g
        -Xmx4g
        -XX:+UseG1GC
        -XX:MaxGCPauseMillis=200
        -XX:+HeapDumpOnOutOfMemoryError
        -XX:HeapDumpPath=/logs/heap-dump.hprof
```

#### 6.2.2 캐싱 전략

**Redis 캐싱**
```java
// 상품 마스터 캐싱 (TTL: 1시간)
@Cacheable(value = "sku", key = "#skuCd", unless = "#result == null")
public SKU getSku(String skuCd) {
    return skuRepository.findBySkuCd(skuCd);
}

// 설비 상태 캐싱 (TTL: 5분)
@Cacheable(value = "equipmentStatus", key = "#equipCd", unless = "#result == null")
public EquipmentStatus getEquipmentStatus(String equipCd) {
    return equipmentService.getStatus(equipCd);
}
```

### 6.3 성능 테스트

**부하 테스트 (JMeter)**
```bash
# 10,000건/시 주문 처리 시나리오
jmeter -n -t loadtest-orders.jmx \
  -l results-$(date +%Y%m%d).jtl \
  -Jusers=100 \
  -Jrampup=60 \
  -Jduration=3600

# 결과 분석
jmeter -g results-20260301.jtl -o /tmp/jmeter-report
```

---

## 7. 장애 대응

### 7.1 장애 등급 및 대응 시간

| 등급 | 정의 | 대응 시간 | 복구 목표 |
|------|------|----------|----------|
| **P0 (Critical)** | 전체 서비스 중단 | 즉시 (5분) | 1시간 |
| **P1 (High)** | 주요 기능 장애 | 15분 이내 | 4시간 |
| **P2 (Medium)** | 일부 기능 장애 | 1시간 이내 | 1일 |
| **P3 (Low)** | 사소한 문제 | 1일 이내 | 1주 |

### 7.2 장애 대응 Runbook

#### 7.2.1 서비스 응답 없음 (P0)

**증상**
- 헬스 체크 실패
- API 요청 타임아웃
- 사용자 접속 불가

**원인 분석**
```bash
# 1. 서비스 상태 확인
docker ps | grep operato-wcs
systemctl status wcs-app

# 2. 로그 확인
docker logs operato-wcs --tail 100
tail -100 /var/log/wcs/application.log

# 3. 리소스 확인
top -b -n 1 | head -20
df -h
free -m
```

**대응 절차**
```bash
# Step 1: 즉시 재시작 시도
docker restart operato-wcs

# Step 2: 재시작 실패 시 롤백 (Blue-Green)
./scripts/switch-to-blue.sh

# Step 3: 로그 수집
docker logs operato-wcs > /tmp/incident-$(date +%Y%m%d_%H%M%S).log

# Step 4: 문제 분석 및 수정
# (OOM, DB 커넥션 풀 고갈, 네트워크 장애 등)

# Step 5: 사후 조치
# - Post-mortem 작성
# - 재발 방지 대책 수립
```

#### 7.2.2 데이터베이스 장애 (P0)

**증상**
- DB 커넥션 오류
- 쿼리 타임아웃
- "too many connections" 에러

**원인 분석**
```bash
# 1. PostgreSQL 상태 확인
sudo systemctl status postgresql
psql -U wcs_admin -d operato_wcs -c "SELECT version();"

# 2. 커넥션 수 확인
psql -U wcs_admin -d operato_wcs -c "
  SELECT count(*) as connections, state
  FROM pg_stat_activity
  GROUP BY state;
"

# 3. 슬로우 쿼리 확인
psql -U wcs_admin -d operato_wcs -c "
  SELECT pid, now() - query_start as duration, query
  FROM pg_stat_activity
  WHERE state = 'active'
  ORDER BY duration DESC
  LIMIT 10;
"
```

**대응 절차**
```bash
# Step 1: 급한 경우 커넥션 강제 종료
psql -U wcs_admin -d operato_wcs -c "
  SELECT pg_terminate_backend(pid)
  FROM pg_stat_activity
  WHERE datname = 'operato_wcs'
    AND pid <> pg_backend_pid()
    AND state = 'idle'
    AND state_change < current_timestamp - INTERVAL '10 minutes';
"

# Step 2: PostgreSQL 재시작
sudo systemctl restart postgresql

# Step 3: 애플리케이션 재연결
docker restart operato-wcs

# Step 4: 원인 분석
# - 슬로우 쿼리 최적화
# - 커넥션 풀 설정 조정
# - 인덱스 추가
```

#### 7.2.3 메모리 부족 (P1)

**증상**
- OutOfMemoryError
- GC 시간 증가
- 응답 시간 느려짐

**원인 분석**
```bash
# 1. 힙 덤프 분석
jmap -dump:format=b,file=/tmp/heap.bin <PID>
# Eclipse MAT로 분석

# 2. GC 로그 확인
jstat -gcutil <PID> 1000 10

# 3. 메모리 누수 의심 객체 확인
jmap -histo <PID> | head -20
```

**대응 절차**
```bash
# Step 1: 힙 크기 증가
# docker-compose.yml
JAVA_OPTS: "-Xms4g -Xmx8g"

# Step 2: 컨테이너 재시작
docker-compose up -d

# Step 3: 메모리 누수 수정
# (힙 덤프 분석 후 코드 수정)
```

### 7.3 장애 사후 분석 (Post-mortem)

**템플릿**
```markdown
# 장애 보고서

## 요약
- **발생일시**: 2026-03-15 14:30
- **복구일시**: 2026-03-15 15:45
- **영향 범위**: 전체 서비스 75분 중단
- **심각도**: P0 (Critical)

## 타임라인
- 14:30: 장애 감지 (Grafana 알림)
- 14:32: On-call Engineer 대응 시작
- 14:35: 원인 파악 (DB 커넥션 풀 고갈)
- 14:40: 임시 조치 (idle 커넥션 강제 종료)
- 14:50: PostgreSQL 재시작
- 15:00: 서비스 복구
- 15:45: 정상화 확인

## 근본 원인 (Root Cause)
- 슬로우 쿼리로 인한 DB 커넥션 홀딩
- 커넥션 풀 설정 부족 (max: 10 → 20으로 증가 필요)

## 해결 방안
1. 슬로우 쿼리 인덱스 추가
2. 커넥션 풀 최대값 증가
3. 커넥션 타임아웃 설정 추가

## 재발 방지
- [ ] 슬로우 쿼리 알림 추가 (> 500ms)
- [ ] DB 커넥션 풀 사용률 모니터링 추가
- [ ] 정기 쿼리 성능 리뷰 (월 1회)

## 교훈
- 프로액티브 모니터링 강화 필요
- DB 인덱스 최적화 정기 점검 필요
```

---

## 8. 일상 운영

### 8.1 일일 운영 체크리스트

**매일 오전 9시**
```bash
#!/bin/bash
# scripts/daily-check.sh

echo "=== Operato WCS 일일 체크 $(date) ==="

# 1. 서비스 상태 확인
echo "1. 서비스 상태"
docker ps | grep operato-wcs
systemctl status postgresql rabbitmq-server redis

# 2. 디스크 용량 확인
echo "2. 디스크 용량"
df -h | grep -E "/$|/var"

# 3. 에러 로그 확인 (최근 24시간)
echo "3. 에러 로그 (최근 24시간)"
grep -i error /var/log/wcs/application.log | tail -20

# 4. 주요 메트릭 확인
echo "4. 주요 메트릭"
curl -s http://localhost:9500/actuator/metrics/http.server.requests | jq .

# 5. 백업 상태 확인
echo "5. 백업 상태"
ls -lh /backups/postgresql/ | tail -5

# 결과 리포트
echo "=== 체크 완료 $(date) ===" >> /var/log/daily-check.log
```

### 8.2 주간 운영 작업

**매주 월요일**
- [ ] 성능 리포트 리뷰 (지난 주 트렌드 분석)
- [ ] 디스크 정리 (오래된 로그 삭제)
- [ ] 보안 스캔 실행
- [ ] 백업 복원 테스트 (샘플)

**매주 금요일**
- [ ] 다음 주 배포 계획 확인
- [ ] 리소스 사용 추세 분석
- [ ] On-call 담당자 교대

### 8.3 월간 운영 작업

**매월 1일**
- [ ] 전체 백업 복원 테스트
- [ ] 보안 패치 적용
- [ ] 인증서 만료 확인
- [ ] 라이선스 갱신 확인

**매월 15일**
- [ ] 용량 계획 검토 (Capacity Planning)
- [ ] 성능 트렌드 분석
- [ ] 비용 분석 및 최적화

---

## 9. 스케일링 전략

### 9.1 수평 확장 (Horizontal Scaling)

#### 9.1.1 애플리케이션 스케일링

**Docker Swarm / Kubernetes**
```yaml
# docker-compose.yml
services:
  wcs-app:
    image: operato-wcs:1.2.0
    deploy:
      replicas: 3  # 3개 인스턴스
      resources:
        limits:
          cpus: '2'
          memory: 4G
      update_config:
        parallelism: 1
        delay: 10s
```

**Auto Scaling (AWS ECS)**
```yaml
# ecs-service.yml
service:
  name: operato-wcs
  desired_count: 3
  auto_scaling:
    min_capacity: 2
    max_capacity: 10
    target_tracking_scaling_policies:
      - metric: CPUUtilization
        target: 70
      - metric: RequestCountPerTarget
        target: 1000
```

#### 9.1.2 데이터베이스 스케일링

**Read Replica 구성**
```yaml
# PostgreSQL Read Replica
primary:
  host: db-primary.internal
  port: 5432

replicas:
  - host: db-replica-1.internal
    port: 5432
  - host: db-replica-2.internal
    port: 5432

# Spring Boot 설정
spring:
  datasource:
    primary:
      url: jdbc:postgresql://db-primary.internal:5432/operato_wcs
    replica:
      url: jdbc:postgresql://db-replica-1.internal:5432/operato_wcs
```

### 9.2 수직 확장 (Vertical Scaling)

**리소스 증설 시나리오**

| 현재 스펙 | 업그레이드 스펙 | 예상 성능 향상 |
|----------|---------------|--------------|
| 4 vCPU, 8GB RAM | 8 vCPU, 16GB RAM | TPS 2배 |
| 8 vCPU, 16GB RAM | 16 vCPU, 32GB RAM | TPS 1.5배 |

---

## 10. 운영 자동화

### 10.1 자동화 스크립트

#### 10.1.1 헬스 체크 자동화

```bash
#!/bin/bash
# scripts/auto-heal.sh

# 헬스 체크 실패 시 자동 재시작
while true; do
    if ! curl -f http://localhost:9500/actuator/health > /dev/null 2>&1; then
        echo "[$(date)] Health check failed, restarting..."
        docker restart operato-wcs

        # Slack 알림
        curl -X POST $SLACK_WEBHOOK_URL \
          -d "{\"text\":\"WCS auto-restarted due to health check failure\"}"
    fi
    sleep 60
done
```

#### 10.1.2 로그 로테이션

```bash
# /etc/logrotate.d/wcs
/var/log/wcs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 wcs wcs
    postrotate
        docker kill -s HUP operato-wcs
    endscript
}
```

### 10.2 ChatOps (Slack 통합)

**Slack Bot 명령어**
```
/wcs status           # 시스템 상태 조회
/wcs deploy [tag]     # 배포 실행
/wcs rollback         # 롤백 실행
/wcs logs [lines]     # 로그 조회
/wcs restart          # 서비스 재시작
/wcs metrics          # 주요 메트릭 조회
```

---

## 참고 문서

- [backend-docker.md](backend-docker.md) — Docker 배포 가이드
- [database-specification.md](../design/database-specification.md) — DB 명세
- [operato-wcs-development-plan.md](../plans/operato-wcs-development-plan.md) — 개발 계획
- [CLAUDE.md](/Users/shortstop/Git/operato-wcs-ai/CLAUDE.md) — 프로젝트 컨벤션

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 2026-03-01 | 1.0.0 | 운영 전략 및 방안 최초 작성 | Claude Sonnet 4.5 |

---

**작성 완료일**: 2026-03-01
**문서 상태**: 초안 (Draft)
**검토 필요**: 실제 운영 환경에 맞게 조정 필요
