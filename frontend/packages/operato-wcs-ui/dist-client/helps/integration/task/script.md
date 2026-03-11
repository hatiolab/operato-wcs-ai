# Script

Javascript Syntax로 작성된 script를 실행하여 그 결과를 리턴하는 태스크이다.

## parameters

- script
  - javascript systax로 작성한다.
  - global 오브젝트는 해당된 [Scenario Context](../concept/context)중 data 와 variables 를 가진 단순한 오브젝트이다. 즉, 스크립트 내에서 해당된 시나리오의 data 변수와 variables 변수를 특별한 수식자없이 또는 this 접근자를 통해서 사용할 수 있다.
    - data : 시나리오 인스턴스 컨텍스트의 data 속성
    - variables : 시나리오 인스턴스 컨텍스트의 시작 파라미터 속성
  - 태스크의 수행 결과는 return 신택스를 통해서 반환된다.
  - script 내에서는 global 오브젝트가 data와 variables로 한정되므로, 대부분의 시스템 관련된 기능은 사용할 수 없는 sandbox 환경이라고 이해하고, 단순한 연산정도의 기능을 구현하는 것을 권장한다.
    - [Node Sandbox](https://medium.com/@devnullnor/a-secure-node-sandbox-f23b9fc9f2b0)
    - [vm2](https://github.com/patriksimek/vm2/blob/master/README.md)

## sample script

```
return data['step1'].map(record => {
  return {
    ...record,
    id: 'PREFIX-' + record.id
  }
})
```

```
return Math.floor(Number(this.data['step2']))
```

```
return this.data['step3'].map(record => {
  return {
    ...record,
    id: (this.variables.prefix || 'DEFAULT-') + record.id
  }
})
```
