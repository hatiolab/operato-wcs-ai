# Operato WCS RabbitMQ 메시징 명세서 (Draft)

## 1. 개요 (Overview)

본 문서는 Operato WCS 백엔드 서버와 현장의 물리 설비(ECS: Equipment Control System - PLC, 표시기 컨트롤러, 스캐너, 컨베이어 등) 간의 실시간 비동기 통신을 위한 RabbitMQ 메시징 규격입니다.

- **프로토콜**: AMQP 0-9-1
- **메시지 포맷**: JSON (UTF-8 인코딩)
- **QoS (Quality of Service)**: WCS → ECS (At most once / At least once 혼용), ECS → WCS (At least once 권장, 수동 Ack)

---

## 2. 브로커 토폴로지 (Broker Topology)

모든 장비 현장 통신은 장비 단위(Equipment ID)로 격리 및 필터링할 수 있도록 Topic Exchange를 기본으로 사용합니다.

### 2.1 Exchange 정책

| Exchange Name | Type | Durable | 용도 |
|---------------|------|---------|------|
| `ex.wcs.outbound` | `topic` | `true` | WCS에서 현장 설비(ECS)로 내리는 제어 명령 퍼블리싱 |
| `ex.wcs.inbound` | `topic` | `true` | 현장 설비(ECS)에서 WCS로 올리는 상태/보고/이벤트 퍼블리싱 |

### 2.2 Routing Key 명명 규칙

**`{domain}.{equipmentType}.{equipmentId}.{action}`**

- `domain`: `wcs` (WCS 발송), `ecs` (ECS 발송)
- `equipmentType`: `das`, `dps`, `sorter`, `conveyor` 등
- `equipmentId`: 고유 장비 식별자 (예: `DAS-01`)
- `action`: `command`, `status`, `event`, `error` 등

*(예시)*
- `wcs.das.DAS-01.command` : WCS가 DAS-01 장비에게 보내는 제어 명령
- `ecs.sorter.SRT-02.event` : 소터 2호기에서 발생한 바코드 스캔 이벤트

---

## 3. 핵심 메시지 스키마: WCS ➔ ECS (제어 명령)

WCS가 현장의 ECS 모듈(Edge Gateway 또는 PLC)에 내리는 작업 지시입니다.
Exchange: `ex.wcs.outbound`

### 3.1 표시기 점등 명령 (Indicator Light On/Off)
- **Routing Key**: `wcs.{type}.{equipmentId}.command`
- **Description**: DAS/DPS 셀의 표시기(Indicator) 불을 켜거나 끄라는(혹은 숫자 표시) 명령
- **Payload**:
  ```json
  {
    "messageId": "msg-1234567890",
    "timestamp": "2026-03-01T15:30:00Z",
    "equipmentId": "DAS-01",
    "commandType": "INDICATOR_CTRL",
    "payload": {
      "cellId": "A-01-05",    // 표시기 식별 번호 (셀 ID)
      "action": "ON",         // ON, OFF, BLINK
      "displayUnit": 3,       // 표시기에 띄울 숫자 (지시 수량)
      "color": "GREEN"        // GREEN, RED, BLUE (지원 H/W인 경우)
    }
  }
  ```

### 3.2 설비 구동/정지 제어 (Hardware Start/Stop)
- **Routing Key**: `wcs.{type}.{equipmentId}.command`
- **Description**: 컨베이어 벨트나 소터기를 가동하거나 비상 정지(E-Stop) 시키는 제어 명령
- **Payload**:
  ```json
  {
    "messageId": "msg-1234567891",
    "timestamp": "2026-03-01T15:30:05Z",
    "equipmentId": "CV-Main-01",
    "commandType": "SYSTEM_CTRL",
    "payload": {
      "action": "START" // START, STOP, RESET, ESTOP
    }
  }
  ```

---

## 4. 핵심 메시지 스키마: ECS ➔ WCS (상태/이벤트 보고)

현장 설비(센서, PLC, 표시기 버튼)가 특정 이벤트를 감지하고 WCS로 데이터를 올려보내는 규격입니다.
Exchange: `ex.wcs.inbound`

### 4.1 작업 완료 버튼 푸시 이벤트 (Button Push Event)
- **Routing Key**: `ecs.{type}.{equipmentId}.event`
- **Description**: DAS/DPS 셀에서 작업자가 작업을 완료하고 표시기 버튼을 눌렀을 때 발생하는 이벤트
- **Payload**:
  ```json
  {
    "messageId": "evt-987654321",
    "timestamp": "2026-03-01T15:30:10Z",
    "equipmentId": "DAS-01",
    "eventType": "BUTTON_PUSH",
    "payload": {
      "cellId": "A-01-05",
      "pushedAt": "2026-03-01T15:30:09Z"
    }
  }
  ```

### 4.2 인라인 스캐너 바코드 판독 (Scanner Read)
- **Routing Key**: `ecs.scanner.{equipmentId}.event`
- **Description**: 소터(Sorter) 도입부나 컨베이어 고정식 스캐너가 박스/상품 바코드를 인식했을 때 WCS에 목적지(Chute)를 묻기 위한 이벤트
- **Payload**:
  ```json
  {
    "messageId": "evt-987654322",
    "timestamp": "2026-03-01T15:30:15Z",
    "equipmentId": "SCAN-01",
    "eventType": "BARCODE_READ",
    "payload": {
      "barcode": "8801234567890",
      "readTime": "2026-03-01T15:30:14.900Z",
      "status": "SUCCESS" // SUCCESS, NO_READ
    }
  }
  ```

### 4.3 설비 하드웨어 에러 (Hardware Alarm)
- **Routing Key**: `ecs.{type}.{equipmentId}.error`
- **Description**: 설비 모터 과열, 통신 단절, 센서 오류 등 알람 발생 보고
- **Payload**:
  ```json
  {
    "messageId": "err-55555555",
    "timestamp": "2026-03-01T15:31:00Z",
    "equipmentId": "SRT-01",
    "eventType": "ALARM",
    "payload": {
      "errorCode": "ERR_MOTOR_OVERHEAT",
      "severity": "CRITICAL", // INFO, WARNING, CRITICAL
      "errorMessage": "소터 메인 구동 모터 온도 초과 (85도)"
    }
  }
  ```

### 4.4 주기적인 상태 리포트 (Heartbeat / Keep-Alive)
- **Routing Key**: `ecs.{type}.{equipmentId}.status`
- **Description**: 각 ECS 게이트웨이가 WCS와의 연결 유지 및 현재 설비 가동 상태를 주기적(예: 10초)으로 알리는 용도
- **Payload**:
  ```json
  {
    "messageId": "hb-100201201",
    "timestamp": "2026-03-01T15:31:10Z",
    "equipmentId": "DAS-01",
    "eventType": "HEARTBEAT",
    "payload": {
      "status": "RUNNING", // RUNNING, STOPPED, ALARM
      "cpuUsage": 45,
      "memoryUsage": 60,
      "connectedClients": 4 
    }
  }
  ```

---

## 5. 장애 대응 정책 (Resilience & Error Handling)

1. **Dead Letter Queue (DLQ)**
   - WCS 백엔드가 `ex.wcs.inbound`에서 메시지를 소비하다가 비즈니스 로직(DB 저장 등) 에러로 처리에 실패(Nack)한 경우, 재시도 횟수 초과 시 별도의 `q.wcs.dlq`로 적재하여 유실을 방지합니다. 관리자는 모니터링 대시보드에서 이를 확인하고 수동 재처리할 수 있어야 합니다.
2. **멱등성 (Idempotency)**
   - 네트워크 재전송으로 인해 버튼 이벤트를 두 번 받거나, 명령을 두 번 내리더라도 부작용이 없어야 합니다. `messageId` 키를 Redis 등에 저장하여 중복 처리를 방어(Deduplication)해야 합니다.
3. **만료 (TTL)**
   - 표시기(Indicator) 점등 명령이나 센서 인식 정보 등은 일정 시간(예: 10초)이 지나면 업무적 유효성을 상실할 수 있습니다. 큐 내 지정된 TTL(Time-To-Live)을 경과한 메시지는 버려지도록 설정합니다.

*(추후 개발 착수 전, 상세한 에러 코드 표(`errorCode`)와 H/W 벤더별 맞춤형 페이로드 확장이 협의 하에 진행될 예정입니다.)*
