# API module

외부 클라이언트에 RESTful 서비스를 제공하기위한 기본 모듈이다.

- openapi V3 [Swagger](https://swagger.io/) 명세를 지원한다.
- (서버) Graphql Resolver로 구현된 기능 중에서 외부 클라이언트를 위한 RESTful API로 노출하는 패턴을 제공한다.
- (클라이언트) API 테스트를 위한 API Sandbox를 제공한다.
- 각 모듈별로 제공된 API 기능은 최종 Application에 통합된다.

## 모듈에서 API 기능 제공하기 (기본 구조)

- API를 제공하고자하는 모듈의 package.json에서 API 모듈을 dependency에 추가한다.
- openapi 명세
  - 각 모듈의 root 폴더에 'openapi' 라는 폴더를 만들어서 제공한다.
  - 각 API 버전별로 {version}.yaml 파일을 생성하고 openapi V3 명세에 맞게 API 명세를 작성한다.
  - API 내용이 많아서 하나의 파일로 작성하기 어려운 경우 openapi/{version} 폴더를 만들고, {version}.yaml 의 일부 내용을 분리해서 폴더에 추가할 수 있다.
  - 프레임워크는 openapi/{version}.yaml 과 openapi/{version}/\*.yaml 을 모두 합해서 해당 버전의 API 명세로 이해한다.
  - openapi/{version}.yaml 에는 openapi 명세의 공통 정의만 가지고, openapi/{verion}/{tag}.yaml 에 명세의 각 tag에 해당하는 내용을 담는 것을 추천한다.
- 서버
  - server/restful 폴더를 생성
  - server/restful/{version} 폴더를 생성. 버전은 API 버전을 의미하며, 'unstable', 'v1', 'v2' 식으로 네이밍할 수 있다.
  - server/restful/{version} 폴더에 구현될 API는 @things-factory/api 모듈에서 정의한 restfulApiRouter에 endpoint를 추가하는 방법으로 정의한다.
  - 'get', 'post', 'put', 'delete' 메쏘드를 각 API의 의미에 맞게 사용할 것을 권장한다. (get => retrieve, post => create, put => update, delete => delete)
- 클라이언트
  - 각 모듈 또는 어플리케이션의 client/api/api-sandbox-{version}.js 에 구현할 것을 권장한다.
  - 현재는 이 페이지에 APIS 명세를 별도로 정의하고 있으나, 향후 openapi 명세를 활용하는 것으로 통합될 예정이다.

## 클라이언트 사이드 기능

### 클라이언트 라우팅

- 클라이언트 페이지명 : 'api-swagger'
  - 'api-swagger' 페이지로 이동하면, openapi 명세에 기반한 swagger 페이지를 볼 수 있으며, 샌드박스 테스트를 해볼 수 있다.

### APIPageTemplate

- API 샌드박스 구성 시에 활용.
- APIPageTemplate

```
import { APIPageTemplate } from '@things-factory/api'

const APIS = [
  {
    name: 'scenario',
    path: 'scenario/:id',
    method: 'get',
    parameters: [],
    description: 'get scenario information'
  }
]

class APISandbox extends APIPageTemplate {
  constructor() {
    super()
  }

  get APIS() {
    return APIS
  }

  get context() {
    return {
      title: 'API Sandbox',
      apiCategory: 'Integration',
      description: 'Scenario control, connection control, ...'
    }
  }
}

window.customElements.define('api-sandbox', APISandbox)
```

## 서버 사이드 기능

- RESTful API router
  - 외부 클라이언트에 RESTful 엔드포인트를 제공
  - 내부적으로는 Graphql Resolver를 호출하여 그 결과를 외부 클라이언트에 제공하는 패턴을 권장
  - endpoint 패턴
    - '/api/{version}/...'

```
/* API implementation sample */

import gql from 'graphql-tag'
import { restfulApiRouter as router } from '@things-factory/api'

/* restfulApiRouter의 prefix가 '/api' 이므로, 그 이후의 path를 지정 */
router.get('/unstable/scenario/:id', async (context, next) => {
  const { client } = context.state
  const { id } = context.params

  context.body = await client.query({
    query: gql`
      query($id: String!) {
        scenario(id: $id) {
          id
          name
          description
        }
      }
    `,
    variables: {
      id
    },
    context
  })
})
```

## Swagger API Page 제공

- about [Swagger](https://swagger.io/)
- [/api-docs](/api-docs)
  - 시스템의 대표 버전의 API 도큐먼트(Swagger) 페이지로 redirect됨
- [/api-docs/{version}](/api-docs/{version})
  - 시스템의 해당 버전의 API 도큐먼트(Swagger) 페이지를 제공함
