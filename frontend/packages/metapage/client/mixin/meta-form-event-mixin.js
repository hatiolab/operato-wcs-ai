
import { CustomButtonMixin } from './custom-button-mixin'
import { css, html } from 'lit-element'

import '../component/operato-input-editor'

/**
 * 메뉴에 설정된 폼 내용을 기준으로 화면을 그린다.
 * 폼 내용으로 버튼 이벤트를 처리 한다. ( 서치폼을 사용 하지 않고 그린 폼 내용을 기준으로 검색 )
 * @param {Object} superClass 
 * @returns 
 */
export const MetaFormEventMixin = (superClass) => class extends CustomButtonMixin(superClass) {

  static get styles() {
    let styles = [
      css`
      :host {
        display: flex;
        flex-direction: column;
        overflow-x: overlay;
        background-color: var(--main-section-background-color);
      }

      #container {
        display: grid;
        grid-template-columns: 0.5fr 2fr;
        grid-auto-rows: min-content;
        grid-gap: var(--record-view-gap);
        padding: var(--record-view-padding);
        background-color: var(--record-view-background-color);        

        overflow-y: auto;
        flex: 1;
      }
      
      label {
        display: flex;
        align-items: center;
        position: relative;
        text-transform: capitalize;

        padding: var(--record-view-item-padding);
        border-bottom: var(--record-view-border-bottom);
        font: var(--record-view-label-font);
        color: var(--record-view-label-color);
      }

      label mwc-icon {
        display: none;
      }

      label[editable] mwc-icon {
        display: inline-block;
        font-size: var(--record-view-label-icon-size);
        opacity: 0.5;
      }

      operato-input-editor {
        border-top: none;
        border-bottom: var(--record-view-border-bottom);
        background-color: transparent;
      }

      operato-input-editor[editing='true'] {
        border-bottom: var(--record-view-edit-border-bottom);
      }

      operato-input-editor:focus-within {
        color: var(--record-view-focus-color);
        font-weight: bold;
      }      
      `
    ]


    // 버튼 콘테이너 스타일 추가 (엘리먼트로 로드시 사용 )
    if (this.getButtonContainerStyle) {
      styles.push(...this.getButtonContainerStyle());
    }

    return styles;
  }



  async connectedCallback() {
    // 조회시 callBack 함수 지정 
    this.fetch_callback = this.responseDataSet;

    if (super.connectedCallback) {
      await super.connectedCallback();
    }
  }


  /**
   * 기본 버튼 및 커스텀 버튼 생성 code 데이터 조회 
   */
  async firstUpdated() {
    // 기본 버튼 및 커스텀 버튼 생성 
    if (super.firstUpdated) {
      await super.firstUpdated();
    }
  }

  /**
   * 기본 버튼을 사용 하기 위해 기본 버튼 필더 override
   *********************************** 
   * @description reset, show, refresh 버튼 만 사용 가능 
   * @returns 
   */
  filterBasicButton() {
    return this.actions.filter(action => action.name == 'reset' || action.name == 'show' || action.name == 'refresh');
  }

  /**
   * 커스텀 버튼을 사용 하기 위해 커스텀 버튼 필터 override
   ************************ 
   * @description service-from... , popup-form... 만 사용 가능 
   * @returns {Array} 
   */
  filterCustomButton() {
    return this.actions.filter(action => action.type && (action.type.startsWith("service-form") || action.type.startsWith("popup-form")));
  }


  render() {
    var columns = this.form_fields.filter(column => !column.hidden); // 그리드 설정 
    var rowIndex = 0; // ????

    return html`
        <div id='container'>
          ${columns.map(column => {
            let { editable, mandatory } = column.record
              
            return html`
                <label ?editable=${editable}><span>${mandatory ? '*' : ''}${column.header}</span>
                  <mwc-icon>edit</mwc-icon>
                </label>
                <operato-input-editor id=${column.name} .column=${column} rowIndex=${++rowIndex} editable=${editable}></operato-input-editor>
          `
      })}
        </div>
        ${this.getButtonHtml ? html`${this.getButtonHtml()}` : html``}
      `
  }



  /**
   * 조회 결과 셋 
   ***************************
   * @param {Object} fetchResult { total: Number, records: Object }
   */
  responseDataSet(fetchResult) {
    let resObj = fetchResult.records;

    if(Array.isArray(resObj)){
      resObj = resObj[0];
    } 

    Object.keys(resObj).forEach(key => {
      let element = this.shadowRoot.querySelector(`#${key}`);

      if (this.isNotEmpty(element)) {
        element.setValue(resObj[key]);
      }
    });
  }

  /**
   * 화면에서 그려진 데이터 중 변경된 내용을 가져온다 .
   * @returns {Object} 
   */
  getFormViewData(){
    // operato-input-editor 전부 가져오기 
    let inputObject = this.shadowRoot.querySelectorAll('operato-input-editor');
    let params = {};

    // 변경 여부 및 저장 파라미터 셋팅 
    inputObject.forEach(input => {
      if(!this.isEmpty(input._dirtyValue)){
        let formConfig = this.form_fields.filter(x=> x.name == input.id)[0];

        // 저장시 무시 여부, 수정 가능 여부 를 체크해 무시 한다. 
        if(this.isNotEmpty(formConfig) && formConfig.save_ignore == false && formConfig.record.editable == true){
          params[input.id] = input._dirtyValue;
        }
      }
    })

    return params;
  }


  /**
   * Button 조회 
   */
  show(){
    // 전체 필드의 데이터 추출 
    let inputList = this.shadowRoot.querySelectorAll('operato-input-editor');
    let inputValues = {};

    inputList.forEach(x=>{
      let value = x._getValue;

      if(this.isNotEmpty(value)){
        inputValues[x.id] = value;
      }
    })

    console.log(inputValues);
    
    let value = {};

    inputList.forEach(x=>{

      if(x.column.type == 'select-combo'){
        value[x.id] = [];

        for(var idx = 0 ; idx < 5 ; idx++){
          let option = Math.random().toString(36).substring(2);
          value[x.id].push({name: option, value:option})
        }

      } else {
        value[x.id] = Math.random().toString(36).substring(2);
      }
    })

    let result = {
      records: value
    }

    this.responseDataSet(result);
  }

  /**
   * Button 새로고침 call this.show 
   */
  refresh (){
    this.show();
  }

  /**
   * Button 초기화 
   **************
   * @description 인풋 필드 비우기 
   */
  reset(){
    let logic = this.getButtonActionLogic('reset');

    // operato-input-editor 전부 가져오기 
    let inputList = this.shadowRoot.querySelectorAll('operato-input-editor');

    inputList.forEach( x=>{
      // 제외 필드 
      if(this.isNotEmpty(logic) && logic.except_fields.includes(x.id)){
        console.log('except : ' + x.id);
        return;
      }
      
      x.clear();
    })
  }

  /**
   * 메뉴 메타 정보에서 버튼 이름에 해당 하는 로직 정보를 추출 한다.
   ********************************************** 
   * @param {String} name 
   * @returns {Object}
   */
  getButtonActionLogic(name){
    let actions = this.actions.filter( x=> x.name == name);

    if(this.isEmpty(actions)) return undefined;

    let action = actions[0];
    if(this.isEmpty(action.logic)) return undefined;

    return JSON.parse(action.logic);
  }
}

