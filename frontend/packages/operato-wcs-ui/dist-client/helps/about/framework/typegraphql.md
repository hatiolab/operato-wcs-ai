# TypeGraphql을 적용하는 이유.

[Modern framework for GraphQL API in Node.js](https://typegraphql.com/)

## 이전 방식에서 아쉬운 점

[관련 이슈 - [shell] typegraphql fully support](https://github.com/hatiolab/things-factory/issues/770)

- graphql의 타입이 typescript의 타입형태가 아니므로 resolver 구현시 명시적인 타입 적용이 어려워서 typescript의 장점이 반감됨.
- 사실상 graphql타입과 entity는 거의 동일한 경우가 많은데, graphql의 타입이 entity의 타입과 일치하지 않으므로, resolver 구현시 typescript 타입을 다시 정의하게되는 경우도 있음.
- graphql 관련된 구현(타입, 리솔버, 엔티티)가 분산되어 있으므로, redundant한 느낌이 있으며, 오류를 인식하거나 찾아내기가 어려웠음.

## typegraphql을 적용시 개선되는 점

- resolver를 구현할 때, typescript 타입을 온전히 적용할 수 있으므로, 타입관련된 코드 오류를 코딩 시점에 인지할 가능성이 높다.
- gql template literal을 사용하여 graphql 타입을 정의하지 않아도 되므로, gql template literal과 관련된 오류가 방지된다.
- resolver 구현시 graphql 타입정보가 같이 있으므로(타입과 구현이 분산되지 않으므로), 코드 오류가 방지된다.

## 적용 방법

- 이전방식과 typegraphql 방식이 혼용될 수 있도록 구성하였으므로, 신규로 개발하는 모듈부터 적용할 것을 추천함.
- 다른 entity와 resolver에서 많이 레퍼런스되는 것들 우선하여 typegraphql을 적용하였음 (domain, auth-base, ..)
  - shell
  - auth-base (일부)
  - setting-base
  - integration-base
  - board-service
  - lite-menu
  - ..
- plop template - 'entity' generator 를 'service' generator 로 변경 적용하였음

![Screen Shot 2021-08-08 at 11 55 41 AM](https://user-images.githubusercontent.com/1239480/128619236-3a5b52d6-e760-48dd-a9b1-78a6f89bedf1.png)

## 유의 사항

### types mapping

| description       | typescript        | typegraphql                                | graphql         |
| ----------------- | ----------------- | ------------------------------------------ | --------------- |
| 문자열            | string            | String (tyepgraphql 자동 매핑)             | String          |
| 정수형            | number (Integer)  | Int                                        | Int             |
| 실수형            | number (Float)    | Float                                      | Float           |
| 날자형            | Date              | Timestamp (tyepgraphql 자동 매핑)          | Timestamp       |
| 오브젝트형        | ScalarObject      | ScalarObject                               | ScalarObject    |
| any               | any               | ScalarAny                                  | ScalarAny       |
| 사용자정의 클래스 | SomeClass         | SomeClass (ObjectType or InputType로 정의) | SomeClass       |
| 사용자정의 Enum   | SomeEnumeration   | SomeEnumeration                            | SomeEnumeration |
| 어레이형          | SomeType[]        | [SomeType]                                 | [SomeType!]     |
| mandatory         | 변수명: SomeType  | SomeType with nullable => false (default)  | SomeType!       |
| optional          | 변수명?: SomeType | SomeType with nullable => true             | SomeType        |

### Annotations

- ObjectType / InputType
- Field
- Resolver
- Query / Mutation
- Directive
- Arg / Args
- Context

### Query sample

![Screen Shot 2021-08-08 at 12 27 54 PM](https://user-images.githubusercontent.com/1239480/128619799-8ef2b3c7-dc12-48f5-9683-794636de2cd1.png)

### Resolver sample

![Screen Shot 2021-08-08 at 12 41 11 PM](https://user-images.githubusercontent.com/1239480/128619978-2ac766e1-f276-4013-87a4-5eec3083bae3.png)

### 기존 방식과 새로운 방식의 혼용

- auth-base 모듈에 기존 방식과 새로운 방식이 혼용되어 있으므로 참고.

![Screen Shot 2021-08-08 at 12 21 27 PM](https://user-images.githubusercontent.com/1239480/128619652-d838f165-26f1-4cd3-8e4e-21d33cc46abb.png)
