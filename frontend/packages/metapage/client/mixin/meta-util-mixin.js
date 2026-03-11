import { css, html } from 'lit-element'

import { navigate, CustomAlert } from '@things-factory/shell'
import { EXPORT } from '@things-factory/export-base'
import { openPopup } from '@things-factory/layout-base'
import { OperatoTerms } from '@things-factory/operatofill/client/terms.js'

import { i18next } from '@operato/i18n'
import { store } from '@operato/shell'
import { isMobileDevice } from '@operato/utils'

/**
 * 메타 서비스를 이용하는 페이지, 엘레먼트 의 최상위 오브젝트 
 ******************************************************
 * @param {Object} baseElement 
 * @returns 
 */
export const MetaUtilMixin = (baseElement) => class extends baseElement {
  static get properties() {
    return {
      config: Object, // 그리드 설정
      total: Number, // 페이지 
      records: Array, // 그리드 조회 데이터 
      sort_fields: Object, // 메뉴 메타에 설정 된 정렬 
      actions: Object, // is_page 의 경우 사용 되는 버튼 액션 
      grid_config: Object, // 메뉴 메타의 그리드 설정 
      menu: Object, // 메뉴 정보 
      menu_params: Object, // 메뉴 파라미터 정보 
      search_hidden_fields: Object, // 필터에 설정된 히든 필드 에 대한 정보 
      select_fields: Object, // 조회 대상 필드 
      sort_fields: Object, // 정렬 대상 필드 
      use_add_button: Boolean, // 추가 버튼 사용 여부 
      route_name: String, // is_element 타입의 경우 routing 명칭 (element 속성으로 소유자가 호출할때 지정해야함)
      grid_view_options: Array, // GRID 표현 옵션 ( CARD, LIST, GRID )
      grid_mode: String, // 현재 그리드 표현 
      search_form_fields: Array, // 서치폼 구성 설정 정보 
      fetch_callback: Object, // 그리드 또는 서치폼 에서 조회 이벤트 발생시 callback 을 받기 위한 함수 
      button_elements: Array, // is_element 타입에서 그려질 버튼 element
      form_fields: Array, // 메뉴 폼 정보 
      is_page: Boolean, // 현재 문서의 page 여부 
      open_params: Object,
      parent_id: String, // is_element 타입의 경우 소유자 id (element 속성으로 소유자가 호출할때 지정해야함)
      resource_url: String, // 조회 URL 
      save_url: String, // 저장 URL
      cud_flag_converter: Array // CUD 플래그 컨버터 함수 
    }
  }

  /**
   * 부모 객체에서 parent_id 변경시 resourceUrl, saveUrl 을 갱신 하고
   * 재조회 한다. 
   ********************************* 
   * @param {String} newVal 
   */
  set_parent_id(newVal) {
    this.parent_id = newVal;

    this.resource_url = this.menu.resource_url.replace(":id", this.parent_id);
    if (this.menu.save_url) {
      this.save_url = this.menu.save_url.replace(":id", this.parent_id);
    }

    // 조회 실행  
    if (this.is_element) {
      if (this.grist) {
        this.grist.fetch();
      } else if (this.useSearchForm()) {
        this.searchForm.submit();
      } else {
        this.fetchHander();
      }
    }
  }

  /**
   * 현재 문서의 Element 여부 
   ************************
   * @returns Boolean
   */
  get is_element() {
    return !this.is_page;
  }

  /**
   * 타이틀 / 버튼 설정 
   */
  get context() {
    return this.createPageContextObject();
  }


  /**
   * 기본 스타일 정의
   * chart / 일반 grist 페이지 / 팝업 지원 
   */
  static get styles() {
    let styles = [
      css`
      :host {
        display: flex;
        flex-direction: column;
        overflow-x: overlay;
        background-color: var(--main-section-background-color);
      }
      .container {
        overflow-y: auto;
        flex: 0.95;
      }
      `
    ];

    // 그리스트 스타일 추가 
    if (this.getGristStyle) {
      styles.push(...this.getGristStyle());
    }

    // 서치폼 스타일 추가 
    if (this.getSearchFormStyle) {
      styles.push(...this.getSearchFormStyle());
    }

    // 버튼 콘테이너 스타일 추가 (엘리먼트로 로드시 사용 )
    if (this.getButtonContainerStyle) {
      styles.push(...this.getButtonContainerStyle());
    }

    return styles;
  }

  /**
   * Lifecycle
   */
  async connectedCallback() {
    if (super.connectedCallback) {
      await super.connectedCallback();
    }
  }

  /**
   * 페이지 init 이 완료 되면 그리드/ 서치폼에 대한 설정을 한다.
   */
  async firstUpdated() {

    //****************** 최초 로딩시 기존 화면 또는 엘리먼트로 부터 넘겨받은 parameter 가 존재 한다면 **
    //*****************  서치폼 또는 필터폼(그리스트) 에 기본값을 셋팅한다.   *********************/
    // 파라미터가 배열이 아닐때만.... 
    if (this.isNotEmpty(this.open_params) && !Array.isArray(this.open_params)) { } else {
      // 파라미터가 배열일때 ? 
      // TODO 
    }

    //****************** 엘리먼트 로드시 버튼 생성 ( ex: popup ) *********************/
    // 버튼 생성 
    if (this.is_element || this.isMultiLayoutPage() == true) {
      let buttons = [];

      // Custom Button Elements 생성 
      if (this.getCustomButtons) {
        buttons.push(...this.getCustomButtons());
      }

      // Basic Button Elements 생성 
      if (this.getBasicButtons) {
        buttons.push(...this.getBasicButtons());
      }

      // 버튼 설정에 따라 정렬 
      this.button_elements = this.arraySortByRankField(buttons);
    }

    //****************** 그리스트 및 그리스트 버튼 설정 *********************/

    // 그리스트가 없으면 하위 그리스트 설정을 진행 하지 않는다 .
    if (!this.grist) {
      return;
    }

    let gristButtons = [];

    // Grist Buttons 생성 
    if (this.getGristButtons) {
      gristButtons = this.getGristButtons();
    }

    // grist 설저 생성 
    if (this.createGristConfig) {
      this.config = this.createGristConfig();
    }

    this.config.columns.push(...gristButtons);
    this.config.columns.push(...this.grid_config);
  }

  /**
   * 용어 코드를 텍스트로 변환 
   **************************************
   * @param {String} code 
   * @param {Array or String} parameters
   * @returns {String} text or code 
   */
  convertLanguageCode(code, parameters) {
    let transText = '';

    if (this.isEmpty(parameters)) {
      transText = OperatoTerms.t1(code);
    } else if (Array.isArray(parameters)) {
      let convParams = [];
      parameters.forEach(x => {
        convParams.push(this.convertLanguageCode(x));
      })

      transText = OperatoTerms.t3(code, convParams);
    } else {
      let convParams = [];
      convParams.push(this.convertLanguageCode(parameters));

      transText = OperatoTerms.t3(code, convParams);
    }

    if (this.isEmpty(transText) || transText == code) {
      transText = i18next.t(code);
    }

    return transText;
  }

  /**
   * alert 박스 보기 
   **************************************
   * @param {String} titleCode 
   * @param {String} textCode 
   * @param {String} type 
   * @param {String} confirmButtonCode 
   * @param {String} cancelButtonCode 
   * @returns {Object}
   */
  async showCustomAlert(titleCode, textCode, type, confirmButtonCode, cancelButtonCode) {

    // alert 정보 생성 
    let alertObj = {
      title: this.convertLanguageCode(titleCode),
      text: this.convertLanguageCode(textCode)
    };

    if (this.isNotEmpty(type)) {
      alertObj['type'] = type;
    }

    if (this.isNotEmpty(confirmButtonCode)) {
      alertObj['confirmButton'] = this.convertLanguageCode(confirmButtonCode);
    }

    if (this.isNotEmpty(cancelButtonCode)) {
      alertObj['cancelButton'] = this.convertLanguageCode(cancelButtonCode);
    }

    return await CustomAlert(alertObj);
  }

  /**
   * Document 내의 attribute 변화를 감지 및 로깅 (개발용)
   **************************************
   * @param {String} name 
   * @param {*} oldVal 
   * @param {*} newVal 
   */
  attributeChangedCallback(name, oldVal, newVal) {
    console.log(this.tagName, 'attribute change: ', name, oldVal, newVal);
    super.attributeChangedCallback(name, oldVal, newVal);
  }

  /**
   * 문서내 attribute, properties 변경 시 문서 update 조건 설정 
   ****************************************  
   * @param {Map} changeProperties 
   * @returns 
   */
  shouldUpdate(changeProperties) {
    var { isConnected = false } = { isConnected: this.isConnected };

    let retValue = false;
    if (!isConnected) {
      retValue = false;
    } else {
      if (this.is_page) {
        retValue = super.shouldUpdate(changeProperties);
      } else {
        retValue = true;
      }
    }

    return retValue;
  }

  /**
   * 내보내기 
   **************************************
   @ @param {Grist} 
   * @returns {Object} { header: headerSetting, data: data } or Not
   */
  async _exportableData(grist) {
    var headerSetting = grist._config.columns
      .filter(column => column.type !== 'gutter' && column.record !== undefined && column.imex !== undefined)
      .map(column => {
        return column.imex
      })

    let records = grist.data.records;

    var data = records.map(item => {
      return {
        id: item.id,
        ...grist._config.columns
          .filter(column => column.type !== 'gutter' && column.record !== undefined && column.imex !== undefined)
          .reduce((record, column) => {
            record[column.imex.key] = column.imex.key
              .split('.')
              .reduce((obj, key) => (obj && obj[key] !== 'undefined' ? obj[key] : undefined), item)
            return record
          }, {})
      }
    })

    // element 로 호출된 경우 리턴 없이 바로 xlsx 내보내기 
    if (this.is_element) {
      let extension = 'xlsx';

      store.dispatch({
        type: EXPORT,
        exportable: {
          extension,
          name: this.menu.title,
          data: { header: headerSetting, data: data }
        }
      })
    } else {
      return { header: headerSetting, data: data }
    }
  }


  /**
   * 모바일 디바이스 여부 
   **************************************
   * @return Boolean
   */
  get isMobile() {
    return isMobileDevice();
  }

  /**
   * HTML 문자열을 elements 로 반환 
   **************************************
   * @param {String} htmlString 
   * @returns {HTMLElement}
   */
  htmlToElement(htmlString) {
    var template = document.createElement('template');
    template.innerHTML = htmlString;
    var elements = template.content.childNodes;
    var element = elements[0];

    template.content.removeChild(element);

    return element;
  }

  /**
   * js 다이다믹 임포트 
   * module 이 미리 지정 되어 있어야 한다.
   **************************************
   * @param {String} module 
   * @param {String} url 
   */
  async dynamicImport(module, url) {
    // 페이지 import 요청 이벤트 전파
    document.dispatchEvent(new CustomEvent('dynamic-page-import-request', {
      detail: {
        module: module,
        url: url
      }
    }))
  }

  /**
   * 페이지를 이동 한다 .
   **************************************
   * @param {String} url 
   * @param {Object} params undefined or Object
   */
  pageNavite(url, params) {
    if (params) {
      navigate(`${url}/${encodeURI(JSON.stringify(params))}`)
    } else {
      navigate(url);
    }
  }

  /**
   * object, string, number, array 빈 값 여부 검사
   **************************************
   * @param {Object} param 
   * @returns {Boolean}
   */
  isEmpty(param) {
    if (param === undefined) {
      return true;
    } else if (param === null) {
      return true;
    } else if (typeof param === 'boolean') {
      return false;
    } else if (typeof param === 'string' || typeof param === 'number') {
      if (param == '') return true;
    } else if (Array.isArray(param)) {
      if (param.length == 0) return true;
    } else if (typeof param === 'object') {
      if (Object.keys(param).length == 0) return true;
    }
    return false;
  }

  /**
   * object, string, number, array 빈 값 isNot 여부 검사
   **************************************
   * @param {Object} param 
   * @returns {Boolean}
   */
  isNotEmpty(param) {
    if (param === undefined) {
      return false;
    } else if (param === null) {
      return false;
    } else if (typeof param === 'boolean') {
      return true;
    } else if (typeof param === 'string' || typeof param === 'number') {
      if (param != '') return true;
    } else if (Array.isArray(param)) {
      if (param.length > 0) return true;
    } else if (typeof param === 'object') {
      if (Object.keys(param).length >= 0) return true;
    }

    return false;
  }

  /**
   * 메뉴 파라미터의 값을 추출
   **************************************
   * @param {String} menuParamName 
   * @param {*} defaultValue 
   * @returns {*}
   */
  menuParamValue(menuParamName, defaultValue) {
    var paramValue = this.menu_params[menuParamName]
    return paramValue ? paramValue : defaultValue;
  }

  /**
   * 메뉴 파라미터의 값을 추출해 Object 로 변환후 return 
   **************************************
   * @param {String} menuParamName 
   * @param {*} defaultValue 
   * @returns {*}
   */
  menuParamValueToObject(menuParamName, defaultValue) {
    var paramValue = this.menuParamValue(menuParamName, defaultValue);
    return paramValue ? JSON.parse(paramValue) : defaultValue;
  }

  /**
   * rank 필드로 Array 순서를 정렬 한다. (Grist 버튼, 커스텀 버튼, 기본버튼 )
   **************************** 
   * @param {Array} arr 
   * @returns {Array}
   */
  arraySortByRankField(arr) {
    arr.sort((a, b) => {
      return a.rank - b.rank;
    });

    return arr;
  }

  /**
   * 페이지일때 context 데이터를 생성해 리턴 한다. 
   ***************************************** 
   * @returns {Object} {title : String, actions : Array, exportable : Object}
   */
  createPageContextObject() {
    let actions = [];
    // 커스텀 버튼 생성
    if (this.getCustomActions) {
      let customButtons = this.getCustomActions();
      if (customButtons) {
        actions.push(...customButtons);
      }
    }

    let basicButtons = {};
    // 기본 버튼 생성 
    if (this.getBasicActions) {
      basicButtons = this.getBasicActions();
      if (basicButtons.actions) {
        actions.push(...basicButtons.actions);
      }
    }

    // 버튼 순서 정렬 
    actions = this.arraySortByRankField(actions);

    let context = {
      title: this.menu.title,
      actions: actions
    };

    // 엑셀 export 버튼 
    if (basicButtons.exportable) {
      context['exportable'] = basicButtons.exportable;
    }
    return context;
  }

  /**
   * 버튼 엘리먼트를 생성해 리턴한다. 
   * ************************************** 
   * @param {String} buttonName 
   * @returns 
   */
  createButtonElement(buttonName) {
    let btnHtml = `<mwc-button raised label="${buttonName}" style="margin-left:7px;margin-top:7px;"></mwc-button>`
    let btnEle = this.htmlToElement(btnHtml);

    return btnEle;
  }


  /**
   * 페이지가 아니고 button_elements 가 있을때 html 을 리턴한다.
   ********************************* 
   * @returns {HTML}
   */
  getButtonHtml() {
    if ((this.is_page && this.isMultiLayoutPage() == false) || this.isEmpty(this.button_elements)) {
      return html``;
    }

    return html`
      <div id="button-container" class="button-container">
        ${this.button_elements}
      </div>
    `;
  }

  /**
   * logic 에 포함된 element 정보를 이용해 팝업을 연다 
   ************************************************* 
   * @description 
   * @param {String} title 팝업 이름 
   * @param {Object} logic { "module": '', "import": '', "tagname": '', "menu": '', size : ''}
   * @param {Object} paramData 
   * @param {String} id 레코드 선택시 팝업을 열때 parent_id 를 전달하기 위한 레코드 ID
   */
  async openDynamicPopup(title, logic, paramData, id) {

    await this.dynamicImport(logic.module, logic.import);

    let htmlText = `<${logic.tagname} route_name='${logic.menu}' parent_id='${id}'></${logic.tagname}>`;
    let htmlElements = this.htmlToElement(htmlText);
    htmlElements.open_params = paramData;

    openPopup(htmlElements, {
      backdrop: true,
      size: logic.size,
      title: title
    })
  }


  /**
   * paramter 에 부모 객체의 ID 값을 추가 한다 . 
   ************************************ 
   * @description 엘레멘트 로드시 부모 객체의 아이디가 데이터에 포함되어야 하는 경우가 있다.
   * @description menu param detail_parent_id 의 필드를 patches 리스트 또는 오브젝트에 반영 한다. 
   * @param {Array Or Object} patches 
   * @returns 
   */
  setParentIdFieldByElement(patches) {
    // 팝업의 경우 parent_id 필드를 찾아 데이터를 채워 준다.
    if (this.is_element) {
      // parent_id 변경 
      let detailParentId = this.menuParamValue('detail_parent_id');

      if (this.isNotEmpty(detailParentId)) {
        if (Array.isArray(patches)) {
          patches.forEach(element => {
            element[detailParentId] = this.parent_id;
          });
        } else {
          patches[detailParentId] = this.parent_id;
        }
      }
    }
    return patches;
  }

  /**
   * 단일 페이지가 아닌 여러 element 를 포함하는 layout 을 지워하는 페이지인지...
   * 메뉴 파라미터의 master-detail 만 봄.
   * TODO : 여러 레이아웃 이 추가 되면 변경 .
   ***********************************************
   * @description 이 결과 값으로 페이지가 기본 재공하는 버튼을 사용할지 element 에서 그리는 버튼을 사용할지 판단.  
   * @returns {Boolean}
   */
  isMultiLayoutPage() {
    if (this.menu_params['master-detail']) {
      return true;
    }

    return false;
  }

  /**
   * 메뉴 파라미터에서 서치폼 사용 여부를 리턴한다.
   * @returns {Boolean}
   */
  useSearchForm() {
    return this.menuParamValue('use-search-form', 'true') == 'true' ? true : false;
  }

  /**
   * 메뉴 파라미터에서 필터폼 사용 여부를 리턴한다.
   * @returns {Boolean}
   */
  useFilterForm() {
    return this.menuParamValue('use-filter-form', 'true') == 'true' ? true : false;
  }

  /**
   * 메뉴 파라미터에서 페이지 로드시 그리스트 auto-fetch 기능 사용 여부를 리턴한다.
   * @returns {Boolean}
   */
  useGristAutoFetch() {
    return this.menuParamValue('use-auto-fetch', 'true') == 'true' ? true : false;
  }


  /**
   * Rest 호출 뒤 Response 객체에 대한 토스트 메시지를 처리한다. 
   ************************ 
   * @param {Object} response 
   */
  restResponseMessage(response) {
    // 처리 결과 ....?
    if ([200, 201].includes(response.status)) {
      let message = this.convertLanguageCode('text.processed_msg');
      document.dispatchEvent(new CustomEvent('notify', { detail: { message } }))
    } else {
      let message = this.convertLanguageCode('text.processing_error_msg');
      document.dispatchEvent(new CustomEvent('notify', { detail: { message } }))
    }
  }
}