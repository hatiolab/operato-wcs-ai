# Operato WCS 데이터베이스 명세서

## 문서 정보

- **프로젝트**: Operato WCS (Warehouse Control System)
- **버전**: 1.0.0
- **작성일**: 2026-03-01
- **작성자**: Claude Sonnet 4.5
- **ORM 프레임워크**: JPA (Java Persistence API)
- **기본 엔티티**: xyz.elidom.orm.entity.basic.ElidomStampHook

---

## 목차

1. [ORM 명명 규칙](#1-orm-명명-규칙)
2. [공통 필드 정의](#2-공통-필드-정의)
3. [모듈별 테이블 목록](#3-모듈별-테이블-목록)
4. [핵심 테이블 상세 명세](#4-핵심-테이블-상세-명세)
5. [테이블 관계도](#5-테이블-관계도)
6. [인덱스 전략](#6-인덱스-전략)
7. [데이터 타입 매핑](#7-데이터-타입-매핑)

---

## 1. ORM 명명 규칙

### 1.1 테이블 명명 규칙

| 변환 규칙 | Java 클래스명 | 테이블명 | 비고 |
|----------|--------------|----------|------|
| 기본 | `JobBatch` | `job_batches` | snake_case 복수형 |
| 단수 유지 | `Order` | `orders` | 문법적 복수형 |
| 약어 | `SKU` | `sku` | 소문자 그대로 |
| 중첩 대문자 | `PDSSystem` | `pds_system` | 연속 대문자는 분리 |

**변환 로직**:
```
PascalCase → snake_case + pluralize
JobBatch → job_batch → job_batches
Stock → stock → stocks
```

### 1.2 컬럼 명명 규칙

| 변환 규칙 | Java 필드명 | 컬럼명 | 비고 |
|----------|------------|--------|------|
| 기본 | `wmsBatchNo` | `wms_batch_no` | camelCase → snake_case |
| 연속 대문자 | `SKUCode` | `sku_code` | 대문자 구간 분리 |
| 불린 | `activeFlag` | `active_flag` | Boolean → flag 접미사 |
| 날짜/시간 | `createdAt` | `created_at` | Timestamp → _at 접미사 |

### 1.3 Primary Key 규칙

| 항목 | 규칙 | 예시 |
|------|------|------|
| PK 필드명 | `id` | 모든 엔티티 |
| PK 컬럼명 | `id` | 모든 테이블 |
| 타입 (대부분) | `String` (UUID) | length=40 |
| 타입 (일부) | `Long` (자동증가) | Domain, Site 등 |
| Generation | `GenerationRule.UUID` | 대부분 |
| Generation | `GenerationRule.NONE` | Domain, User (수동) |

### 1.4 Foreign Key 규칙

| 항목 | 규칙 | 예시 |
|------|------|------|
| FK 필드명 | `{entity}Id` | `batchId`, `userId` |
| FK 컬럼명 | `{table}_id` | `batch_id`, `user_id` |
| 특수 케이스 | `domainId` | Multi-tenant용 |

---

## 2. 공통 필드 정의

모든 엔티티는 `xyz.elidom.orm.entity.basic.ElidomStampHook`를 상속받아 다음 공통 필드를 포함합니다.

### 2.1 Multi-tenant & Audit 필드

| 필드명 | 컬럼명 | 타입 | Nullable | 길이 | 설명 |
|--------|--------|------|----------|------|------|
| domainId | domain_id | Long | NO | - | 도메인 ID (Multi-tenant 식별자) |
| creatorId | creator_id | String | YES | 40 | 생성자 사용자 ID |
| updaterId | updater_id | String | YES | 40 | 최종 수정자 사용자 ID |
| createdAt | created_at | DateTime | NO | - | 생성 일시 |
| updatedAt | updated_at | DateTime | NO | - | 최종 수정 일시 |

### 2.2 공통 인덱스

모든 테이블은 기본적으로 `domain_id`를 포함한 인덱스를 가집니다.

```sql
INDEX ix_{table}_0 ON {table} (domain_id, {business_key})
```

---

## 3. 모듈별 테이블 목록

### 3.1 xyz.anythings.base (WCS 핵심 모듈 - 56개 테이블)

#### 3.1.1 작업 관리 (Job Management)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 |
|----------|--------------|------|---------|
| job_batches | JobBatch | 작업 배치 (WCS 핵심 작업 단위) | String(UUID) |
| job_instances | JobInstance | 작업 인스턴스 | String(UUID) |
| job_inputs | JobInput | 작업 투입 | String(UUID) |
| job_configs | JobConfig | 작업 설정 | String(UUID) |
| job_config_sets | JobConfigSet | 작업 설정 세트 | String(UUID) |
| total_pickings | TotalPicking | 토탈 피킹 | String(UUID) |

#### 3.1.2 주문 관리 (Order Management)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 |
|----------|--------------|------|---------|
| orders | Order | 입/출고 주문 정보 | String(UUID) |
| order_labels | OrderLabel | 주문 라벨 | String(UUID) |
| order_preprocesses | OrderPreprocess | 주문 전처리 | String(UUID) |
| order_samplers | OrderSampler | 주문 샘플러 | String(UUID) |
| batch_receipts | BatchReceipt | 주문 수신 요약 마스터 | String(UUID) |
| batch_receipt_items | BatchReceiptItem | 주문 수신 상세 | String(UUID) |

#### 3.1.3 상품 및 재고 (Product & Inventory)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 | Unique Fields |
|----------|--------------|------|---------|---------------|
| sku | SKU | 상품 마스터 | String(UUID) | domain_id, com_cd, sku_cd |
| stocks | Stock | 재고 관리 | String(UUID) | domain_id, equip_type, equip_cd, cell_cd |
| stock_hists | StockHist | 재고 이력 | String(UUID) | - |
| stock_adjusts | StockAdjust | 재고 조정 | String(UUID) | - |
| stocktakings | Stocktaking | 재고 실사 | String(UUID) | - |
| serial_instances | SerialInstance | 시리얼 인스턴스 | String(UUID) | - |

#### 3.1.4 설비 및 시설 (Equipment & Facility)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 |
|----------|--------------|------|---------|
| equip_groups | EquipGroup | 설비 그룹 | String(UUID) |
| racks | Rack | 랙/호기 | String(UUID) |
| cells | Cell | 셀/로케이션 | String(UUID) |
| stations | Station | 작업 스테이션 | String(UUID) |
| station_workers | StationWorker | 스테이션 작업자 매핑 | String(UUID) |
| work_cells | WorkCell | 작업 셀 | String(UUID) |

#### 3.1.5 박스 및 포장 (Box & Packing)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 |
|----------|--------------|------|---------|
| box_types | BoxType | 박스 유형 | String(UUID) |
| box_packs | BoxPack | 박스 포장 | String(UUID) |
| box_items | BoxItem | 박스 아이템 | String(UUID) |
| tray_boxes | TrayBox | 트레이 박스 | String(UUID) |

#### 3.1.6 기타 설정 및 디바이스

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 |
|----------|--------------|------|---------|
| areas | Area | 구역 | String(UUID) |
| stages | Stage | 스테이지 | String(UUID) |
| companies | Company | 화주사 | String(UUID) |
| shops | Shop | 매장/판매처 | String(UUID) |
| deployments | Deployment | 배포 | String(UUID) |
| device_confs | DeviceConf | 디바이스 설정 | String(UUID) |
| device_profiles | DeviceProfile | 디바이스 프로파일 | String(UUID) |
| invoices | Invoice | 송장 | String(UUID) |
| kiosks | Kiosk | 키오스크 | String(UUID) |
| pdas | PDA | PDA | String(UUID) |
| tablets | Tablet | 태블릿 | String(UUID) |
| printers | Printer | 프린터 | String(UUID) |
| printouts | Printout | 출력물 | String(UUID) |
| popular_ranks | PopularRank | 인기 순위 | String(UUID) |
| reworks | Rework | 재작업 | String(UUID) |
| rework_items | ReworkItem | 재작업 아이템 | String(UUID) |

---

### 3.2 operato.logis.sms (Sorter Management System - 4개 테이블)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 | Unique Fields |
|----------|--------------|------|---------|---------------|
| sorters | Sorter | 자동 분류기 (Sorter) | String(UUID) | domain_id, sorter_cd |
| chutes | Chute | 슈트 (Chute) | String(UUID) | - |
| chute_class_codes | ChuteClassCode | 슈트 분류 코드 | String(UUID) | - |
| inductions | Induction | 투입구 | String(UUID) | - |

---

### 3.3 operato.logis.light (Lighting System - 5개 테이블)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 | Unique Fields |
|----------|--------------|------|---------|---------------|
| gateways | Gateway | 게이트웨이 | String(UUID) | domain_id, gw_cd |
| indicators | Indicator | 표시기 (LED) | String(UUID) | gw_cd, ind_cd |
| ind_configs | IndConfig | 표시기 설정 | String(UUID) | - |
| ind_config_sets | IndConfigSet | 표시기 설정 세트 | String(UUID) | - |
| ind_change_hists | IndChangeHist | 표시기 변경 이력 | String(UUID) | - |

---

### 3.4 xyz.elidom.sys (시스템 엔티티 - 5개 테이블)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 | Unique Fields |
|----------|--------------|------|---------|---------------|
| domains | Domain | 도메인 (Multi-tenant) | Long | name |
| users | User | 사용자 | String | login |
| settings | Setting | 설정 | String(UUID) | domain_id, name, terminal_type |
| error_logs | ErrorLog | 에러 로그 | String(UUID) | - |
| password_histories | PasswordHistory | 비밀번호 이력 | String(UUID) | - |

---

### 3.5 xyz.elidom.sec (보안 엔티티 - 8개 테이블)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 | Unique Fields |
|----------|--------------|------|---------|---------------|
| roles | Role | 역할 | String(UUID) | domain_id, name |
| permissions | Permission | 권한 | String(UUID) | - |
| users_roles | UsersRole | 사용자-역할 매핑 | String(UUID) | - |
| login_histories | LoginHistory | 로그인 이력 | String(UUID) | - |
| user_histories | UserHistory | 사용자 이력 | String(UUID) | - |
| user_role_histories | UserRoleHistory | 사용자 역할 이력 | String(UUID) | - |
| permit_urls | PermitUrl | 허용 URL | String(UUID) | - |
| request_auths | RequestAuth | 요청 인증 | String(UUID) | - |

---

### 3.6 xyz.elidom.base (기본 엔티티 - 11개 테이블)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 | Unique Fields |
|----------|--------------|------|---------|---------------|
| menus | Menu | 메뉴 | String(UUID) | domain_id, name |
| menu_columns | MenuColumn | 메뉴 컬럼 | String(UUID) | - |
| menu_buttons | MenuButton | 메뉴 버튼 | String(UUID) | - |
| menu_params | MenuParam | 메뉴 파라미터 | String(UUID) | - |
| menu_details | MenuDetail | 메뉴 상세 | String(UUID) | domain_id, name |
| menu_detail_buttons | MenuDetailButton | 메뉴 상세 버튼 | String(UUID) | - |
| menu_detail_columns | MenuDetailColumn | 메뉴 상세 컬럼 | String(UUID) | - |
| resources | Resource | 리소스 | String(UUID) | domain_id, name |
| resource_columns | ResourceColumn | 리소스 컬럼 | String(UUID) | - |
| view_columns | ViewColumn | 뷰 컬럼 | String(UUID) | - |
| grid_personalizes | GridPersonalize | 그리드 개인화 | String(UUID) | - |
| page_route_histories | PageRouteHistory | 페이지 라우트 이력 | String(UUID) | - |

---

### 3.7 xyz.elidom.core (코어 엔티티 - 8개 테이블)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 | Unique Fields |
|----------|--------------|------|---------|---------------|
| common_codes | Code | 공통 코드 | String(UUID) | domain_id, name |
| code_details | CodeDetail | 공통 코드 상세 | String(UUID) | - |
| attachments | Attachment | 첨부파일 | String(UUID) | - |
| storages | Storage | 스토리지 | String(UUID) | - |
| data_srcs | DataSrc | 데이터 소스 | String(UUID) | - |
| procedures | Procedure | 프로시저 | String(UUID) | - |
| properties | Property | 속성 | String(UUID) | domain_id, name |
| domain_apps | DomainApp | 도메인 앱 | String(UUID) | - |

---

### 3.8 xyz.elidom.mw.rabbitmq (RabbitMQ 엔티티 - 5개 테이블)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 |
|----------|--------------|------|---------|
| mw_sites | Site | 미들웨어 사이트 | String(UUID) |
| mw_trace_publish | TracePublish | 메시지 발행 추적 | String(UUID) |
| mw_trace_deliver | TraceDeliver | 메시지 전달 추적 | String(UUID) |
| mw_trace_error | TraceError | 메시지 에러 추적 | String(UUID) |
| mw_trace_dead | TraceDead | Dead Letter 추적 | String(UUID) |

---

### 3.9 xyz.anythings.sec (Anythings Security - 20개 테이블)

| 테이블명 | 엔티티 클래스 | 설명 | PK 타입 |
|----------|--------------|------|---------|
| appliances | Appliance | 기기 | String(UUID) |
| applications | Application | 애플리케이션 | String(UUID) |
| boards | Board | 보드 | String(UUID) |
| board_fonts | BoardFont | 보드 폰트 | String(UUID) |
| board_histories | BoardHistory | 보드 이력 | String(UUID) |
| board_templates | BoardTemplate | 보드 템플릿 | String(UUID) |
| granted_roles | GrantedRole | 부여된 역할 | String(UUID) |
| groups | Group | 그룹 | String(UUID) |
| invitations | Invitation | 초대 | String(UUID) |
| lite_menus | LiteMenu | 간편 메뉴 | String(UUID) |
| login_histories | LoginHistory | 로그인 이력 | String(UUID) |
| page_preferences | PagePreference | 페이지 선호도 | String(UUID) |
| partners | Partner | 파트너 | String(UUID) |
| play_groups | PlayGroup | 플레이 그룹 | String(UUID) |
| play_group_boards | PlayGroupBoard | 플레이 그룹 보드 | String(UUID) |
| privileges | Privilege | 권한 | String(UUID) |
| roles_privileges | RolesPrivilege | 역할-권한 매핑 | String(UUID) |
| users_domains | UsersDomain | 사용자-도메인 매핑 | String(UUID) |
| verification_tokens | VerificationToken | 검증 토큰 | String(UUID) |
| web_auth_credentials | WebAuthCredential | 웹 인증 자격증명 | String(UUID) |

---

## 4. 핵심 테이블 상세 명세

### 4.1 job_batches (작업 배치)

**모듈**: xyz.anythings.base
**엔티티 클래스**: JobBatch
**설명**: WCS의 핵심 작업 단위. 입고/출고 작업을 배치 단위로 관리

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 40 | UUID | PK |
| domain_id | BIGINT | NO | - | - | 도메인 ID (Multi-tenant) |
| wms_batch_no | VARCHAR | YES | 40 | - | WMS 배치 번호 |
| wcs_batch_no | VARCHAR | YES | 40 | - | WCS 배치 번호 |
| batch_group_id | VARCHAR | YES | 40 | - | 배치 그룹 ID |
| com_cd | VARCHAR | YES | 30 | - | 화주사 코드 |
| job_type | VARCHAR | NO | 20 | - | 작업 유형 (DAS/DPS/P-DAS/SMS 등) |
| biz_type | VARCHAR | YES | 10 | - | 비즈니스 유형 (B2B-IN/OUT, B2C-IN/OUT) |
| job_date | VARCHAR | NO | 10 | - | 작업 일자 (YYYY-MM-DD) |
| job_seq | VARCHAR | YES | 12 | - | 작업 차수 |
| area_cd | VARCHAR | YES | 30 | - | 구역 코드 |
| stage_cd | VARCHAR | YES | 30 | - | 스테이지 코드 |
| equip_type | VARCHAR | YES | 20 | - | 설비 유형 |
| equip_group_cd | VARCHAR | YES | 30 | - | 설비 그룹 코드 |
| equip_cd | VARCHAR | YES | 30 | - | 설비 코드 |
| equip_nm | VARCHAR | YES | 40 | - | 설비 명 |
| batch_order_qty | INTEGER | YES | - | 0 | 배치 주문 수량 |
| batch_sku_qty | INTEGER | YES | - | 0 | 배치 상품 수량 |
| batch_pcs | INTEGER | YES | - | 0 | 배치 PCS |
| result_order_qty | INTEGER | YES | - | 0 | 처리 주문 수량 |
| result_sku_qty | INTEGER | YES | - | 0 | 처리 상품 수량 |
| result_pcs | INTEGER | YES | - | 0 | 처리 PCS |
| progress_rate | FLOAT | YES | - | 0.0 | 진행율 (%) |
| uph | FLOAT | YES | - | 0.0 | UPH (Units Per Hour) |
| status | VARCHAR | YES | 10 | - | 배치 상태 (ready/running/finished/cancel) |
| instructed_at | DATETIME | YES | - | - | 작업 지시 시간 |
| finished_at | DATETIME | YES | - | - | 배치 완료 시간 |
| job_config_set_id | VARCHAR | YES | 40 | - | 작업 설정 세트 ID (FK) |
| ind_config_set_id | VARCHAR | YES | 40 | - | 표시기 설정 세트 ID (FK) |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_job_batches_0 | domain_id, job_type, job_date, job_seq, status | NO | 작업 조회 |
| ix_job_batches_1 | domain_id, wms_batch_no | NO | WMS 배치 조회 |
| ix_job_batches_2 | domain_id, batch_group_id | NO | 배치 그룹 조회 |
| ix_job_batches_3 | domain_id, equip_group_cd, equip_type, equip_cd | NO | 설비별 조회 |
| ix_job_batches_4 | domain_id, area_cd, stage_cd | NO | 구역/스테이지별 조회 |
| ix_job_batches_5 | domain_id, biz_type | NO | 비즈니스 타입별 조회 |

#### 관계

```
job_batches (1) ─── (N) orders (batch_id)
job_batches (N) ─── (1) job_config_sets (job_config_set_id)
job_batches (N) ─── (1) ind_config_sets (ind_config_set_id)
```

---

### 4.2 orders (주문)

**모듈**: xyz.anythings.base
**엔티티 클래스**: Order
**설명**: 입/출고 주문 정보. WCS의 핵심 트랜잭션 데이터

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 40 | UUID | PK |
| domain_id | BIGINT | NO | - | - | 도메인 ID |
| batch_id | VARCHAR | NO | 40 | - | 배치 ID (FK → job_batches) |
| wms_batch_no | VARCHAR | YES | 40 | - | WMS 배치 번호 |
| wcs_batch_no | VARCHAR | YES | 40 | - | WCS 배치 번호 |
| job_date | VARCHAR | NO | 10 | - | 작업 일자 |
| job_seq | VARCHAR | YES | 10 | - | 작업 차수 |
| job_type | VARCHAR | YES | 20 | - | 작업 유형 |
| biz_type | VARCHAR | YES | 10 | - | 비즈니스 유형 |
| order_no | VARCHAR | NO | 40 | - | 주문 번호 |
| order_line_no | VARCHAR | YES | 40 | - | 주문 라인 번호 |
| box_id | VARCHAR | YES | 40 | - | 박스 ID |
| invoice_id | VARCHAR | YES | 40 | - | 송장 번호 |
| box_type_cd | VARCHAR | YES | 30 | - | 박스 유형 코드 |
| com_cd | VARCHAR | YES | 30 | - | 화주사 코드 |
| area_cd | VARCHAR | YES | 30 | - | 구역 코드 |
| stage_cd | VARCHAR | YES | 30 | - | 스테이지 코드 |
| equip_type | VARCHAR | YES | 30 | - | 설비 유형 |
| equip_cd | VARCHAR | YES | 30 | - | 설비 코드 |
| shop_cd | VARCHAR | YES | 30 | - | 판매처 코드 |
| shop_nm | VARCHAR | YES | 40 | - | 판매처 명 |
| sku_cd | VARCHAR | NO | 30 | - | 상품 코드 (FK → sku) |
| sku_barcd | VARCHAR | YES | 30 | - | 상품 바코드 |
| sku_nm | VARCHAR | YES | 200 | - | 상품명 |
| order_qty | INTEGER | NO | - | 0 | 주문 수량 |
| picked_qty | INTEGER | YES | - | 0 | 피킹 수량 |
| boxed_qty | INTEGER | YES | - | 0 | 박스 수량 |
| cancel_qty | INTEGER | YES | - | 0 | 취소 수량 |
| status | VARCHAR | YES | 10 | - | 주문 상태 (wait/picking/picked/boxed/cancel) |
| attr01 ~ attr05 | VARCHAR | YES | 100 | - | 확장 필드 1~5 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_orders_0 | domain_id, batch_id, order_no | NO | 배치별 주문 조회 |
| ix_orders_1 | domain_id, wms_batch_no, order_no | NO | WMS 배치별 조회 |
| ix_orders_2 | domain_id, job_date, job_seq | NO | 일자별 조회 |
| ix_orders_3 | domain_id, box_id | NO | 박스별 조회 |
| ix_orders_4 | domain_id, equip_cd, status | NO | 설비별 상태 조회 |
| ix_orders_5 | domain_id, sku_cd | NO | 상품별 조회 |
| ix_orders_6 | domain_id, shop_cd | NO | 판매처별 조회 |
| ix_orders_7 | domain_id, status | NO | 상태별 조회 |

#### 관계

```
orders (N) ─── (1) job_batches (batch_id)
orders (N) ─── (1) sku (sku_cd)
```

---

### 4.3 sku (상품 마스터)

**모듈**: xyz.anythings.base
**엔티티 클래스**: SKU
**설명**: 상품 마스터 정보

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 40 | UUID | PK |
| domain_id | BIGINT | NO | - | - | 도메인 ID |
| com_cd | VARCHAR | NO | 30 | - | 화주사 코드 |
| sku_cd | VARCHAR | NO | 30 | - | 상품 코드 |
| sku_nm | VARCHAR | NO | 200 | - | 상품명 |
| sku_barcd | VARCHAR | YES | 30 | - | 상품 바코드 1 |
| sku_barcd2 | VARCHAR | YES | 30 | - | 상품 바코드 2 |
| sku_barcd3 | VARCHAR | YES | 30 | - | 상품 바코드 3 |
| sku_barcd4 | VARCHAR | YES | 30 | - | 상품 바코드 4 |
| box_barcd | VARCHAR | YES | 30 | - | 박스 바코드 |
| box_in_qty | INTEGER | YES | - | 1 | 박스 입수 수량 |
| sku_price | FLOAT | YES | - | 0.0 | 상품 가격 |
| sku_len | FLOAT | YES | - | 0.0 | 상품 길이 (cm) |
| sku_wd | FLOAT | YES | - | 0.0 | 상품 너비 (cm) |
| sku_ht | FLOAT | YES | - | 0.0 | 상품 높이 (cm) |
| sku_wt | FLOAT | YES | - | 0.0 | 상품 중량 (g) |
| cell_cd | VARCHAR | YES | 30 | - | 셀 코드 (고정 로케이션) |
| image_url | VARCHAR | YES | 255 | - | 이미지 URL |
| active_flag | BOOLEAN | YES | - | true | 활성 여부 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_sku_0 | domain_id, com_cd, sku_cd | YES | 상품 코드 unique |
| ix_sku_1 | domain_id, sku_barcd | NO | 바코드 조회 |
| ix_sku_2 | domain_id, box_barcd | NO | 박스 바코드 조회 |
| ix_sku_3 | domain_id, sku_nm | NO | 상품명 조회 |

#### 관계

```
sku (1) ─── (N) orders (sku_cd)
sku (1) ─── (N) stocks (sku_cd)
```

---

### 4.4 stocks (재고)

**모듈**: xyz.anythings.base
**엔티티 클래스**: Stock
**설명**: 재고 관리 - 셀 단위 재고 정보

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 40 | UUID | PK |
| domain_id | BIGINT | NO | - | - | 도메인 ID |
| equip_type | VARCHAR | NO | 20 | - | 설비 유형 |
| equip_cd | VARCHAR | NO | 30 | - | 설비 코드 |
| cell_cd | VARCHAR | NO | 30 | - | 셀 코드 |
| com_cd | VARCHAR | YES | 30 | - | 화주사 코드 |
| sku_cd | VARCHAR | YES | 30 | - | 상품 코드 |
| stock_qty | INTEGER | YES | - | 0 | 재고 수량 |
| load_qty | INTEGER | YES | - | 0 | 적치 수량 |
| alloc_qty | INTEGER | YES | - | 0 | 할당 수량 |
| picked_qty | INTEGER | YES | - | 0 | 피킹 수량 |
| min_stock_qty | INTEGER | YES | - | 0 | 최소 재고 수량 |
| max_stock_qty | INTEGER | YES | - | 9999 | 최대 재고 수량 |
| fixed_flag | BOOLEAN | YES | - | false | 고정 여부 |
| active_flag | BOOLEAN | YES | - | true | 활성 여부 |
| last_tran_cd | VARCHAR | YES | 30 | - | 마지막 트랜잭션 코드 |
| last_tran_time | DATETIME | YES | - | - | 마지막 트랜잭션 시간 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_stocks_0 | domain_id, equip_type, equip_cd, cell_cd | YES | 셀별 unique |
| ix_stocks_1 | domain_id, sku_cd | NO | 상품별 재고 조회 |
| ix_stocks_2 | domain_id, equip_cd, active_flag | NO | 설비별 활성 재고 |

#### 트랜잭션 타입

| 코드 | 설명 |
|------|------|
| create | 재고 생성 |
| in | 입고 |
| out | 출고 |
| adjust | 조정 |
| supply | 보충 |
| pick | 피킹 |
| assign | 할당 |
| update | 업데이트 |

---

### 4.5 domains (도메인)

**모듈**: xyz.elidom.sys
**엔티티 클래스**: Domain
**설명**: Multi-tenant 도메인 정보

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | BIGINT | NO | - | AUTO_INCREMENT | PK |
| name | VARCHAR | NO | 64 | - | 도메인명 (unique) |
| subdomain | VARCHAR | NO | 64 | - | 서브도메인 |
| system_flag | BOOLEAN | YES | - | false | 시스템 도메인 여부 |
| brand_name | VARCHAR | YES | 64 | - | 브랜드명 |
| timezone | VARCHAR | YES | 64 | UTC | 타임존 |
| locale | VARCHAR | YES | 10 | en_US | 로케일 |
| mw_site_cd | VARCHAR | YES | 20 | - | 미들웨어 사이트 코드 |
| active_flag | BOOLEAN | YES | - | true | 활성 여부 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_domains_0 | name | YES | 도메인명 unique |
| ix_domains_1 | subdomain | NO | 서브도메인 조회 |

---

### 4.6 users (사용자)

**모듈**: xyz.elidom.sys
**엔티티 클래스**: User
**설명**: 사용자 정보

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 32 | - | PK (사용자 ID) |
| login | VARCHAR | NO | 25 | - | 로그인 ID (unique) |
| email | VARCHAR | YES | 32 | - | 이메일 |
| encrypted_password | VARCHAR | NO | 80 | - | 암호화된 비밀번호 |
| name | VARCHAR | NO | 30 | - | 사용자명 |
| super_user | BOOLEAN | YES | - | false | 슈퍼유저 여부 |
| admin_flag | BOOLEAN | YES | - | false | 관리자 여부 |
| active_flag | BOOLEAN | YES | - | true | 활성 여부 |
| locale | VARCHAR | YES | 10 | - | 로케일 |
| timezone | VARCHAR | YES | 64 | - | 타임존 |
| account_type | VARCHAR | YES | 20 | - | 계정 유형 |
| password_expire_date | VARCHAR | YES | 20 | - | 비밀번호 만료일 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_users_0 | login | YES | 로그인 ID unique |
| ix_users_1 | email | NO | 이메일 조회 |

---

### 4.7 menus (메뉴)

**모듈**: xyz.elidom.base
**엔티티 클래스**: Menu
**설명**: 시스템 메뉴 정보 (계층 구조)

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 40 | UUID | PK |
| domain_id | BIGINT | NO | - | - | 도메인 ID |
| name | VARCHAR | NO | 64 | - | 메뉴명 (unique within domain) |
| parent_id | VARCHAR | YES | 40 | - | 부모 메뉴 ID (self FK) |
| menu_type | VARCHAR | YES | 20 | - | 메뉴 유형 |
| category | VARCHAR | YES | 20 | - | 카테고리 |
| routing | VARCHAR | YES | 64 | - | 라우팅 경로 |
| resource_type | VARCHAR | YES | 15 | - | 리소스 타입 |
| resource_name | VARCHAR | YES | 64 | - | 리소스명 |
| resource_url | VARCHAR | YES | 255 | - | 리소스 URL |
| icon_path | VARCHAR | YES | 100 | - | 아이콘 경로 |
| role_id | VARCHAR | YES | 40 | - | 역할 ID (FK) |
| rank | INTEGER | YES | - | 0 | 정렬 순서 |
| hidden_flag | BOOLEAN | YES | - | false | 숨김 여부 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_menus_0 | domain_id, name | YES | 메뉴명 unique |
| ix_menus_1 | parent_id | NO | 부모 메뉴 조회 |
| ix_menus_2 | domain_id, category | NO | 카테고리별 조회 |

#### 관계

```
menus (self-reference)
   │
   ├─── (N) menu_columns
   ├─── (N) menu_buttons
   └─── (N) menu_params
```

---

### 4.8 sorters (자동 분류기)

**모듈**: operato.logis.sms
**엔티티 클래스**: Sorter
**설명**: 자동 분류기 (Sorter) 설비 정보

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 40 | UUID | PK |
| domain_id | BIGINT | NO | - | - | 도메인 ID |
| sorter_cd | VARCHAR | NO | 30 | - | Sorter 코드 |
| sorter_nm | VARCHAR | YES | 40 | - | Sorter 명 |
| com_cd | VARCHAR | YES | 30 | - | 화주사 코드 |
| area_cd | VARCHAR | YES | 30 | - | 구역 코드 |
| stage_cd | VARCHAR | YES | 30 | - | 스테이지 코드 |
| max_chute_cnt | INTEGER | YES | - | 0 | 최대 슈트 수 |
| max_induction_cnt | INTEGER | YES | - | 0 | 최대 투입구 수 |
| max_speed | FLOAT | YES | - | 0.0 | 최대 속도 (m/s) |
| max_throughput | INTEGER | YES | - | 0 | 최대 처리량 (개/시간) |
| active_flag | BOOLEAN | YES | - | true | 활성 여부 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_sorters_0 | domain_id, sorter_cd | YES | Sorter 코드 unique |

---

### 4.9 gateways (게이트웨이)

**모듈**: operato.logis.light
**엔티티 클래스**: Gateway
**설명**: 표시기 게이트웨이 정보

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 40 | UUID | PK |
| domain_id | BIGINT | NO | - | - | 도메인 ID |
| gw_cd | VARCHAR | NO | 30 | - | 게이트웨이 코드 |
| gw_nm | VARCHAR | YES | 40 | - | 게이트웨이 명 |
| gw_ip | VARCHAR | YES | 20 | - | 게이트웨이 IP |
| gw_port | INTEGER | YES | - | 0 | 게이트웨이 포트 |
| com_cd | VARCHAR | YES | 30 | - | 화주사 코드 |
| area_cd | VARCHAR | YES | 30 | - | 구역 코드 |
| stage_cd | VARCHAR | YES | 30 | - | 스테이지 코드 |
| max_ind_cnt | INTEGER | YES | - | 0 | 최대 표시기 수 |
| active_flag | BOOLEAN | YES | - | true | 활성 여부 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_gateways_0 | domain_id, gw_cd | YES | 게이트웨이 코드 unique |

---

### 4.10 indicators (표시기)

**모듈**: operato.logis.light
**엔티티 클래스**: Indicator
**설명**: 표시기 (LED) 정보

#### 테이블 컬럼

| 컬럼명 | 타입 | Nullable | 길이 | Default | 설명 |
|--------|------|----------|------|---------|------|
| id | VARCHAR | NO | 40 | UUID | PK |
| domain_id | BIGINT | NO | - | - | 도메인 ID |
| gw_cd | VARCHAR | NO | 30 | - | 게이트웨이 코드 (FK) |
| ind_cd | VARCHAR | NO | 30 | - | 표시기 코드 |
| ind_nm | VARCHAR | YES | 40 | - | 표시기 명 |
| com_cd | VARCHAR | YES | 30 | - | 화주사 코드 |
| area_cd | VARCHAR | YES | 30 | - | 구역 코드 |
| stage_cd | VARCHAR | YES | 30 | - | 스테이지 코드 |
| equip_cd | VARCHAR | YES | 30 | - | 설비 코드 |
| cell_cd | VARCHAR | YES | 30 | - | 셀 코드 |
| ind_type | VARCHAR | YES | 20 | - | 표시기 유형 |
| status | VARCHAR | YES | 10 | - | 상태 (on/off/blink) |
| active_flag | BOOLEAN | YES | - | true | 활성 여부 |
| creator_id | VARCHAR | YES | 40 | - | 생성자 ID |
| updater_id | VARCHAR | YES | 40 | - | 수정자 ID |
| created_at | DATETIME | NO | - | NOW() | 생성 일시 |
| updated_at | DATETIME | NO | - | NOW() | 수정 일시 |

#### 인덱스

| 인덱스명 | 컬럼 | Unique | 설명 |
|---------|------|--------|------|
| PRIMARY | id | YES | PK |
| ix_indicators_0 | gw_cd, ind_cd | YES | 표시기 코드 unique |
| ix_indicators_1 | domain_id, cell_cd | NO | 셀별 표시기 조회 |

---

## 5. 테이블 관계도

### 5.1 핵심 비즈니스 관계

```
┌─────────────┐
│   domains   │
└──────┬──────┘
       │ (1:N)
       ▼
┌─────────────┐      (1:N)      ┌─────────────┐
│    users    │ ◄─────────────── │ users_roles │
└─────────────┘                  └──────┬──────┘
                                        │ (N:1)
                                        ▼
                                 ┌─────────────┐
                                 │    roles    │
                                 └─────────────┘

┌──────────────────┐
│   job_batches    │
└────────┬─────────┘
         │ (1:N)
         ▼
  ┌─────────────┐      (N:1)      ┌─────────────┐
  │   orders    │ ─────────────────▶│     sku     │
  └─────────────┘                  └─────────────┘
         │ (N:1)
         ▼
  ┌─────────────┐
  │  box_packs  │
  └─────────────┘

┌─────────────┐      (N:1)      ┌─────────────┐
│   stocks    │ ─────────────────▶│    cells    │
└─────────────┘                  └──────┬──────┘
                                        │ (N:1)
                                        ▼
                                 ┌─────────────┐
                                 │    racks    │
                                 └─────────────┘
```

### 5.2 설비 관계

```
┌──────────────┐
│  gateways    │ (operato.logis.light)
└──────┬───────┘
       │ (1:N)
       ▼
┌──────────────┐      (N:1)      ┌──────────────┐
│  indicators  │ ─────────────────▶│    cells    │
└──────────────┘                  └──────────────┘

┌──────────────┐
│   sorters    │ (operato.logis.sms)
└──────┬───────┘
       │ (1:N)
       ├────────────────┐
       ▼                ▼
┌──────────────┐  ┌──────────────┐
│    chutes    │  │  inductions  │
└──────────────┘  └──────────────┘
```

### 5.3 시스템 관계

```
┌─────────────┐
│    menus    │ (self-reference)
└──────┬──────┘
       │ parent_id
       ▼
┌─────────────┐
│    menus    │
└──────┬──────┘
       │ (1:N)
       ├───────────────────────┐
       │                       │
       ▼                       ▼
┌──────────────┐        ┌──────────────┐
│ menu_columns │        │ menu_buttons │
└──────────────┘        └──────────────┘
```

---

## 6. 인덱스 전략

### 6.1 인덱스 명명 규칙

```
ix_{table_name}_{sequence_number}
```

**예시**:
- `ix_job_batches_0`
- `ix_orders_1`
- `ix_sku_2`

### 6.2 공통 인덱스 패턴

| 패턴 | 컬럼 구성 | 용도 | 예시 |
|------|----------|------|------|
| Domain 필터링 | domain_id, {business_key} | Multi-tenant 조회 | ix_job_batches_0 |
| 상태 필터링 | domain_id, status, {key} | 상태별 조회 | ix_orders_7 |
| 날짜 필터링 | job_date, job_seq | 일자별 조회 | ix_orders_2 |
| 설비 필터링 | equip_type, equip_cd | 설비별 조회 | ix_job_batches_3 |
| Unique 제약 | domain_id, {unique_key} | 중복 방지 | ix_sku_0 |

### 6.3 복합 인덱스 설계 원칙

1. **선택도(Selectivity) 높은 컬럼을 앞에**
   - ✅ `(domain_id, wms_batch_no)`
   - ❌ `(status, domain_id)` (status 값은 제한적)

2. **WHERE, JOIN에 자주 사용되는 컬럼 우선**
   - `domain_id`는 거의 모든 쿼리에 포함

3. **커버링 인덱스 고려**
   - SELECT 대상 컬럼까지 인덱스에 포함

---

## 7. 데이터 타입 매핑

### 7.1 Java ↔ SQL 타입 매핑

| Java 타입 | SQL 타입 | 일반 길이 | 용도 | 예시 컬럼 |
|-----------|----------|----------|------|----------|
| String | VARCHAR | 10-30 | 코드 | sku_cd, com_cd, area_cd |
| String | VARCHAR | 40 | 명칭 | sku_nm, equip_nm |
| String | VARCHAR | 100-255 | 설명, URL | description, image_url |
| Long | BIGINT | - | 숫자 ID | domain_id |
| Integer | INTEGER | - | 수량, 카운트 | stock_qty, order_qty |
| Float/Double | DECIMAL(19,4) | - | 중량, 가격, 비율 | sku_wt, sku_price, uph |
| Boolean | BOOLEAN/TINYINT | 1 | 플래그 | active_flag, fixed_flag |
| Date | DATETIME | - | 일시 | created_at, updated_at |
| String (Date) | VARCHAR | 10 | 날짜 문자열 | job_date (YYYY-MM-DD) |

### 7.2 특수 타입

| 용도 | Java 타입 | SQL 타입 | 길이 | 예시 |
|------|-----------|----------|------|------|
| UUID | String | VARCHAR | 40 | id (PK) |
| 암호화 비밀번호 | String | VARCHAR | 80 | encrypted_password |
| 이메일 | String | VARCHAR | 32-100 | email |
| IP 주소 | String | VARCHAR | 20 | gw_ip |
| JSON | String | TEXT | - | config_json |
| 확장 필드 | String | VARCHAR | 100 | attr01~05 |

---

## 8. 확장 필드 (Extension Fields)

많은 테이블에서 유연성을 위해 확장 필드를 제공합니다.

### 8.1 확장 필드 패턴

| 필드명 | 타입 | 길이 | 용도 |
|--------|------|------|------|
| attr01 ~ attr05 | VARCHAR | 100 | 추가 속성 1~5 |
| ext01 ~ ext05 | VARCHAR | 100 | 확장 필드 1~5 |
| flag01 ~ flag05 | BOOLEAN | 1 | 플래그 1~5 |

### 8.2 적용 테이블

- `orders` (attr01~05)
- `job_batches` (확장 필드 없음, 고정 스키마)
- `sku` (attr01~05)

---

## 9. 통계 정보

### 9.1 모듈별 테이블 수

| 모듈 | 테이블 수 | 주요 테이블 |
|------|----------|------------|
| xyz.anythings.base | 56 | job_batches, orders, sku, stocks |
| operato.logis.sms | 4 | sorters, chutes |
| operato.logis.light | 5 | gateways, indicators |
| xyz.anythings.gw | 6 | gateways, indicators (중복) |
| xyz.elidom.sys | 5 | domains, users |
| xyz.elidom.sec | 8 | roles, permissions |
| xyz.elidom.base | 11 | menus, resources |
| xyz.elidom.core | 8 | codes, attachments |
| xyz.elidom.mw.rabbitmq | 5 | mw_trace_* |
| xyz.anythings.sec | 20 | boards, applications |
| **합계** | **150+** | - |

### 9.2 PK 타입 분포

| PK 타입 | 비율 | 주요 사용처 |
|---------|------|-----------|
| String (UUID) | ~95% | 대부분의 비즈니스 테이블 |
| Long (자동증가) | ~5% | domains, sites 등 시스템 테이블 |

---

## 10. DDL 생성 예시

### 10.1 job_batches 테이블

```sql
CREATE TABLE job_batches (
    id VARCHAR(40) NOT NULL,
    domain_id BIGINT NOT NULL,
    wms_batch_no VARCHAR(40),
    wcs_batch_no VARCHAR(40),
    batch_group_id VARCHAR(40),
    com_cd VARCHAR(30),
    job_type VARCHAR(20) NOT NULL,
    biz_type VARCHAR(10),
    job_date VARCHAR(10) NOT NULL,
    job_seq VARCHAR(12),
    area_cd VARCHAR(30),
    stage_cd VARCHAR(30),
    equip_type VARCHAR(20),
    equip_group_cd VARCHAR(30),
    equip_cd VARCHAR(30),
    equip_nm VARCHAR(40),
    batch_order_qty INTEGER DEFAULT 0,
    batch_sku_qty INTEGER DEFAULT 0,
    batch_pcs INTEGER DEFAULT 0,
    result_order_qty INTEGER DEFAULT 0,
    result_sku_qty INTEGER DEFAULT 0,
    result_pcs INTEGER DEFAULT 0,
    progress_rate FLOAT DEFAULT 0.0,
    uph FLOAT DEFAULT 0.0,
    status VARCHAR(10),
    instructed_at DATETIME,
    finished_at DATETIME,
    job_config_set_id VARCHAR(40),
    ind_config_set_id VARCHAR(40),
    creator_id VARCHAR(40),
    updater_id VARCHAR(40),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX ix_job_batches_0 (domain_id, job_type, job_date, job_seq, status),
    INDEX ix_job_batches_1 (domain_id, wms_batch_no),
    INDEX ix_job_batches_2 (domain_id, batch_group_id),
    INDEX ix_job_batches_3 (domain_id, equip_group_cd, equip_type, equip_cd),
    INDEX ix_job_batches_4 (domain_id, area_cd, stage_cd),
    INDEX ix_job_batches_5 (domain_id, biz_type),
    FOREIGN KEY (job_config_set_id) REFERENCES job_config_sets(id),
    FOREIGN KEY (ind_config_set_id) REFERENCES ind_config_sets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 11. 참고 문서

- [operato-wcs-development-plan.md](../plans/operato-wcs-development-plan.md) — 프로젝트 개발 계획서
- [operato-wcs-wbs.md](../plans/operato-wcs-wbs.md) — WBS (Work Breakdown Structure)
- [CLAUDE.md](/Users/shortstop/Git/operato-wcs-ai/CLAUDE.md) — 프로젝트 컨벤션

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 2026-03-01 | 1.0.0 | 데이터베이스 명세서 최초 작성 | Claude Sonnet 4.5 |

---

## 부록 A: 엔티티 전체 목록

(200개 엔티티 클래스의 전체 목록은 별도 파일로 관리)

**파일 위치**: `docs/design/database-entity-list.md`

---

**작성 완료일**: 2026-03-01
**문서 상태**: 초안 (Draft)
**검토 필요**: ERD 다이어그램 추가, FK 관계 상세화
