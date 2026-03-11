import { html } from 'lit-html'
import { navigateWithSilence, UPDATE_MODULES } from '@operato/shell'
import { navigate, store } from '@things-factory/shell'
import { getRouteMappings } from '@things-factory/operatofill'

export default function bootstrap() {
  // 1. App 모듈 
  let logisSysModule = this

  // 2. Operato 인증 완료 핸들러 등록
  document.addEventListener('operatofill-process-start', function(event) {
    // 2.1 메뉴의 모든 라우팅 정보 추출
    let mappings = Object.assign([], getRouteMappings())
      // 2.2 시스템 메뉴 추출, TODO 하드코딩 제거
    let sysRoute = mappings.find(mapping => mapping.tagname == 'system-home') // 시스템 홉 정보 찾기

    // 2.3 시스템 홈이 있으면 
    if (sysRoute) {
      // 2.3.1 시스템 홈 && 시스템 화면 중에 STATIC Routing인 메뉴들만 operato-logis-system-ui 모듈의 라우팅 대상이 된다.
      let sysRoutes = mappings.filter(mapping => (mapping.id == sysRoute.id || mapping.parent_id == sysRoute.id) && (mapping.parent == true || mapping.routing_type == 'STATIC'))
        // 2.3.2 모듈 명 추출
      let modName = myModuleName()
      let modIdx = myModuleIndex()

      // 2.3.3 operatofill 처리 완료 이벤트 Fire
      document.dispatchEvent(new CustomEvent('operatofill-process-end', {
        detail: {
          moduleName: modName,
          moduleIndex: modIdx,
          routes: sysRoutes
        }
      }))
    }
  });

  // 3. 모듈 정보에 route 함수 등록
  this.route = operatoDynamicRoute
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
  let sysMod = modules.find(m => m.name == modName)
    // 3. 모듈 Index 정보로 metapage 모듈 추출 -> @things-factory 다음 버전엔 모듈 정보에 이름이 존재하므로 다음 버전 배포 후 이 로직 제거
  if (!sysMod) {
    let modIdx = myModuleIndex()
    sysMod = modules[modules.length + modIdx]
  }
  // 4. 시스템 홈 메뉴가 없다면 시스템 메뉴는 없는 것이니 스킵
  let sysRoutes = sysMod.routes
  if (!sysRoutes || sysRoutes.length == 0) {
    return null
  }

  // 5. page 정보로 라우팅 정보를 추출
  let route = sysRoutes.find(r => r.page == page)
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
  return '@things-factory/operato-wcs-system-ui'
}

/**
 * 모듈 Index -> 추후 제거
 */
function myModuleIndex() {
  return -2
}