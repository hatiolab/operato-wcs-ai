import { BasicButtonMixin } from './basic-button-mixin'

import { CommonButtonStyles } from '@operato/styles'
import { css } from 'lit-element'

export const CustomButtonMixin = (superClass) => class extends BasicButtonMixin(superClass) {


  /** 
   * 버튼 컨테이너 스타일 
   */
  static getButtonContainerStyle() {
    return [css`
      .button-container {
        padding: var(--button-container-padding);
        margin: var(--button-container-margin);
        text-align: var(--button-container-align);
        background-color: var(--button-container-background);
        height: var(--button-container-height);
        text-align: right;
        padding-right: 12px;
      }
    `]
  }

  /**
   * Page Context 생성에서 사용될 버튼 액션 정의
   ************************************************
   * @description 버튼 타입중 Grid 리스트 옵션을 선택 한 경우 : get grist() 함수가 페이지내에 없으면 그리지 않는다.
   * @description 버튼 타입중 Search-From 을 선택 한 경우에는 : get searchForm()  함수가 없으면 그리지 않는다.
   * @returns {Array} 
   */
  getCustomActions() {
    let actions = [];

    // 멀티 레이아웃이면 기본 버튼을 사용하지 않음
    if (this.isMultiLayoutPage()) {
      return actions;
    }

    // 버튼과 function 연결 
    let configButtons = this.filterCustomButton();
    configButtons.forEach((currentElement) => {

      // 지원되지 않는 타입 확인 continue
      if (this.isCurrentNotSupportedType(currentElement)) {
        return;
      }

      // custom 버튼 함수 
      currentElement.action = () => {
        this.customBtnEventHandler(currentElement);
      }

      if (currentElement.style) {
        Object.assign(currentElement, CommonButtonStyles[currentElement.style]);
      }

      actions.push(currentElement);
    });

    return actions;
  }

  /**
   * 커스텀 버튼 element를 생성 한다. 
   *********************************** 
   * 문서가 페이지로 로딩된 경우에는 빈 배열 리턴 아니면 버튼을 생성해 리턴한다. 
   * @returns {Array}
   */
  getCustomButtons() {
    if (this.is_page && this.isMultiLayoutPage() == false) return [];

    let configButtons = this.filterCustomButton();
    let buttons = [];

    configButtons.forEach((currentElement) => {
      // 지원되지 않는 타입 확인 continue
      if (this.isCurrentNotSupportedType(currentElement)) {
        return;
      }

      let btnEle = this.createButtonElement(currentElement.title);
      btnEle["rank"] = currentElement.rank;

      // custom 버튼 함수 
      btnEle.onclick = () => {
        this.customBtnEventHandler(currentElement);
      }

      buttons.push(btnEle);
    });

    return buttons;
  }

  /**
   * 메타 actions 에서 커스텀 버튼 조건으로 filtering 
   ************************ 
   * @returns {Array} 
   */
  filterCustomButton() {
    return this.actions.filter(action => action.type && !action.type.startsWith("grid") && !action.type.startsWith("basic")
      && action.type.startsWith("service-form") && action.type.startsWith("popup-form"));
  }

  /**
   * 버튼의 타입에 따라 현재 문서에서 지원 되지 않는 기능을 확인한다.
   ***************************************  
   * @description list, selected 타입은 그리스트가 필수  ( get grist)
   * @description search-form 타입은 서치폼이 필수  ( get searchForm)
   * @param {Object} action 
   * @returns 
   */
  isCurrentNotSupportedType(action) {
    // 그리드 관련 타입 continue
    if ((action.type.endsWith('list') || action.type.endsWith('selected')) && !this.grist) {
      return true;
    }

    // 서치폼 관련 타입 continue
    if (action.type.endsWith('search-form') && !this.searchForm) {
      return true;
    }
    return false;
  }



  /**
   * 커스텀 버튼 핸들러 
   **************************************** 
   * @param {Object} customAction
   */
  async customBtnEventHandler(customAction) {
    let customLogic = {};

    // 커스컴 서비스 , controller 호출 
    if (customAction.type.startsWith('service-')) {
      customLogic['action'] = 'service';
      customLogic['param_data'] = customAction.type.replace("service-", "");
      customLogic['logic'] = customAction.logic;
    } else if (customAction.type.startsWith('page-link-')) { // PAGE 이동 
      customLogic['action'] = 'page';
      customLogic['param_data'] = customAction.type.replace("page-link-", "");
      customLogic['logic'] = customAction.logic;
    } else if (customAction.type.startsWith('popup-link-')) { // POPUP 열기 
      customLogic['action'] = 'popup';
      customLogic['param_data'] = customAction.type.replace("popup-link-", "");
      customLogic['logic'] = JSON.parse(customAction.logic);
    }
    customLogic['method'] = customAction.method;

    // 파라미터로 전달할 데이터 추출 
    let paramData = await this.getCustomButtonParams(customLogic.param_data);

    // 파라미터가 없으면 리턴 
    if (!paramData) {
      return;
    }

    // 파라미터가 none 으로 설정 되면 .
    if (paramData == 'ok') {
      paramData = undefined;
    }

    // method 가 PAGE 이면 페이지 이동 
    if (customLogic.action == 'page') {
      this.pageNavite(customLogic.logic, paramData);
    } else if (customLogic.action == 'popup') {
      await this.openDynamicPopup(customAction.title, customLogic.logic, paramData);
    } else if (customLogic.action == 'service') {
      // REST 호출 (dynamic 서비스 or REST 서비스 )

      if (customLogic.logic.indexOf(':id') >= 0 && this.isNotEmpty(this.parent_id)) {
        customLogic.logic = customLogic.logic.replace(':id', this.parent_id);
      }

      if (customLogic.param_data == 'form-param') {
        customLogic.logic += `?${paramData}`;
        await this.requestRestService(customLogic.method, customLogic.logic);
      } else {
        await this.requestRestService(customLogic.method, customLogic.logic, paramData);
      }
    }
  }

  /**
   * targetData 로 파라미터를 생성 한다. 
   *************************************************************
   * @param {String} targetData [ none, search-form, selected, list, form-map, form-param]
   * @return {Array}
   */
  async getCustomButtonParams(targetData) {
    if (targetData == 'none') {
      return 'ok';
    }

    // 서치폼 값을 파라미터로 사용 
    if (targetData == 'search-form') {
      let formFilters = [];
      // search-form , ox-filter-form 두가지 지원 
      if (this.searchForm.tagName.toLowerCase() == 'search-form') {
        formFilters = await this.searchForm.getQueryFilters();
      } else {
        formFilters = this.searchForm.value;
      }

      return [...this.search_hidden_fields, ...formFilters];
    }

    // 리스트 전체를 파라미터로 사용 
    if (targetData == 'list') {
      return this.grist.data.dirtyData.records;
    }

    // 리스트에 선택된 데이터를 파라미터로 사용 
    if (targetData == 'selected') {
      let selectedRows = this.grist.selected;

      if (!selectedRows || selectedRows.length == 0) {
        this.showCustomAlert('title.info', 'text.select_item');

        return undefined;
      }

      return selectedRows;
    }

    // 폼뷰 데이터를 파라미터로 사용 
    if (targetData == 'form-map' || targetData == 'form-param') {
      if (!this.getFormViewData) {
        return undefined;
      }

      let formMap = this.getFormViewData();

      // form-map 은 데이터를 그대로 리턴 
      if (targetData == 'form-map') {
        return formMap;
      }

      // form-param 은 문자열로 가공된 결과를 리턴 
      let paramString = '';
      Object.keys(formMap).forEach(key => {
        paramString += `${key}=${formMap[key]}&`;
      })
      return paramString;
    }

    return undefined;
  }

}