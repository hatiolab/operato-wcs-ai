# SMS 모듈 API 목록 (SMS Module API List)

## 슈트 클래스 코드 API (`ChuteClassCodeController`)
* **기본 경로(Base Path)**: `/rest/chute_class_codes`
* **설명**: 슈트 클래스 코드 정보 관리를 위한 CRUD API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `ChuteClassCode input` | `ChuteClassCode` |
| GET | `/{id}` | ID로 단건 조회 | [Path] `id`(String) | `ChuteClassCode` |
| PUT | `/{id}` | 수정 | [Path] `id`(String), [Body] `ChuteClassCode input` | `ChuteClassCode` |
| DELETE | `/{id}` | 삭제 | [Path] `id`(String) | `void` |
| GET | `/{id}/exist` | ID 존재 여부 확인 | [Path] `id`(String) | `Boolean` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<ChuteClassCode> list` | `Boolean` |

## 슈트 API (`ChuteController`)
* **기본 경로(Base Path)**: `/rest/chutes`
* **설명**: 슈트(Chute) 장비 기준 정보 관리를 위한 CRUD API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Chute input` | `Chute` |
| GET | `/{id}` | ID로 단건 조회 | [Path] `id`(String) | `Chute` |
| PUT | `/{id}` | 수정 | [Path] `id`(String), [Body] `Chute input` | `Chute` |
| DELETE | `/{id}` | 삭제 | [Path] `id`(String) | `void` |
| GET | `/{id}/exist` | ID 존재 여부 확인 | [Path] `id`(String) | `Boolean` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Chute> list` | `Boolean` |

## 인덕션 API (`InductionController`)
* **기본 경로(Base Path)**: `/rest/inductions`
* **설명**: 인덕션(Induction) 장비 기준 정보 관리를 위한 CRUD API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Induction input` | `Induction` |
| GET | `/{id}` | ID로 단건 조회 | [Path] `id`(String) | `Induction` |
| PUT | `/{id}` | 수정 | [Path] `id`(String), [Body] `Induction input` | `Induction` |
| DELETE | `/{id}` | 삭제 | [Path] `id`(String) | `void` |
| GET | `/{id}/exist` | ID 존재 여부 확인 | [Path] `id`(String) | `Boolean` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Induction> list` | `Boolean` |

## 소터 API (`SorterController`)
* **기본 경로(Base Path)**: `/rest/sorters`
* **설명**: 소터(Sorter) 장비 기준 정보 관리를 위한 CRUD API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Sorter input` | `Sorter` |
| GET | `/{id}` | ID로 단건 조회 | [Path] `id`(String) | `Sorter` |
| PUT | `/{id}` | 수정 | [Path] `id`(String), [Body] `Sorter input` | `Sorter` |
| DELETE | `/{id}` | 삭제 | [Path] `id`(String) | `void` |
| GET | `/{id}/exist` | ID 존재 여부 확인 | [Path] `id`(String) | `Boolean` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Sorter> list` | `Boolean` |

## 소터 실적 트래킹 API (`SmsTrackingController`)
* **기본 경로(Base Path)**: `/rest/sms_trackings`
* **설명**: 소터/슈트 실적 집계 및 트래킹 조회 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/chute_result` | 슈트 실적 내역 조회 (페이징) | [Query] `page`(Integer), `limit`(Integer), `select`(String), `sort`(String), `query`(String) | `Page<?>` |
