import { html } from 'lit-html'
import { navigateWithSilence, UPDATE_MODULES } from '@operato/shell'
import { navigate, store } from '@things-factory/shell'
import { getRouteMappings } from '@things-factory/operatofill'

export default function bootstrap() {
  // 1. App 모듈 
  let logisMetaModule = this

  // 2. Operato 인증 완료 핸들러 등록
  document.addEventListener('operatofill-process-start', function(event) {
    // 2.1 메뉴의 모든 라우팅 정보 추출
    let mappings = Object.assign([], getRouteMappings())
      // 2.2 모든 메뉴 중에 meta 페이지를 사용하는 메뉴는 metapage 모듈에서 라우팅 처리하도록 라우팅 필터링
    let metaRoutes = mappings.filter(mapping => mapping.parent == false && mapping.routing_type != 'STATIC')
      // 2.3 모듈 명 추출
    let modName = myModuleName()
    let modIdx = myModuleIndex()

    // 2.4 operatofill 처리 완료 이벤트 Fire
    document.dispatchEvent(new CustomEvent('operatofill-process-end', {
      detail: {
        moduleName: modName,
        moduleIndex: modIdx,
        routes: metaRoutes
      }
    }))
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
  // 1. store에서 모든 모듈 정보 조회
  let modules = store.getState().app.modules;
  // 2. 모듈 정보 중에 모듈명 정보로 metapage 모듈 추출
  let modName = myModuleName()
  let metaMod = modules.find(m => m.name == modName)
    // 3. 모듈 Index 정보로 metapage 모듈 추출 -> @things-factory 다음 버전엔 모듈 정보에 이름이 존재하므로 다음 버전 배포 후 이 로직 제거
  if (!metaMod) {
    let modIdx = myModuleIndex()
    metaMod = modules[modules.length + modIdx]
  }
  let metaRoutes = metaMod.routes

  // 4. page 정보로 라우팅 정보를 추출
  let route = metaRoutes.find(r => r.page == page)
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
  return '@things-factory/metapage'
}

/**
 * 모듈 Index -> 추후 제거
 */
function myModuleIndex() {
  return -3
}