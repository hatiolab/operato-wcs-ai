# Operato WCS REST API 명세서 (Draft)

## 1. 개요 (Overview)

본 문서는 Operato WCS(Warehouse Control System)의 클라이언트(설비 PC, PDA, KIOSK, Manager Web)와 백엔드 서버 간의 통신을 위한 RESTful API 명세입니다.

- **Base URL**: `https://api.operato-wcs.com/v1` (또는 고객사 도메인)
- **Data Format**: `application/json`
- **Authentication**: JWT (JSON Web Token) via HTTP `Authorization: Bearer <token>` 헤더

### 1.1 HTTP 상태 코드 규약

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | 요청 성공 |
| 201 | Created | 리소스 생성 성공 |
| 400 | Bad Request | 잘못된 요청파라미터 (유효성 검증 실패) |
| 401 | Unauthorized | 인증 실패 (토큰 없음 또는 만료) |
| 403 | Forbidden | 권한 없음 (해당 자원에 대한 접근 불가) |
| 404 | Not Found | 리소스를 찾을 수 없음 |
| 409 | Conflict | 비즈니스 로직 충돌 (예: 이미 처리된 주문) |
| 500 | Internal Error | 서버 내부 오류 |

### 1.2 공통 응답 포맷 (Common Response Format)

**성공 (Success)**
```json
{
  "success": true,
  "data": { ... }, // 객체 또는 배열
  "message": "요청이 성공적으로 처리되었습니다."
}
```

**실패 (Error)**
```json
{
  "success": false,
  "error": {
    "code": "ERR_INVALID_STATUS",
    "message": "해당 작업은 이미 완료 처리되었습니다.",
    "details": [ ... ] // 필드 유효성 에러 등 상세 내용
  }
}
```

---

## 2. 인증 및 권한 (Authentication & Authorization)

### 2.1 로그인 (Login)
- **Endpoint**: `POST /auth/login`
- **Description**: 사용자(작업자/관리자) 로그인을 수행하고 JWT 토큰을 발급받습니다.
- **Request Body**:
  ```json
  {
    "userId": "admin_user",
    "password": "password123",
    "deviceType": "WEB" // WEB, PDA, KIOSK, PC
  }
  ```
- **Response**:
  ```json
  {
    "token": "eyJhbG...",
    "refreshToken": "dGhpcyBp...",
    "expiresIn": 3600,
    "user": {
      "userId": "admin_user",
      "userName": "홍길동",
      "roles": ["ADMIN", "MANAGER"]
    }
  }
  ```

### 2.2 KIOSK/장비 자동 로그인 (Device Login)
- **Endpoint**: `POST /auth/device-login`
- **Description**: KIOSK, DAS/DPS PC 등 고정된 장비가 MAC 주소나 장비 ID를 통해 로그인합니다.

---

## 3. 마스터 데이터 (Master Data)

### 3.1 권역(Zone) 및 로케이션(Location) 조회
- **Endpoint**: `GET /master/locations`
- **Query Params**: `zoneId`, `type` (CELL, BUFFER 등), `status` (EMPTY, OCCUPIED)

### 3.2 설비(Equipment) 목록 조회
- **Endpoint**: `GET /master/equipments`
- **Query Params**: `type` (DAS, DPS, SORTER, PTL 등), `status` (ACTIVE, ERROR)
- **Response**:
  ```json
  [
    {
      "equipmentId": "DAS-01",
      "name": "강남센터 DAS 1호기",
      "type": "DAS",
      "ipAddress": "192.168.1.10",
      "status": "RUNNING"
    }
  ]
  ```

---

## 4. 주문 처리 (Order Processing)

### 4.1 출고 주문(Outbound Order) 목록 조회
- **Endpoint**: `GET /orders/outbound`
- **Query Params**: `date`, `status` (READY, PICKING, SORTING, COMPLETED), `waveId`

### 4.2 웨이브(Wave) 생성
- **Endpoint**: `POST /orders/waves`
- **Description**: WMS에서 수신한 출고 오더들을 묶어 하나의 작업 배치(Wave)로 생성합니다.
- **Request Body**:
  ```json
  {
    "waveName": "초특급 배송 1차",
    "type": "B2C_DAS", // B2B_DPS, B2C_DAS 등
    "priority": "HIGH",
    "orderIds": ["ORD-1001", "ORD-1002", "ORD-1003"]
  }
  ```

### 4.3 주문 상태 변경 (WMS 수동 동기화)
- **Endpoint**: `PUT /orders/{orderId}/status`

---

## 5. 작업 통제 (Task Control - WCS Core)

작업자(PDA, KIOSK)가 수행하는 업무와 설비 제어 로직 간의 인터페이스입니다. (실제 설비 기계-표시기 등-와의 통신은 REST API가 아닌 RabbitMQ Pub/Sub으로 이루어집니다.)

### 5.1 피킹 작업(Picking Task) 시작 처리
- **Endpoint**: `POST /tasks/picking/{taskId}/start`
- **Description**: 작업자가 PDA를 통해 특정 피킹 태스크를 시작했음을 서버에 알립니다.
- **Request Body**:
  ```json
  {
    "workerId": "worker01",
    "equipmentId": "PDA-05"
  }
  ```

### 5.2 DAS/DPS 분배 대상 조회 (Scan Barcode)
- **Endpoint**: `POST /tasks/distribution/scan`
- **Description**: DAS나 DPS 작업대(PC)에서 상품 바코드를 스캔했을 때, 어느 슈트(Chute)나 박스에 넣어야 할지(표시기를 켤지) WCS에 질의합니다.
- **Request Body**:
  ```json
  {
    "equipmentId": "DAS-01",
    "barcode": "8801234567890" // 상품 바코드 번호
  }
  ```
- **Response**:
  ```json
  {
    "match": true,
    "chuteNo": "A-15",
    "targetQty": 2,
    "displayMessage": "2개 투입"
  }
  ```
  *(주: 응답과 동시에 WCS 백엔드는 내부적으로 RabbitMQ를 통해 `DAS-01` 설비의 `A-15` 표시기 컨트롤러에 불을 켜라는 메시지를 발행(Publish)합니다.)*

### 5.3 분배 완료 처리 (Confirm)
- **Endpoint**: `POST /tasks/distribution/confirm`
- **Description**: 표시기의 버튼을 누르거나, 스캔을 통해 분배 작업 1건이 완료되었음을 WCS에 보고합니다. (RabbitMQ 메시지로도 수신 가능하나 수동 버튼 액션용 API)

### 5.4 예외(Exception) 보고
- **Endpoint**: `POST /tasks/exceptions`
- **Description**: 상품 파손, 수량 부족(Shortage), 바코드 미인식 등 작업 중 발생한 문제를 보고합니다.
- **Request Body**:
  ```json
  {
    "taskId": "TSK-00123",
    "type": "SHORTAGE",      // 부족
    "barcode": "88012345",
    "reportedQty": 1,        // 실제 발견 수량
    "reason": "박스 내 상품 누락"
  }
  ```

---

## 6. 설비 상태 모니터링 (Equipment Monitoring)

### 6.1 설비 상태 조회
- **Endpoint**: `GET /equipments/{equipmentId}/status`

### 6.2 설비 강제 제어 (Command)
- **Endpoint**: `POST /equipments/{equipmentId}/command`
- **Description**: WCS Dashboard에서 특정 설비를 강제로 구동/정지/리셋 합니다.
- **Request Body**:
  ```json
  {
    "command": "RESET", // START, STOP, RESET
    "operatorId": "admin"
  }
  ```

---

> **참고 (Note):**
> 본 문서는 초기 설계 Draft입니다. 각 엔드포인트의 세부 필드 제약사항, 페이징 처리를 위한 `page`, `size` 파라미터 규격, 정렬 기준(`sort`) 등은 개발 착수 전 프론트엔드 팀과 협의하여 Swagger(OpenAPI 3.0) 포맷으로 구체화되어 배포될 예정입니다. 설비(HW)간의 초저지연 통신은 이 REST API가 아닌 별도의 **`messaging-specification.md(RabbitMQ)`** 문서를 참조하십시오.
