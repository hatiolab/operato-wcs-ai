# Lite Menu module

간편하게(Menu 모듈 대비) 메뉴 구성을 할 수 있도록 제공되는 모듈이다.

| 항목             | Menu 모듈 | Lite Menu 모듈 | 설명                                                                                                    |
| ---------------- | --------- | -------------- | ------------------------------------------------------------------------------------------------------- |
| 정적메뉴제공     | x         | o              | 정적메뉴는 메뉴 구성의 베이스가되어, 도메인이나 사용자의 권한 등과 무관하게 항상 보여지는 메뉴를 말한다 |
| 동적메뉴제공     | o         | o              | 도메인별로 커스터마이징되는 메뉴                                                                        |
| 사용자 권한 연동 | o         | x              |                                                                                                         |
| 메뉴 depth       | 2         | 2              |                                                                                                         |

## features

- 반응형 디자인
- 2 레벨 메뉴
  - 톱레벨의 두가지 기능
    - 페이지 라우팅
    - 하위 메뉴 펼치기
- 정적/동적 메뉴 구성 (from lite-menu entity)
  - 부가메뉴관리페이지에서 동적인 메뉴를 관리할 수 있다.
- 메뉴와 페이지 연결
  - 톱레벨은 현재 페이지가 자신의 페이지이거나 하위 메뉴의 페이지이면 강조되어 표현되어야 한다.
  - 또한, 톱레벨이 강조되어 표현되는 경우에 하위 메뉴는 펼쳐져야 한다.
  - 톱레벨과 하위 메뉴의 연결 관계는 하위 메뉴 페이지의 regexp 등으로 설정가능해야 한다.
  - 전체 메뉴에서 하나의 톱레벨만이 강조될 수 있다.

## Look & Feel

- 두 가지 레이아웃을 제공
  - Portrait
    - nav 또는 aside 영역에 추가되는 경우
    - 상하로 길죽한 형태의 메뉴 구조
    - 레벨 1 메뉴를 기본으로 트리형태의 메뉴 구조
    - 레벨 2를 펼치거나 숨기는 과정에 슬라이딩 애니메이션
  - Landscape
    - header 영역에 추가되는 경우
    - 좌우로 넓적한 형태의 메뉴 구조
    - 모든 레벨의 메뉴가 한번에 보이는 메뉴 구조

## 구현

- LiteMenu를 배치하는 적절한 포인트는 클라이언트 모듈의 bootstrap.js 이다.
- setupMenuPart(option: {hovering?: boolean, position?: string})
  - 주어진 옵션에 따라서 LiteMenu를 생성한다.
  - hovering: true | false
    - hovering 메뉴인지, 고정자리를 차지하는 메뉴인지를 설정한다
  - position: VIEWPART_POSITION.NAVBAR | VIEWPART_POSITION.ASIDEBAR | VIEWPART_POSITION.HEADERBAR
    - LiteMenu가 위치할 포지션을 설정한다.
    - NAVBAR와 ASIDEBAR를 지정하면 portrait 스타일의 메뉴가 추가되며, 그 외에는 landscape 형태의 메뉴가 추가된다.
- updateMenuTemplate(menutemplate)
  - 정적인 메뉴구조를 설정한다.
  - 만약 메뉴구조를 변경하고 싶을 때에 호출할 수 있다.

```
import { updateMenuTemplate, setupMenuPart } from '@things-factory/lite-menu'
import { VIEWPART_POSITION } from '@things-factory/layout-base'

...

setupMenuPart({ hovering: false, position: VIEWPART_POSITION.HEADERBAR })
updateMenuTemplate([{
  name: '생산',
  icon: 'business', /* Material Icon의 이름 */
  menus: [
    {
      name: '생산계획',
      path: 'production-plan', /* page 이름 */
      icon: 'add_circle_outline'
    },
    {
      name: '작업지시',
      path: 'production-order',
      icon: 'add_circle_outline'
    },
    {
      name: '로트추적',
      path: 'lot-tracing',
      icon: 'add_circle_outline'
    },
    {
      name: '자재투입추적',
      path: 'material-tracing',
      icon: 'add_circle_outline'
    }
  ]
},
{
  name: '설비',
  icon: 'groups',
  menus: [
    {
      name: '설비상태',
      path: 'equipment-state',
      icon: 'add_circle_outline'
    },
    {
      name: '설비별데이타이력',
      path: 'equipment-data-history',
      icon: 'add_circle_outline'
    },
    {
      name: '설비 정비계획',
      path: 'equipment-maintenance-plan',
      icon: 'add_circle_outline'
    }
  ]
},
{
  ...
}])

```
