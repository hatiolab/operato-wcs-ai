import { store, CustomAlert } from '@things-factory/shell'
import { updateOperatoLogisBaseUrl } from './actions/main.js'
import { i18next, localize } from '@things-factory/i18n-base'

// Operato Base URL 변수
let OPERATO_BASE_URL = null
  // Operato 메뉴 - 라우팅 정보
let OPERATO_MENU_ROUTES = []

/**
 * 서버에서 조회한 정보로 메뉴 템플릿을 구성하여 리턴
 * 
 * @param {*} menuData 
 * @returns 
 */
export function initMenuRoutes(menuData) {
  // 1. routes
  let routes = []

  // 1. 서버에서 조회한 메뉴 정보 중에 메인 메뉴를 추출
  let mainMenus = menuData.map(item => {
    if (item.menu_type == 'MENU') {
      let pageName = item.name;
      routes.push({ id: item.id, parent: true, title: item.title, tagname: pageName, page: pageName, template: item.template, routing_type: item.routing_type })
      return { id: item.id, name: item.title, icon: item.icon_path, path: pageName, menus: [] }
    } else {
      return { id: '1' }
    }
  }).filter(i => i.id != '1')

  // 2. 메인 메뉴 하위에 서브 메뉴를 추가
  menuData.forEach(item => {
    if (item.menu_type == 'SCREEN') {
      let parentMenu = mainMenus.find(main => main.id == item.parent_id)
      if (parentMenu) {
        routes.push({ id: item.id, parent: false, parent_id: parentMenu.id, title: item.title, tagname: (item.description ? item.description : item.routing), page: item.routing, template: item.template, routing_type: item.routing_type })
        parentMenu.menus.push({
          id: item.id,
          name: item.title,
          path: item.routing,
          icon: item.icon_path
        })
      }
    }
  })

  // 3. 변수 설정
  OPERATO_MENU_ROUTES = Object.assign([], routes)

  // 4. 메뉴 데이터 리턴
  return { menus: mainMenus, routes: routes };
}

/**
 * store에서 라우트 관련 매핑 정보를 조회해서 리턴
 */
export function getRouteMappings() {
  return OPERATO_MENU_ROUTES
}

/**
 * Operato Base URL을 operato-wcs-ui 서버로 부터 리턴
 * 
 * @returns 
 */
export async function operatoBaseUrl() {
  if (OPERATO_BASE_URL == null) {
    // 1. Operato Server로 세션이 아직 살아있는지 확인 요청.
    let res = await fetch('/env/operato/base_url', {
      method: 'GET',
      headers: {
        'Content-type': 'application/json',
        'Accept': 'application/json'
      }
    })

    // 2. 서버에서 리턴한 JSON 데이터 추출
    let data = await res.json()
      // 3. 변수에 저장
    OPERATO_BASE_URL = data.value
      // 4. store에 저장
    store.dispatch(updateOperatoLogisBaseUrl(OPERATO_BASE_URL))
  }

  return OPERATO_BASE_URL
}

/**
 * Operato 서버에 세션이 살아있는지 체크
 * 
 * @returns 
 */
export async function operatoSessionValid(domain) {
  let baseUrl = await operatoBaseUrl()
  let checkAuthUrl = `${OPERATO_BASE_URL}/session_info`

  // Operato Server로 세션이 아직 살아있는지 확인 요청.
  let res = await fetch(checkAuthUrl, {
    //    referrerPolicy: 'unsafe-url',
    method: 'GET',
    credentials: 'include',
    mode: 'cors',
    headers: {
      'Accept': 'application/json',
      'Content-type': 'application/json',
      'X-Domain-Id': domain.subdomain
    }
  })


  return res.status == 401 ? false : true;
}

/**
 * Operato 서버에 로그아웃
 * 
 * @returns 
 */
export async function operatoLogout() {
  let logoutUrl = `${OPERATO_BASE_URL}/logout`
  let res = await fetch(logoutUrl, {
    method: 'POST',
    credentials: 'include'
  })

  return res
}

/**
 * Operato 메뉴 정보를 조회하여 리턴
 * 
 * @returns 
 */
export async function operatoGetMenus() {
  let params = `query=%5B%7B%20name%3A%20%22category%22%2C%20operator%3A%20%22eq%22%2C%20value%3A%20OPERATO%7D%2C%20%7B%20name%3A%20%22hidden_flag%22%2C%20operator%3A%20%22is_not_true%22%20%7D%5D`
  let menuUrl = `${OPERATO_BASE_URL}/menus/user_menus/OPERATO?${params}`

  // 1. 메뉴 정보 조회
  let res = await fetch(menuUrl, {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Content-type': 'application/json',
      'Accept': 'application/json',
      'X-Domain-Id': currentSubDomain(),
      'x-locale': currentLocale()
    }
  })

  let menuData = await res.json()
    // 2. 메뉴 데이터 리턴
  return menuData
}

/**
 * Operato 메뉴 메타 정보를 조회하여 리턴
 *****************************************************
 * @param {*} menuName
 * @returns 
 */
export async function operatoGetMenuMeta(menuName) {
  let menuUrl = `${OPERATO_BASE_URL}/menus/${menuName}/operato_menu_meta`

  // 1. 메뉴 메타 정보 조회
  let res = await fetch(menuUrl, {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Content-type': 'application/json',
      'Accept': 'application/json',
      'X-Domain-Id': currentSubDomain(),
      'x-locale': currentLocale()
    }
  })

  let menuMetaData = await res.json()
    // 2. 메뉴 메타 데이터 리턴
  return menuMetaData
}

/**
 * Operato 서버에 GET 방식 호출, response 리턴
 *****************************************************
 * @param {String} url
 * @param {*} params
 * @returns response 리턴
 */
export async function operatoGet(url, params) {
  let getUrl = `${OPERATO_BASE_URL}/${url}`

  if (params && params.length > 0) {
    getUrl += '?';

    if (typeof params != 'string') {
      params.forEach((currentElement, index, array) => {
        getUrl += `${currentElement['name']}=${currentElement['value']}&`
      });
    } else {
      getUrl += params;
    }
  }

  let res = await fetch(getUrl, {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Content-type': 'application/json',
      'Accept': 'application/json',
      'X-Domain-Id': currentSubDomain(),
      'x-locale': currentLocale()
    }
  })

  return res
}

/**
 * Operato 서버에 GET 방식 호출, 응답 중에 JSON 데이터를 리턴
 *****************************************************
 * @param {String} url
 * @param {*} params
 * @returns response 중에 json 데이터 리턴
 */
export async function operatoGetData(url, params) {
  let res = await operatoGet(url, params)
  return await res.json()
}

/**
 * Operato 서버에 POST 방식 호출, 서버 response 자체를 리턴
 *****************************************************
 * @param {*} url 
 * @param {*} bodyObj 
 * @returns response 리턴
 */
export async function operatoPost(url, bodyObj) {
  let bodyStr = (typeof bodyObj === 'string') ? bodyObj : JSON.stringify(bodyObj);
  let postUrl = `${OPERATO_BASE_URL}/${url}`
  let res = await fetch(postUrl, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-type': 'application/json',
      'Accept': 'application/json',
      'X-Domain-Id': currentSubDomain(),
      'x-locale': currentLocale()
    },
    body: bodyStr
  })

  return res
}

/**
 * Operato 서버에 POST 방식 호출 후 응답 데이터를 리턴
 *****************************************************
 * @param {*} url 
 * @param {*} bodyObj 
 * @returns response 중에 json 데이터 리턴
 */
export async function operatoPostData(url, bodyObj) {
  let res = await operatoPost(url, bodyObj)
  return await res.json()
}

/**
 * Operato 서버에 PUT 방식 호출, 서버 Response 자체를 리턴
 *****************************************************
 * @param {String} url 
 * @param {*} bodyObj 
 * @returns 서버 response 리턴
 */
export async function operatoPut(url, bodyObj) {
  let bodyStr = (typeof bodyObj === 'string') ? bodyObj : JSON.stringify(bodyObj);
  let putUrl = `${OPERATO_BASE_URL}/${url}`
  let res = await fetch(putUrl, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-type': 'application/json',
      'Accept': 'application/json',
      'X-Domain-Id': currentSubDomain(),
      'x-locale': currentLocale()
    },
    body: bodyStr
  })

  return res
}

/**
 * Operato 서버에 PUT 방식 호출 후 응답 데이터를 리턴
 *****************************************************
 * @param {String} url 
 * @param {*} bodyObj 
 * @returns response 중에 json 데이터 리턴
 */
export async function operatoPutData(url, bodyObj) {
  let res = await operatoPut(url, bodyObj)
  return await res.json()
}

/**
 * Operato 서버에 DELETE 방식 호출 후 서버 response를 리턴
 *****************************************************
 * @param {String} url 
 * @param {*} bodyObj
 * @returns response 리턴
 */
export async function operatoDelete(url, bodyObj) {
  let bodyStr = (typeof bodyObj === 'string') ? bodyObj : JSON.stringify(bodyObj);
  let delUrl = `${OPERATO_BASE_URL}/${url}`
  let res = await fetch(delUrl, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-type': 'application/json',
      'Accept': 'application/json',
      'X-Domain-Id': currentSubDomain(),
      'x-locale': currentLocale()
    },
    body: bodyStr
  })

  return res
}

/**
 * Operato 서버에 DELETE 방식 호출 후 응답 데이터를 리턴
 *****************************************************
 * @param {String} url
 * @param {*} bodyObj 
 * @returns response 중에 json 데이터 리턴
 */
export async function operatoDeleteData(url, bodyObj) {
  let res = await operatoDelete(url, bodyObj)
  return await res.json()
}

/**
 * Operato 서버에 멀티 데이터 업데이트 처리 요청
 *****************************************************
 * @param {String} url 
 * @param {*} bodyObj 
 * @returns response 리턴
 */
export async function operatoUpdateMultiple(url, bodyObj) {
  let bodyStr = (typeof bodyObj === 'string') ? bodyObj : JSON.stringify(bodyObj);
  let updateUrl = `${OPERATO_BASE_URL}/${url}`
  let res = await fetch(updateUrl, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-type': 'application/json',
      'Accept': 'application/json',
      'X-Domain-Id': currentSubDomain(),
      'x-locale': currentLocale()
    },
    body: bodyStr
  })

  return res;
}

/**
 * store 에서 도메인 가져오기
 *****************************************************
 * @returns 
 */
function currentDomain() {
  return store.getState().app.domain;
}

/**
 * store 에서 서브 도메인 가져오기
 *****************************************************
 * @returns 
 */
function currentSubDomain() {
  let domain = currentDomain();

  if (domain) {
    return currentDomain().subdomain;
  }

  return '';
}

/*
 * 현재 로케일 설정을 가져온다
 *****************************************************
 * @returns 
 * @description 
 *  : 한글 ko 로 저장 server 에 ko-KR 로 전송 
 *  : 영문 en-US 로 저장 server 에 그대로 전송 
 */
export function currentLocale() {
  let localeCookie = document.cookie;

  localeCookie = localeCookie.replace('i18next=', '');

  if (localeCookie == 'ko') return 'ko-KR';
  return localeCookie;
}

/**
 * 스토어에 저장된 현재 메뉴의 이름
 *****************************************************
 * @returns 
 */
export function currentRouteMenu() {
  return store.getState().route.page;
}