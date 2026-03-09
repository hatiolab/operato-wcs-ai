# Operato WCS 프로젝트 개요

**Operato WCS (Warehouse Control System)** 는 HatioLab이 개발한 물류 창고 제어 시스템 제품입니다.
백엔드는 Spring Boot, 프론트엔드는 Lit(Web Components) 기반으로 구성됩니다.

## 시스템 목적

Operato WCS는 풀필먼트 센터(Fulfillment Center)에서 이루어지는 **입고 → 보관 → 출고 → 배송** 프로세스 전반에 걸쳐 사람과 설비의 작업을 소프트웨어적으로 **제어·모니터링**하여 최대의 운영 퍼포먼스를 지원하는 **현장 제어 시스템**입니다.

## 제품 목표

끊임없이 변화하는 물류 환경에 유연하고 신속하게 대응 가능한 **다목적 물류 시스템 구축**을 최종 목표로 합니다.

| 방향 | 핵심 내용 |
|------|----------|
| 설비 최적화 운영 | 자동화 설비의 모듈화(플러그인), 설비 Capa 기반 최적 작업 분배, 분류 설비 가동율 극대화 |
| 통합 모니터링 | 센터 전체 설비·재고 실시간 통합 모니터링 (3D 재고 모니터링 포함) |
| 유연하고 지속적인 관리 | 프로세스 변화에 신속 대응, 작업 이력 데이터 기반 설비 Capa 지속 업데이트 |

## Operato 제품 라인업

Operato WCS는 HatioLab의 물류 운영 플랫폼 중 **현장 제어(Execution)** 계층에 위치합니다.

| 제품 | 구분 | 설명 |
|------|------|------|
| Operato Visualizer | Visualization | 물류 센터 가시화 솔루션 |
| Operato WMS | Execution | 창고 관리 시스템 (WCS의 상위 시스템) |
| **Operato WCS** | **Execution** | **창고 제어 시스템 (본 프로젝트)** |
| Operato ECS | Execution | 설비 제어 시스템 (WCS와 연동되는 물리 설비 제어) |

## 제품 특장점

| 특장점 | 설명 |
|--------|------|
| Web 기반 프레임워크 | 운영자·작업자·현황판 UI 통합 제공, Responsive UI 및 PWA로 모바일 환경 지원 |
| 물류 특화 프레임워크 | 물류 작업 처리 컨테이너 기반, 자동화 설비를 플러그인 형태로 추가 |
| 설비 통합 운영 및 최적화 | 주문·작업 형태에 따라 최적 설비에 분배, 설비 가동율 극대화로 확실한 ROI 보장 |
| 높은 유연성 | 설비군별·작업 형태별 설정 프로파일 제공, 동적 커스텀 서비스 기반 프로세스 제어 |
| 통합 모니터링 제공 | 설비 상황 및 재고 현황을 웹 기반 모니터링 보드로 실시간 확인 |
| 물류환경 선진화 | 모바일, 로봇, AGV, IoT, 클라우드 기술을 활용한 스마트 물류환경 지원 |

## 관련 문서

| 문서 | 경로 |
|------|------|
| 시스템 아키텍처 | [architecture.md](../architecture/architecture.md) |
| 백엔드 아키텍처 | [backend-architecture.md](../architecture/backend-architecture.md) |
| WCS 기능 정의 | [wcs-feature-definition.md](../requirements/wcs-feature-definition.md) |
