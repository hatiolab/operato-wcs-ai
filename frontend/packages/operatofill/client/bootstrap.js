import operatoLogis from './reducers/main'
import { operatoSessionValid, operatoLogout, operatoGetMenus, initMenuRoutes } from './operatofill.js'
import { OperatoTerms } from './terms.js'

import { html } from 'lit-html'
import { navigateWithSilence, UPDATE_MODULES } from '@operato/shell'
import { i18next } from '@operato/i18n'

import { navigate, store } from '@things-factory/shell'
import { auth } from '@things-factory/auth-base'
import { TOOL_POSITION, VIEWPART_POSITION, appendViewpart, toggleOverlay } from '@things-factory/layout-base'
import { setupMenuPart, updateMenuTemplate } from '@things-factory/lite-menu'
import { getEditor, registerEditor, registerRenderer } from '@operato/data-grist'
import { ADD_MORENDA } from '@things-factory/more-base'
import { ADD_SETTING } from '@things-factory/setting-base'
import { APPEND_APP_TOOL } from '@things-factory/apptool-base'
import { setAuthManagementMenus } from '@things-factory/auth-ui'

export default function bootstrap() {
  // 1. Operato Logis Manager용 리듀서 추가
  store.addReducers({
    operatoLogis
  })

  // 2. 인증 후 프로필 정보가 추가될 때 
  auth.on('profile', async({ credential, domains, domain }) => {
    // 2.1 Operato 서버로 세션이 유효한 지 확인 요청.
    let sessionStatus = false
    if (domain && domain.subdomain) {
      sessionStatus = await operatoSessionValid(domain)
    }

    // 2.2 세션이 메뉴 정보를 Operato 서버로 부터 가져와서 표시
    if (sessionStatus) {
      // 2.2.1 서버의 메뉴 정보를 Get ...
      let menuData = await operatoGetMenus()
        // 2.2.2 메뉴 템플릿 && 라우팅 정보 빌드
      let menuRoutes = initMenuRoutes(menuData)
        // 2.2.3 메뉴 템플릿 업데이트
      updateMenuTemplate(menuRoutes.menus)
        // 2.2.4 용어 수집
      await OperatoTerms.operatoAllTerminologies()
        // 2.2.5 언어 변경시 처리 핸들러
      i18next.on('languageChanged', () => location.reload())

      // 2.2.6 operatofill 처리 완료 이벤트 리스터 등록
      document.addEventListener('operatofill-process-end', function(event) {
        // 1. 모듈 정보 추출
        let allModules = store.getState().app.modules
        let modules = Object.assign([], allModules)

        // 2. store의 모듈중에 이벤트로 넘어온 모듈을 추출
        let moduleName = event.detail.moduleName
        let moduleIndex = event.detail.moduleIndex
        let thisModule = modules.find(m => m.name == moduleName)

        if (!thisModule) {
          thisModule = modules[modules.length + moduleIndex]
        }

        // 3. 모듈의 routes 정보를 오버라이드
        thisModule.routes = event.detail.routes

        if (moduleIndex == -1) {
          // 4. store에 모듈 업데이트
          store.dispatch({
            type: UPDATE_MODULES,
            modules: modules
          })

          // 5. 첫 페이지를 리로딩 처리 (shell/app.js에서 리로딩 처리시에는 라우팅 정보가 없어서 처리를 못하므로 여기서 다시 한번 해줌...)
          // TODO 먼저 PAGE_NOT_FOUND 화면이 나온 후에 화면이 나오는 부분 처리 필요 ...
          store.dispatch(navigateWithSilence(location))
        }
      })

      // 2.2.7 operatofill 처리 시작 이벤트 Fire
      document.dispatchEvent(new CustomEvent('operatofill-process-start', {
        detail: {
          menuRoutes: menuRoutes
        }
      }))
    }
  })

  // 3. 로그아웃 이벤트를 받아서 스프링 쪽에 logout 요청
  auth.on('presignout', () => {
    operatoLogout()
  })
}