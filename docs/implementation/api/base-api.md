# Base 모듈 API 목록 (Base Module API List)

## 에어리어 API (`AreaController`)
* **기본 경로(Base Path)**: `/rest/areas`
* **설명**: 공간/구역(Area) 기분 정보를 관리하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Area input` | `Area` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Area> list` | `Boolean` |

## BoxPack API (`BoxPackController`)
* **기본 경로(Base Path)**: `/rest/box_packs`
* **설명**: DpsBoxPack Service API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |

## 박스유형 API (`BoxTypeController`)
* **기본 경로(Base Path)**: `/rest/box_types`
* **설명**: 상자 유형 기준 정보를 관리하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `BoxType input` | `BoxType` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<BoxType> list` | `Boolean` |

## 셀 API (`CellController`)
* **기본 경로(Base Path)**: `/rest/cells`
* **설명**: 로케이션(Cell) 기준 정보를 관리하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Cell input` | `Cell` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Cell> list` | `Boolean` |

## 고객사 API (`CompanyController`)
* **기본 경로(Base Path)**: `/rest/companies`
* **설명**: 고객사/협력사 정보를 관리하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Company input` | `Company` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Company> list` | `Boolean` |

## DeviceProcess API (`DeviceProcessController`)
* **기본 경로(Base Path)**: `/rest/device_process`
* **설명**: Device Process Controller API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| PUT | `/reprint/box_label` | Reprint Box Label | [Query] `stage_cd`(=)<br/>`= true` | `BaseResponse` |

## DeviceProfile API (`DeviceProfileController`)
* **기본 경로(Base Path)**: `/rest/device_profiles`
* **설명**: 디바이스 프로필 및 구성을 관리하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `DeviceProfile input` | `DeviceProfile` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<DeviceProfile> list` | `Boolean` |

## 설비그룹 API (`EquipGroupController`)
* **기본 경로(Base Path)**: `/rest/equip_groups`
* **설명**: 설비 그룹 기준 정보를 관리하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `EquipGroup input` | `EquipGroup` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<EquipGroup> list` | `Boolean` |

## IndicatorTest API (`IndicatorTestController`)
* **기본 경로(Base Path)**: `/rest/indicator_test`
* **설명**: Indicator Test Service API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| POST | `/unit_test` | Indicator Unit Test | [Body] `IndTest indTest` | `Map<String, Object>` |

## 송장 API (`InvoiceController`)
* **기본 경로(Base Path)**: `/rest/invoices`
* **설명**: 송장 정보 관리 및 생성을 위한 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Invoice input` | `Invoice` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Invoice> list` | `Boolean` |
| POST | `/create_by_range` | Create Invoices by range | [Body] `Map<String, Object> range` | `Map<String, Object>` |

## 작업배치 API (`JobBatchController`)
* **기본 경로(Base Path)**: `/rest/job_batches`
* **설명**: 작업 배치(Wave 등) 관리 및 진행률, 지시 처리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `JobBatch input` | `JobBatch` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<JobBatch> list` | `Boolean` |
| GET | `/daily_progress_rate` | Daily progress rate | [Query] `stage_cd`(=)<br/>`= true` | `BatchProgressRate` |
| POST | `/receive_batches/start` | Start to receive batch orders | [Body] `BatchReceipt summary` | `BatchReceipt` |
| POST | `/instruct/batches` |  | [Body] `= true` | `Map<String, Object>` |
| POST | `/instruct/total_pickings` |  | [Body] `= true` | `Map<String, Object>` |

## JobConfigSet API (`JobConfigSetController`)
* **기본 경로(Base Path)**: `/rest/job_config_set`
* **설명**: 작업 설정 프로필 셋 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `JobConfigSet input` | `JobConfigSet` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<JobConfigSet> list` | `Boolean` |

## JobInput API (`JobInputController`)
* **기본 경로(Base Path)**: `/rest/job_inputs`
* **설명**: 작업 투입 내역을 관리하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `JobInput input` | `JobInput` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<JobInput> list` | `Boolean` |

## 작업인스턴스 API (`JobInstanceController`)
* **기본 경로(Base Path)**: `/rest/job_instances`
* **설명**: 작업 인스턴스(상세 내역)를 관리하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `JobInstance input` | `JobInstance` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<JobInstance> list` | `Boolean` |

## 키오스크 API (`KioskController`)
* **기본 경로(Base Path)**: `/rest/kiosks`
* **설명**: 키오스크 장비 관리 및 설정 변경 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Kiosk input` | `Kiosk` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Kiosk> list` | `Boolean` |
| PUT | `/update/setting` | 설정 업데이트 | `HttpServletRequest req`<br/>`HttpServletResponse res`<br/>[Query] `equip_type`(=)<br/>`= true` | `Kiosk` |

## 주문 API (`OrderController`)
* **기본 경로(Base Path)**: `/rest/orders`
* **설명**: 주문 정보 관리, 취소 및 라벨 정보 조회 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Order input` | `Order` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Order> list` | `Boolean` |
| GET | `/order_labels` | Find detail by master ID | [Path] `id`(String) | `OrderLabel` |

## OrderLabel API (`OrderLabelController`)
* **기본 경로(Base Path)**: `/rest/order_labels`
* **설명**: Order Label Service API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `OrderLabel input` | `OrderLabel` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<OrderLabel> list` | `Boolean` |

## OrderPreprocess API (`OrderPreprocessController`)
* **기본 경로(Base Path)**: `/rest/order_preprocesses`
* **설명**: 주문 전처리(출고 전 분류 내역 등) 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `OrderPreprocess input` | `OrderPreprocess` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<OrderPreprocess> list` | `Boolean` |
| GET | `/index_all` | Search All By Search Conditions | [Query] `select`(=)<br/>`= false` | `Map<String, ?>` |

## OrderSampler API (`OrderSamplerController`)
* **기본 경로(Base Path)**: `/rest/order_samplers`
* **설명**: 주문 샘플러 처리 및 조회 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `OrderSampler input` | `OrderSampler` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<OrderSampler> list` | `Boolean` |

## PDA API (`PDAController`)
* **기본 경로(Base Path)**: `/rest/pdas`
* **설명**: PDA 기기 관리 및 설정 업데이트 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `PDA input` | `PDA` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<PDA> list` | `Boolean` |
| PUT | `/update/setting` | 설정 업데이트 | `HttpServletRequest req`<br/>`HttpServletResponse res`<br/>[Query] `equip_type`(=)<br/>`= true` | `PDA` |

## PopularRank API (`PopularRankController`)
* **기본 경로(Base Path)**: `/rest/popular_ranks`
* **설명**: 인기상품 랭킹/순위 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `PopularRank input` | `PopularRank` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<PopularRank> list` | `Boolean` |

## 랙 API (`RackController`)
* **기본 경로(Base Path)**: `/rest/racks`
* **설명**: 랙(Rack) 구조물 기준 정보 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Rack input` | `Rack` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Rack> list` | `Boolean` |

## 재작업 API (`ReworkController`)
* **기본 경로(Base Path)**: `/rest/reworks`
* **설명**: 재작업(Rework) 처리 지시 및 상세 내역 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Rework input` | `Rework` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Rework> list` | `Boolean` |

## SKU API (`SKUController`)
* **기본 경로(Base Path)**: `/rest/sku`
* **설명**: 상품(SKU) 마스터 정보 관리 및 임포트 체크 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `SKU input` | `SKU` |
| POST | `/check_import` | Check Before Import | [Body] `List<SKU> list` | `List<SKU>` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<SKU> list` | `Boolean` |

## SerialInstance API (`SerialInstanceController`)
* **기본 경로(Base Path)**: `/rest/serial_instances`
* **설명**: 시리얼 단품 인스턴스 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `SerialInstance input` | `SerialInstance` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<SerialInstance> list` | `Boolean` |

## 쇼핑몰/매장 API (`ShopController`)
* **기본 경로(Base Path)**: `/rest/shops`
* **설명**: 쇼핑몰, 매장(Shop) 기준 정보 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Shop input` | `Shop` |
| POST | `/check_import` | Check Before Import | [Body] `List<Shop> list` | `List<Shop>` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Shop> list` | `Boolean` |

## 스테이지 API (`StageController`)
* **기본 경로(Base Path)**: `/rest/stages`
* **설명**: 스테이지(구역 그룹, 작업 단위 구역 등) 관리 및 실시간 반영 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Stage input` | `Stage` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Stage> list` | `Boolean` |

## 스테이션 API (`StationController`)
* **기본 경로(Base Path)**: `/rest/stations`
* **설명**: 스테이션(작업대장) 기준 정보 및 구성 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Station input` | `Station` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Station> list` | `Boolean` |

## 재고 API (`StockController`)
* **기본 경로(Base Path)**: `/rest/stocks`
* **설명**: 재고(Stock) 조회 및 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Stock input` | `Stock` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Stock> list` | `Boolean` |

## StockHist API (`StockHistController`)
* **기본 경로(Base Path)**: `/rest/stock_hists`
* **설명**: 재고 이력(History) 조회 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `StockHist input` | `StockHist` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<StockHist> list` | `Boolean` |

## Stocktaking API (`StocktakingController`)
* **기본 경로(Base Path)**: `/rest/stocktakings`
* **설명**: 재고 실사(재고 조사) 관리 및 처리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Stocktaking input` | `Stocktaking` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Stocktaking> list` | `Boolean` |
| POST | `/start` | Start StockTaking (재고 실사 시작) | [Body] `List<String> rackCdList` | `Map<String, Object>` |
| PUT | `/finish` | Finish StockTaking By Multiple (재고 실사 완료) | [Body] `List<String> stockTakingIdList` | `Map<String, Object>` |

## 태블릿 API (`TabletController`)
* **기본 경로(Base Path)**: `/rest/tablets`
* **설명**: 태블릿 장비 관리 및 설정 변경 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `Tablet input` | `Tablet` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<Tablet> list` | `Boolean` |
| PUT | `/update/setting` | 설정 업데이트 | `HttpServletRequest req`<br/>`HttpServletResponse res`<br/>[Query] `equip_type`(=)<br/>`= true` | `Tablet` |

## TotalPicking API (`TotalPickingController`)
* **기본 경로(Base Path)**: `/rest/total_pickings`
* **설명**: 토탈 피킹(총량 피킹) 작업 및 결과 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `TotalPicking input` | `TotalPicking` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<TotalPicking> list` | `Boolean` |

## TrayBox API (`TrayBoxController`)
* **기본 경로(Base Path)**: `/rest/tray_boxes`
* **설명**: 트레이 박스/용기 기준 정보 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `TrayBox input` | `TrayBox` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<TrayBox> list` | `Boolean` |

## UnitTest API (`UnitTestController`)
* **기본 경로(Base Path)**: `/rest/unit_test`
* **설명**: Unit Test

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/current_time` | 데이터베이스 기준 현재 시간 조회 | - | `Map<String, Object>` |

## 작업셀 API (`WorkCellController`)
* **기본 경로(Base Path)**: `/rest/work_cells`
* **설명**: 작업 셀(스테이션 부속 단위) 관리 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| GET | `/` | 조건에 따른 조회 (페이징) | [Query] `page`(=)<br/>`= false` | `Page<?>` |
| POST | `/` | 신규 생성 | [Body] `WorkCell input` | `WorkCell` |
| POST | `/update_multiple` | 다건 일괄 생성/수정/삭제 | [Body] `List<WorkCell> list` | `Boolean` |

