# 문서 디렉토리 구조 (`docs/`)

```
docs/
├── overview/
│   └── overview.md                              # 프로젝트 개요 (시스템 목적, 제품 목표·라인업·특장점)
│
├── architecture/
│   ├── architecture.md                          # 시스템 아키텍처 (전체 구조, 핵심 기능, 설비 인터페이스)
│   ├── backend-architecture.md                  # 백엔드 아키텍처 설계 (패키지 구조, 레이어, 기술 스택)
│   └── frontend-structure.md                    # 프론트엔드 디렉토리 구조·명명 규칙·빌드 경로
│
├── checklist/
│   ├── dev-start-checklist.md                   # 개발 시작 전 준비 사항 점검 목록
│   └── dev-start-checklist-log.md               # 체크리스트 항목별 처리 이력
│
├── design/
│   ├── api-specification.md                     # REST API 엔드포인트 명세
│   ├── database-specification.md                # DB 스키마·ERD·테이블 정의
│   └── messaging-specification.md               # RabbitMQ 메시지 인터페이스 명세 (WCS↔ECS)
│
├── implementation/
│   ├── backend-coding-conventions.md            # 백엔드 Java 코딩 컨벤션
│   ├── frontend-coding-conventions.md           # 프론트엔드 Lit/TypeScript 코딩 컨벤션
│   └── api/                                     # 모듈별 API 명세
│       ├── base-api.md                          # Base 모듈 API 목록
│       ├── bms-api.md                           # BMS 모듈 API 목록
│       ├── gw-api.md                            # GW(Gateway) 모듈 API 목록
│       ├── sms-api.md                           # SMS 모듈 API 목록
│       └── wcs-api.md                           # WCS 모듈 API 목록
│
├── operations/
│   ├── backend-docker.md                        # 백엔드 Docker 배포 가이드
│   ├── operations-strategy.md                   # 운영 전략 (모니터링, 장애 대응)
│   ├── smb-infrastructure-proposal.md           # 중소 규모 인프라 제안서
│   ├── smb-infrastructure-proposal-v2.md        # 중소 규모 인프라 제안서 (v2)
│   ├── enterprise-infrastructure-proposal.md    # 대규모 엔터프라이즈 인프라 제안서
│   └── deployment.md                            # 개발 vs 운영 환경 배포 가이드
│
├── plans/
│   ├── development-plan.md                      # 개발 계획서 (마일스톤, 일정)
│   └── wbs.md                                   # WBS (Work Breakdown Structure)
│
├── quality/
│   ├── backend/                                 # 백엔드 품질 문서
│   │   ├── README.md                            # 백엔드 품질 문서 개요
│   │   ├── summary.md                           # 백엔드 품질 분석 요약
│   │   ├── code-quality-report.md               # 백엔드 코드 품질 분석 보고서
│   │   ├── improvement-checklist.md             # 백엔드 개선 항목 체크리스트
│   │   ├── security-improvements.md             # 백엔드 보안 개선 사항
│   │   └── testing-guide.md                     # 백엔드 테스트 가이드
│   └── frontend/                                # 프론트엔드 품질 문서
│       ├── code-quality-report.md               # 프론트엔드 코드 품질 분석 보고서
│       ├── improvement-checklist.md             # 프론트엔드 개선 항목 체크리스트
│       └── testing-guide.md                     # 프론트엔드 테스트 가이드
│
├── refactoring/
│   └── backend-refactoring-plan.md              # 백엔드 리팩토링 계획
│
├── requirements/
│   ├── wcs-feature-definition.md                # WCS 핵심 기능 정의서
│   ├── ecs-module-feature-definition.md         # ECS 모듈별 기능 정의서
│   ├── business-process.md                      # 입고·출고·보관 업무 프로세스 정의
│   └── feature-improvement-roadmap.md           # 기능 개선 로드맵
│
├── research/
│   ├── wcs-wms-trends-2025-2026-ko.md           # WCS/WMS 기술 트렌드 분석 (한국어)
│   └── wcs-wms-trends-2025-2026-en.md           # WCS/WMS 기술 트렌드 분석 (영어)
│
└── sales/
    ├── sales-strategy-pricing.md                # 영업 전략·가격 정책
    └── sales-strategy-pricing-v2.md             # 영업 전략·가격 정책 (v2)
```

> **유지 규칙:** `docs/` 하위에 폴더나 파일이 추가·변경·삭제되면, 이 구조를 함께 업데이트할 것.

## 명명 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 폴더명 | 영어 소문자 kebab-case | `requirements/`, `design/` |
| 파일명 | 영어 소문자 kebab-case | `backend-architecture.md` |
| 버전 구분 | `-v2`, `-v3` 접미사 | `sales-strategy-pricing-v2.md` |
| 예외 | `README.md`만 대문자 허용 | `quality/README.md` |
