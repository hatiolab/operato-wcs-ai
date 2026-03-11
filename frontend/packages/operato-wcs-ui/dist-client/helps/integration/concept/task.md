# task

- 태스크는 [시나리오 엔진](./scenario-engine)에 의해서 실행되는 최소 단위의 작업이다.
- 태스크는 [시나리오](./scenario)의 각 스텝을 정의하기 위해 구현된 프로그램이다.
- 태스크는 여러가지 기능을 복합적으로 가지지 않도록 단순하고 명료한 기능으로 구현하는 것이 좋다.
- 태스크 구현에 필요한 여러가지 정보와 기능을 지원하기 위해서 시나리오에서는 [Scenario Context](./context)를 제공한다.
- 태스크 종류는 다음과 같이 구분될 수 있다.

## 구분

- 독립 태스크
  - Sleep, Log,
  - publish - notify data with tag to subscribers
- 플로우 컨트롤 태스크
  - Goto Step : Map, Range, ..
  - Sub Scenario : Map, Range, ..
  - Throw
  - End
- [커넥션](./connection) 기반 태스크
  - database-query
  - graphql-query, mutator
  - mqtt : subscribe/publish
  - indydcp : Indy7 robot control
  - modbus : read/writes..
  - robotics related : camera, markers, ..
