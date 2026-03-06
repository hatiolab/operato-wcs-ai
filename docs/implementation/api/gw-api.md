# GW 모듈 API 목록 (Gateway Module API List)

GW(Gateway) 모듈은 게이트웨이, 표시기(Indicator), 펌웨어 배포(Deployment), 표시기 설정(IndConfigSet) 등 **물리 설비 연동 기반 인프라**를 관리하는 API를 제공합니다.

- **패키지**: `xyz.anythings.gw.rest`
- **공통 파라미터**: 페이징 조회 API는 모두 `page`, `limit`, `select`, `sort`, `query` 쿼리 파라미터를 지원합니다.

---

## 게이트웨이 API (`GatewayController`)
* **기본 경로(Base Path)**: `/rest/gateways`
* **설명**: 설비 연동 게이트웨이(Gateway) 기준 정보를 관리하는 API
* **엔티티**: `Gateway` (테이블: `gateways`)

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`, `limit`, `select`, `sort`, `query` | `Page<?>` |
| GET | `/{id}` | 단건 조회 | [Path] `id` | `Gateway` |
| GET | `/{id}/exist` | 존재 여부 확인 | [Path] `id` | `Boolean` |
| POST | `/` | 신규 생성 | [Body] `Gateway input` | `Gateway` |
| PUT | `/{id}` | 수정 | [Path] `id`, [Body] `Gateway input` | `Gateway` |
| DELETE | `/{id}` | 삭제 | [Path] `id` | `void` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Gateway> list` | `Boolean` |
| GET | `/search_by_equip/{equip_type}/{equip_cd}` | 설비 기준 게이트웨이 조회 | [Path] `equip_type`, `equip_cd` | `List<Gateway>` |

### 주요 엔드포인트 상세

#### `GET /search_by_equip/{equip_type}/{equip_cd}`
특정 설비에 연결된 게이트웨이 목록을 조회합니다. 내부적으로 `gateways`, `indicators`, `cells` 테이블을 JOIN하여 해당 설비 타입·코드에 매핑된 게이트웨이를 검색합니다.

### Gateway 엔티티 필드

| 필드 (Field) | 컬럼 (Column) | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| id | id | String(40) | Y | PK (UUID) |
| stageCd | stage_cd | String(30) | Y | 스테이지 코드 |
| gwCd | gw_cd | String(30) | Y | 게이트웨이 코드 (도메인 내 유니크) |
| gwNm | gw_nm | String(100) | N | 게이트웨이 명칭 |
| gwIp | gw_ip | String(16) | N | 게이트웨이 IP 주소 |
| channelNo | channel_no | String(40) | N | 채널 번호 |
| panNo | pan_no | String(40) | N | PAN 번호 |
| version | version | String(15) | N | 펌웨어 버전 |
| status | status | String(10) | N | 상태 |
| remark | remark | String(1000) | N | 비고 |

---

## 표시기 API (`IndicatorController`)
* **기본 경로(Base Path)**: `/rest/indicators`
* **설명**: 표시기(Indicator/LED) 기준 정보를 관리하는 API
* **엔티티**: `Indicator` (테이블: `indicators`)

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징, 설비 필터링 지원) | [Query] `page`, `limit`, `select`, `sort`, `query` | `Page<?>` |
| GET | `/{id}` | 단건 조회 | [Path] `id` | `Indicator` |
| GET | `/{id}/exist` | 존재 여부 확인 | [Path] `id` | `Boolean` |
| POST | `/` | 신규 생성 | [Body] `Indicator input` | `Indicator` |
| PUT | `/{id}` | 수정 | [Path] `id`, [Body] `Indicator input` | `Indicator` |
| DELETE | `/{id}` | 삭제 | [Path] `id` | `void` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Indicator> list` | `Boolean` |

### 주요 엔드포인트 상세

#### `GET /` — 설비 기준 필터링
`query` 파라미터에 `equip_cd` 필터가 포함된 경우 커스텀 로직이 동작합니다:
1. `equip_type`과 `equip_cd` 필터를 추출하여 `cells` 테이블에서 해당 설비에 매핑된 표시기 코드(`ind_cd`) 목록을 조회
2. 조회된 `ind_cd` 목록을 IN 조건으로 변환하여 표시기를 필터링
3. 원래의 `equip_type`, `equip_cd` 필터는 쿼리에서 제거

### Indicator 엔티티 필드

| 필드 (Field) | 컬럼 (Column) | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| id | id | String(40) | Y | PK (UUID) |
| gwCd | gw_cd | String(30) | N | 소속 게이트웨이 코드 |
| indCd | ind_cd | String(30) | Y | 표시기 코드 (게이트웨이 내 유니크) |
| indNm | ind_nm | String(100) | N | 표시기 명칭 |
| version | version | String(15) | N | 펌웨어 버전 |
| rssi | rssi | Float | N | 수신 신호 강도 |
| status | status | String(10) | N | 상태 |
| remark | remark | String(1000) | N | 비고 |

---

## 펌웨어 배포 API (`DeploymentController`)
* **기본 경로(Base Path)**: `/rest/deployments`
* **설명**: 게이트웨이·표시기 펌웨어 배포(Deployment) 정보를 관리하는 API
* **엔티티**: `Deployment` (테이블: `deployments`)

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`, `limit`, `select`, `sort`, `query` | `Page<?>` |
| GET | `/{id}` | 단건 조회 | [Path] `id` | `Deployment` |
| GET | `/{id}/exist` | 존재 여부 확인 | [Path] `id` | `Boolean` |
| POST | `/` | 신규 생성 | [Body] `Deployment input` | `Deployment` |
| PUT | `/{id}` | 수정 | [Path] `id`, [Body] `Deployment input` | `Deployment` |
| DELETE | `/{id}` | 삭제 | [Path] `id` | `void` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Deployment> list` | `Boolean` |

### 배포 상태 값

| 상태 | 상수 | 설명 |
|---|---|---|
| WAIT | `STATUS_WAIT` | 예약 대기 |
| RESERVED | `STATUS_RESERVED` | 예약됨 |
| RUN | `STATUS_RUN` | 배포 진행 중 |
| END | `STATUS_END` | 배포 완료 |
| ERROR | `STATUS_ERROR` | 배포 중 에러 발생 |

### 대상 타입 값

| 타입 | 상수 | 설명 |
|---|---|---|
| GW | `TARGET_TYPE_GW` | 게이트웨이 펌웨어 |
| MPI | `TARGET_TYPE_MPI` | 표시기 펌웨어 |

### Deployment 엔티티 필드

| 필드 (Field) | 컬럼 (Column) | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| id | id | String(40) | Y | PK (UUID, AttachTemp ID와 동일) |
| targetType | target_type | String(20) | Y | 대상 타입 (GW / MPI) |
| targetId | target_id | String(40) | Y | 대상 ID |
| version | version | String(15) | Y | 배포 버전 |
| scheduledAt | scheduled_at | String(22) | Y | 배포 예약 일시 |
| startedAt | started_at | String(22) | N | 배포 시작 일시 |
| finishedAt | finished_at | String(22) | N | 배포 완료 일시 |
| fileName | file_name | String(100) | N | 펌웨어 파일명 |
| fileSize | file_size | Long | N | 펌웨어 파일 크기 |
| forceFlag | force_flag | Boolean | N | 강제 배포 여부 |
| status | status | String(10) | N | 배포 상태 |
| fileData | file_data | byte[] | N | 펌웨어 파일 데이터 |
| remark | remark | String(1000) | N | 비고 |

---

## 표시기 설정 셋 API (`IndConfigSetController`)
* **기본 경로(Base Path)**: `/rest/ind_config_set`
* **설명**: 표시기 설정 셋(IndConfigSet) 및 설정 항목(IndConfig)을 관리하는 API. 배치(Batch)·스테이지(Stage) 범위의 설정 프로파일 빌드/조회 기능 포함.
* **엔티티**: `IndConfigSet` (테이블: `ind_config_set`), `IndConfig` (테이블: `ind_configs`)
* **의존 서비스**: `IIndConfigProfileService`

### 기본 CRUD

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`, `limit`, `select`, `sort`, `query` | `Page<?>` |
| GET | `/{id}` | 단건 조회 | [Path] `id` | `IndConfigSet` |
| GET | `/{id}/exist` | 존재 여부 확인 | [Path] `id` | `Boolean` |
| POST | `/` | 신규 생성 | [Body] `IndConfigSet input` | `IndConfigSet` |
| PUT | `/{id}` | 수정 | [Path] `id`, [Body] `IndConfigSet input` | `IndConfigSet` |
| DELETE | `/{id}` | 삭제 | [Path] `id` | `void` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<IndConfigSet> list` | `Boolean` |

### 설정 항목 관리

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/{id}/include_details` | 설정 셋 + 하위 설정 항목 포함 조회 | [Path] `id` | `Map<String, Object>` |
| GET | `/{id}/items` | 설정 항목 목록 조회 | [Path] `id` | `List<IndConfig>` |
| POST | `/{id}/items/update_multiple` | 설정 항목 다건 수정 | [Path] `id`, [Body] `List<IndConfig> list` | `List<IndConfig>` |

### 설정 셋 복사

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| POST | `/{id}/copy` | 템플릿 설정 셋 복사 | [Path] `id` (원본 ID), [Body] `Map { target_code, target_name }` | `IndConfigSet` |

### 배치(Batch) 범위 설정

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| POST | `/batch/build_config_set/{batch_id}/{ind_config_set_id}` | 배치별 설정 셋 빌드 | [Path] `batch_id`, `ind_config_set_id` | `IndConfigSet` |
| GET | `/batch/config_value/{batch_id}` | 배치 범위 설정값 조회 | [Path] `batch_id`, [Query] `config_key` | `KeyValue` |
| DELETE | `/clear_config_set/{batch_id}` | 배치별 설정 셋 초기화 | [Path] `batch_id` | `BaseResponse` |

### 스테이지(Stage) 범위 설정

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| POST | `/stage/build_config_set/{stage_cd}` | 스테이지별 기본 설정 셋 빌드 | [Path] `stage_cd` ("ALL" 입력 시 전체 스테이지) | `BaseResponse` |
| GET | `/stage/config_value/{stage_cd}` | 스테이지 범위 설정값 조회 | [Path] `stage_cd`, [Query] `config_key` | `KeyValue` |

### IndConfigSet 엔티티 필드

| 필드 (Field) | 컬럼 (Column) | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| id | id | String(40) | Y | PK (UUID) |
| stageCd | stage_cd | String(30) | N | 스테이지 코드 |
| indType | ind_type | String(20) | N | 표시기 타입 |
| comCd | com_cd | String(30) | N | 회사 코드 |
| jobType | job_type | String(20) | N | 작업 유형 |
| equipType | equip_type | String(20) | N | 설비 타입 |
| equipCd | equip_cd | String(30) | N | 설비 코드 |
| confSetCd | conf_set_cd | String(30) | Y | 설정 셋 코드 |
| confSetNm | conf_set_nm | String(100) | N | 설정 셋 명칭 |
| defaultFlag | default_flag | Boolean | N | 기본 설정 여부 |
| remark | remark | String(1000) | N | 비고 |
| items | — | List\<IndConfig\> | — | 하위 설정 항목 (비영속, @Ignore) |

### IndConfig 엔티티 필드

| 필드 (Field) | 컬럼 (Column) | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| id | id | String(40) | Y | PK (UUID) |
| indConfigSetId | ind_config_set_id | String(40) | Y | 소속 설정 셋 ID (FK) |
| category | category | String(100) | N | 카테고리 |
| name | name | String(40) | Y | 설정 키 (설정 셋 내 유니크) |
| description | description | String(255) | N | 설정 설명 |
| value | value | String(100) | N | 설정 값 |
| remark | remark | String(255) | N | 비고 |
| config | config | String(4000) | N | 추가 설정 (JSON 등) |
