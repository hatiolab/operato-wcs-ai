import { html } from 'lit-html'
import { navigateWithSilence, UPDATE_MODULES } from '@operato/shell'
import { navigate, store } from '@things-factory/shell'
import { TOOL_POSITION, VIEWPART_POSITION, appendViewpart, toggleOverlay } from '@things-factory/layout-base'
import { ADD_MORENDA } from '@things-factory/more-base'
import { ADD_SETTING } from '@things-factory/setting-base'
import { APPEND_APP_TOOL } from '@things-factory/apptool-base'
import { setupMenuPart, updateMenuTemplate } from '@things-factory/lite-menu'

import { getRouteMappings } from '@things-factory/operatofill'

export default function bootstrap() {
  // 1. app 모듈 
  let logisAppModule = this

  // 2. Operato 인증 완료 핸들러 등록
  document.addEventListener('operatofill-process-start', function(event) {
    // 2.1 메뉴의 모든 라우팅 정보 추출
    let mappings = Object.assign([], getRouteMappings())
      // 2.2 시스템 메뉴 추출, TODO 하드코딩 제거
    let systemMenu = mappings.find(mapping => mapping.tagname == 'system-home')
    let logisRoutes = null

    // 2.3 시스템 홈이 있으면
    if (systemMenu) {
      // 시스템 화면이 아니고 STATIC Routing인 메뉴들만 operato-wcs-ui 모듈의 라우팅 대상이 된다.
      logisRoutes = mappings.filter(mapping => mapping.parent_id != systemMenu.id && mapping.id != systemMenu.id && (mapping.parent == true || mapping.routing_type == 'STATIC'))

      // 2.4 시스템 홈이 없다면
    } else {
      // 대 메뉴와 STATIC Routing인 메뉴들만 operato-wcs-ui 모듈의 라우팅 대상이 된다.
      logisRoutes = mappings.filter(mapping => mapping.routing_type == 'STATIC')
    }

    // 2.5 모듈 명 추출
    let modName = myModuleName()
    let modIdx = myModuleIndex()

    // 2.6 operatofill 처리 완료 이벤트 Fire
    document.dispatchEvent(new CustomEvent('operatofill-process-end', {
      detail: {
        moduleName: modName,
        moduleIndex: modIdx,
        routes: logisRoutes
      }
    }))
  });

  // 3. 모듈에 실제 라우트 함수 등록
  this.route = operatoDynamicRoute

  // 4. 메뉴 설정
  setupMenuPart()

  // 5. 도메인 스위치 버튼 추가
  store.dispatch({
    type: APPEND_APP_TOOL,
    tool: {
      name: 'domain-switch',
      template: html `<domain-switch rounded-corner dark></domain-switch>`,
      position: TOOL_POSITION.REAR
    }
  })

  /* add refresh morenda */
  store.dispatch({
    type: ADD_MORENDA,
    morenda: {
      icon: html ` <mwc-icon>refresh</mwc-icon> `,
      name: html ` <ox-i18n msgid="button.refresh"></ox-i18n> `,
      position: TOOL_POSITION.FRONT_END,
      action: () => {
        location.reload()
      }
    }
  })

  // 6. 페이지 import 요청 이벤트
  document.addEventListener('dynamic-page-import-request', async function(event) {
    let module = event.detail.module;
    let url = event.detail.url;
    await dynamicImport(module, url)
  });
}

/**
 * 동적 페이지 임포트
 * 
 * @param {*} module 
 * @param {*} url
 * @returns 
 */
async function dynamicImport(module, url) {
  switch (module) {
    case 'metapage':
      import (`@things-factory/metapage/client/${url}`)
      break;
    case 'system-ui':
      import (`@things-factory/operato-wcs-system-ui/client/${url}`)
      break;
    case 'operatofill':
      import (`@things-factory/operatofill/client/${url}`)
      break;
    case 'manager':
      import (`@things-factory/operato-wcs-ui/client/${url}`)
      break;
  }
}

/**
 * Operato Logis Manager의 실제 라우팅 처리 로직
 * 
 * @param {*} page 
 * @returns 
 */
function operatoDynamicRoute(page) {
  // 1. 시스템 메뉴 이거나 시스템 메뉴의 하위 메뉴 중에 page 정보가 매칭되는 페이지 추출
  let modules = store.getState().app.modules;
  // 2. 모듈 정보 중에 모듈명 정보로 metapage 모듈 추출
  let modName = myModuleName()
  let appMod = modules.find(m => m.name == modName)
    // 3. 모듈 Index 정보로 metapage 모듈 추출 -> @things-factory 다음 버전엔 모듈 정보에 이름이 존재하므로 다음 버전 배포 후 이 로직 제거
  if (!appMod) {
    let modIdx = myModuleIndex()
    appMod = modules[modules.length + modIdx]
  }
  let appRoutes = appMod.routes
    // 4. page 정보로 라우팅을 찾는다.
  let route = appRoutes.find(mapping => mapping.page == page)
  if (route) {
    import (`${route.template}`)
    return page
  } else {
    return null
  }
}

/**
 * 모듈 명 리턴
 */
function myModuleName() {
  return '@things-factory/operato-wcs-ui'
}

/**
 * 모듈 Index -> 추후 제거
 */
function myModuleIndex() {
  return -1
}