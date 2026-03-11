# Variables

Scenario의 입력변수를 [Scenario Context](../concept/context) Data에 추가하는 태스크이다.
Variables Task는 이후 Step의 Task에서 Scenario Input 변수를 사용할 수 있도록, Scenario 시작 시에 받은 Input 변수들을 그대로 리턴하는 태스크이다.

예를 들어,

- Scenario Input 변수값

```
{
  inputA: "ABC",
  inputB: "DEF"
}
```

이고,

- Variables Task의 이름이 "VARIABLES" 인 경우,
- Variables Task가 완료된 후에 Scenario Context의 Data에는 다음과 같은 값이 추가되게 된다.

```
# scenario.context.data
{
  ...,
  VARIALBES: {
    inputA: "ABC",
    inputB: "DEF"
  },
  ...
}

```

## parameters

이 Task에는 별도 속성이 정의되지 않는다.
