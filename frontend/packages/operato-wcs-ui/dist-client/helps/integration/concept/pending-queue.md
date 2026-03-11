# Pending Scenario Queue

일정 시간 이후에 실행될 지연 예약된 시나리오를 등록할 수 있도록 제공되는 장치이다.

## Operation

- 지연 큐는 [book-up-scenario](../task/book-up-scenario.md) 태스크에 의해서 일정 시간 후에 실행될 시나리오를 등록할 수 있는 대기 큐이다.
- 지연 큐에 보관된 지연 시나리오는 지연 시간이 만료되면 [pick-pending-scenario](../task/pick-pending-scenario.md) 태스크에 의해서 꺼내져서 실행되게 된다.
- 지연 큐는 reset-pending-queue 태스크에 의해서 클리어될 수 있다.

### 태그 tag

- 지연된 시나리오는 'tag' 속성으로 카테고리화 될 수 있다. 즉, book-up 시에는 지연 시나리오에 'tag'를 부여할 수 있으며, [pick-pending-scenario](../task/pick-pending-scenario.md) 시에는 특정 'tag'가 부여된 시나리오 중에서 지연시간이 만료된 시나리오를 실행할 수 있다.

### 시나리오 우선 순위 priority

- 지연 큐에 [book-up-scenario](../task/book-up-scenario.md)되는 시나리오는 우선순위 priority 를 속성으로 가질 수 있다.
- 지연 만료된 시나리오 중에서 [pick-pending-scenario](../task/pick-pending-scenario.md)에 의해서 시나리오가 선택될 때, 우선 순위가 높은 것이 최우선으로 선택되며, 동일 우선순위의 시나리오 중에서는 만료시간이 가장 오래된 시나리오가 선택된다.

## 활용

### long 시나리오 구현에 활용

- 여러 단계의 다양한 서브 시나리오들로 구성된 긴 시나리오를 이해하고 관리하는 것은 쉽지 않다.
- 이련 경우에 여러개의 단위 시나리오를 정의하고, 지연 큐를 활용해서 단위 시나리오들을 순차적으로 작동시키는 방법으로 재구성할 수 있다.

### 유한 자원의 시나리오 실행 쓰로틀링 throttling 에 활용

- [pick-pending-scenario](../task/pick-pending-scenario.md) 태스크는 지연만료된 시나리오를 지연 큐에서 꺼내서 실행 run 하는 태스크이다.
- 유한 자원을 가진 경우, 동시에 자원 이상의 시나리오를 실행하지 못하게 컨트롤 하고자 하는 경우에 유용하게 응용할 수 있다.
- 예를 들어, 하나의 로봇암 A는 한 시점에 하나의 시나리오만을 수행해야 하는 경우가 있다고 가정하면, 로봇암 A가 주로 실행하는 시나리오들을 'RA'라는 태그를 붙여서 지연 큐에 [book-up-scenario](../task/book-up-scenario.md)한다.
  그리고, 로봇암 A를 위해서 무한 반복하는 하나의 시나리오를 만들고, 그 시나리오에 [pick-pending-scenario](../task/pick-pending-scenario.md) 태스크를 이용해서, 'RA' 태그가 붙여진 지연 만료된 시나리오를 하나씩 꺼내서 순차적으로 실행한다.
