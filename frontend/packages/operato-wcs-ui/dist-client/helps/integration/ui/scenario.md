# Scenario

시나리오 정의 화면에서는 시스템에서 운영될 시나리오를 (생성/수정/삭제/테스트) 관리한다.

## properties

- name
  - 시나리오의 이름을 지정
  - 이름은 유니크해야함
- description
- [schedule](./crontab-editor.md)
- timezone
  - 스케쥴이 운영될 기준 시간대를 설정한다.
- active flag
  - 어플리케이션 시작 시점에 자동으로 시작여부 지정
- progress (모니터링중에만 표시되는 정보임)
  - (현재 스텝 번호 / 시나리오의 전체 스텝수) %
  - 메인 시나리오 기준이며, 서버시나리오는 하나의 스텝으로 계산된다.
- round (모니터링중에만 표시되는 정보임)
  - 반복되는 경우 현재 몇번째 반복횟수인지를 표현
- message (모니터링중에만 표시되는 정보임)
  - 시나리오 인스턴스의 최종 상태와 관련한 메시지

## operations

- start monitoring
  - [인티그레이션 모니터링](./integration-monitor.md) 화면으로 이동한다.
- copy scenario
  - 선택된 시나리오를 복사한다
- save scenario
  - 시나리오 수정사항을 저장한다.
  - 실행중 시나리오는 수정할 수 없다.
- delete scenario
  - 선택된 시나리오를 삭제한다.
  - 실행중 시나리오는 삭제할 수 없다.
