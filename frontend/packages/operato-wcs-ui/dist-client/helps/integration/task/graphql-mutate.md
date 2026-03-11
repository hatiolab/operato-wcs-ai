# GraphqlMutate Task

서버에서 제공하는 graphql mutate API를 호출하는 태스크이다.
MutationQuery를 작성할 때, 변수 처리는 다음과 같이 두 가지 방법으로 가능하며, 방법 2를 사용하기를 권장한다.

방법 1. 시나리오 컨텍스트의 data와 variables를 변수로 활용할 수 있다.
이 방법은 문자열 변수를 적용할 때 적합하다.

```
mutation {
  syncAllMarketplaceOrder(fromDate: "${this.variables.fromDate}", toDate: "${this.variables.toDate}") {
    ...
  }

  removeTagFromObject(tag: "${this.data.tag}") {
    ...
  }
}
```

방법 2. varibles에 사용할 변수를 지정하고, graphql 변수 처리방법을 사용한다. variables의 값은 시나리오 컨텍스트의 data 에 대한 accessor로 지정할 수 있다.
이 방법은 문자열 변수를 적용할 때 적합하다.

```
mutation($fromDate: String!, $toDate: String!) {
  syncAllMarketplaceOrder(fromDate: $fromDate, toDate: $toDate) {
    ...
  }

  removeTagFromObject(tag: $tag) {
    ...
  }
}
```

## parameters

- mutation

  - graphql mutation 쿼리를 입력한다.

- variables
  - graphql query에서 사용될 변수를 정의한다.
  - key예는 변수의 이름을 지정하며, value에는 변수값에 해당하는 data accessor를 설정한다.
