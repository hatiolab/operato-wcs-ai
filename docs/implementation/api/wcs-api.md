# WCS 모듈 API 목록 (WCS Module API List)

## 일별 생산성 집계 API (`DailyProdSummaryController`)
* **기본 경로(Base Path)**: `/rest/daily_prod_summary`
* **설명**: 일별 생산성 요약 정보 관리를 위한 CRUD API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `DailyProdSummary input` | `DailyProdSummary` |
| GET | `/{id}` | ID로 단건 조회 | [Path] `id`(String) | `DailyProdSummary` |
| PUT | `/{id}` | 수정 | [Path] `id`(String), [Body] `DailyProdSummary input` | `DailyProdSummary` |
| DELETE | `/{id}` | 삭제 | [Path] `id`(String) | `void` |
| GET | `/{id}/exist` | ID 존재 여부 확인 | [Path] `id`(String) | `Boolean` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<DailyProdSummary> list` | `Boolean` |

## 작업자 생산성 API (`ProductivityController`)
* **기본 경로(Base Path)**: `/rest/productivity`
* **설명**: 생산성(Productivity) 데이터 관리를 위한 CRUD API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Productivity input` | `Productivity` |
| GET | `/{id}` | ID로 단건 조회 | [Path] `id`(String) | `Productivity` |
| PUT | `/{id}` | 수정 | [Path] `id`(String), [Body] `Productivity input` | `Productivity` |
| DELETE | `/{id}` | 삭제 | [Path] `id`(String) | `void` |
| GET | `/{id}/exist` | ID 존재 여부 확인 | [Path] `id`(String) | `Boolean` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Productivity> list` | `Boolean` |

## 웨이브 데이터 API (`WaveController`)
* **기본 경로(Base Path)**: `/rest/waves`
* **설명**: 웨이브(Wave) 데이터 기본 관리를 위한 CRUD API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Wave input` | `Wave` |
| GET | `/{id}` | ID로 단건 조회 | [Path] `id`(String) | `Wave` |
| PUT | `/{id}` | 수정 | [Path] `id`(String), [Body] `Wave input` | `Wave` |
| DELETE | `/{id}` | 삭제 | [Path] `id`(String) | `void` |
| GET | `/{id}/exist` | ID 존재 여부 확인 | [Path] `id`(String) | `Boolean` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Wave> list` | `Boolean` |

## 설비 작업자 투입 실적 API (`WorkerActualController`)
* **기본 경로(Base Path)**: `/rest/worker_actuals`
* **설명**: 설비별 작업자 투입/종료 실적 관리를 위한 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 실적 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
| POST | `/` | 신규 실적 생성 | [Body] `WorkerActual input` | `WorkerActual` |
| GET | `/{id}` | ID로 단건 조회 | [Path] `id`(String) | `WorkerActual` |
| PUT | `/{id}` | 실적 수정 | [Path] `id`(String), [Body] `WorkerActual input` | `WorkerActual` |
| DELETE | `/{id}` | 실적 삭제 | [Path] `id`(String) | `void` |
| GET | `/{id}/exist` | ID 존재 여부 확인 | [Path] `id`(String) | `Boolean` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<WorkerActual> list` | `Boolean` |
| GET | `/search/input_workers/{equip_type}/{equip_cd}` | 특정 설비에 투입된 작업자 목록 조회 | [Path] `equip_type`(String), `equip_cd`(String) | `List<WorkerActual>` |
| POST | `/input_worker` | 설비에 작업자 투입 상태 등록 | [Body] `WorkerActual input` | `WorkerActual` |
| PUT | `/out_worker/{id}` | 기투입된 작업자의 작업 종료 처리 | [Path] `id`(String) | `WorkerActual` |

## WCS 주문 프로세스 제어 API (`WcsProcessController`)
* **기본 경로(Base Path)**: `/rest/wcs_process`
* **설명**: WMS로부터 수신한 웨이브(Wave) 처리, 대상 분류, 분할/병합, 설비/작업자 할당 등 핵심 프로세스를 제어하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| POST | `/receive_waves/ready/{area_cd}/{stage_cd}/{com_cd}/{job_date}` | 웨이브 수신 준비 (작업 단위 생성) | [Path] `area_cd`(String), `stage_cd`(String), `com_cd`(String), `job_date`(String)<br/>[Query] `job_type`(String, 선택) | `BatchReceipt` |
| POST | `/receive_waves/start` | 웨이브 수신 시작 | [Body] `BatchReceipt summary` | `BatchReceipt` |
| DELETE | `/cancel_wave/{wave_id}` | 수신된 웨이브 취소 | [Path] `wave_id`(String) | `BatchReceipt` |
| POST | `/classify_wave/{wave_id}` | 웨이브 내 주문/상품을 작업 대상별로 분류 | [Path] `wave_id`(String)<br/>[Body] `List<String> classifyCodes` | `Wave` |
| POST | `/split_wave/{wave_id}/{split_method}/{count}` | 특정 로직에 따라 웨이브 작업 분할 | [Path] `wave_id`(String), `split_method`(String), `count`(Integer) | `List<Wave>` |
| POST | `/merge_wave/{main_wave_id}/{target_wave_id}` | 두 개의 분할된 웨이브를 하나로 병합 | [Path] `main_wave_id`(String), `target_wave_id`(String) | `Wave` |
| GET | `/equip_capa/{wave_id}` | 웨이브 내 작업 처리를 위한 설비 그룹별 가용 처리량 및 완료 예상 시간 조회 | [Path] `wave_id`(String) | `List<EquipCapaByWave>` |
| PUT | `/confirm_wave/{wave_id}` | 파악된 설비/작업자 배분에 따라 최종 웨이브 작업 확정 | [Path] `wave_id`(String) | `Wave` |
