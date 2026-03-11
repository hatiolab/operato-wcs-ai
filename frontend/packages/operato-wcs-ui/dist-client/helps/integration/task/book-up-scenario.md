# Bookup Scenario

[지연 큐](../concept/pending-queue.md)에 지연된 시나리오를 추가하는 태스크이다.

## parameters

- scenario (mandatory)
  - [지연 큐](../concept/pending-queue.md)에 추가될 지연 시나리오를 선택한다.
- delay (mandatory)

  - 지연 시간을 초 seconds 단위로 지정한다. -1로 지정하면, 지연없이 바로 [pickup](./pick-pending-scenario) 될 수 있다.
  - 이 값에는 변수를 적용할 수 있다.
  - 예시
    - -1 => 지연 없음 no-delay
    - 100 => 100 seconds
    - #{recipe} => 'recipe' 스텝 결과 값
    - #{pizza.cookingTime} => 'pizza' 스텝 결과 데이타의 cookingTime 값

- priority
  - 지연 시나리오의 우선 순위를 지정한다. default 값은 0이다.
- variables
  - 시나리오에 변수로 전달될 [accessor](../concept/data-accessor) 값을 설정한다.
- tag
  - 시나리오에 붙여질 태그를 설정한다.
