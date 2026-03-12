# 품질 분석 및 개선 가이드

> 분석일: 2026-03-05 | 종합 점수: **5.7/10**

---

## 문서 목록

| 문서 | 설명 |
|------|------|
| [summary.md](summary.md) | 분석 결과 요약 (강점, 약점, 즉시 조치 항목) |
| [code-quality-report.md](code-quality-report.md) | 코드 규모, 패키지 구조, 아키텍처, 의존성 상세 분석 |
| [security-improvements.md](security-improvements.md) | 보안 취약점 목록 및 개선 방법 |
| [testing-guide.md](testing-guide.md) | 테스트 전략, 환경 구성, 작성 가이드 |
| [improvement-checklist.md](improvement-checklist.md) | 우선순위별 개선 체크리스트 (P0~P3) |

---

## 핵심 지표

| 항목 | 값 |
|------|-----|
| Java 파일 | 470개 |
| 코드 라인 | ~81,000 LOC |
| 단위 테스트 | 0개 |
| 취약 라이브러리 | 3개 (FastJSON, iText, Commons Collections) |
| God Class | 2개 (DeviceProcessController, DasAssortService) |

---

## 관련 자료

- 프로젝트 개요: [CLAUDE.md](../../CLAUDE.md)
- 아키텍처: [docs/architecture/backend-architecture.md](../architecture/backend-architecture.md)
- 배포 가이드: [docs/operations/backend-docker.md](../operations/backend-docker.md)
