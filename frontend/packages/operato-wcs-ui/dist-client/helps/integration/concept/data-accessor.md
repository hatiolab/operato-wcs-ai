# data accessor

- 태스크의 속성으로 [시나리오 컨텍스트](./scenario-context) 내의 Data 중에서 필요한 value를 한정하고자 할 때 사용된다
- 컨텍스트의 데이타는 각 스텝의 이름을 Key로 가지며, 스텝의 결과값을 Value로 유지하는 오브젝트이다.
- 따라서 accessor는 항상 스텝의 이름으로 시작되며, 그 값 아래로도 더 한정하고 싶은 경우에는 자바스크립트 오브젝트의 Value를 지정하는 표현을 사용할 수 있다.
- 예시
  - `stepname`
  - `stepname.property`
  - `stepname.property.property. ...`
