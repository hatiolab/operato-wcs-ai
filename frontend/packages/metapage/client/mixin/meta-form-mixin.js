import { SearchFormRenderMixin } from './search-form-render-mixin'
import {CustomButtonMixin} from './custom-button-mixin'
import { css, html } from 'lit-element'

import '../component/operato-input-editor'

/**
 * 메뉴에 설정된 폼 내용을 기준으로 화면을 그린다. 
 * @param {Object} superClass 
 * @returns 
 */
export const MetaFormMixin = (superClass) => class extends SearchFormRenderMixin(CustomButtonMixin(superClass)) {

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

    // 메뉴 파라미터 서치폼 사용 여부에 따라 조회....
    if (this.useSearchForm()) {
      this.searchForm.submit();
    } else {
      this.fetchHandler();
    }
  }

  /**
   * 기본 저장 버튼을 사용 하기 위해 기본 버튼 필더 override
   *********************************** 
   * @returns 
   */
  filterBasicButton() {
    return this.actions.filter(action => action.name == 'save');
  }

  render() {
    var columns = this.form_fields.filter(column => !column.hidden); // 그리드 설정 
    var rowIndex = 0; // ????

    return html`
        ${this.getSearchFormHtml ? html`${this.getSearchFormHtml()}` : html``}
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

    Object.keys(resObj).forEach(key => {
      let element = this.shadowRoot.querySelector(`#${key}`);

      if (this.isNotEmpty(element)) {
        element.setValue(resObj[key]);
      }
    });
  }

  /**
   * 저장 버튼 오바리드 
   */
  async save() {

    let params = this.getFormViewData();

    // 변경 된게 없으면 메시지 처리 
    if(!params || Object.keys(params).length == 0){
      this.showCustomAlert('title.info', 'text.NOTHING_CHANGED');
      return;
    }

    params = this.setParentIdFieldByElement(params);

    let response = await this.restPut(this.saveUrl, params);
    this.restResponseMessage(response);
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
}

