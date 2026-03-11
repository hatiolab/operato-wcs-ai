import { css, html, LitElement } from 'lit-element'
import { i18next, localize } from '@operato/i18n'
import { ScrollbarStyles } from '@operato/styles'
import { SearchFormRenderMixin } from '../mixin/search-form-render-mixin'

import '@operato/input/ox-input-code.js'

export class BasicCodeEditorElement extends SearchFormRenderMixin(localize(i18next)(LitElement)) {
    static get properties() {
      return {
        code_value_org: String,
        code_value_new: String
      }
    }

    static get styles() {
      let styles = [
        ScrollbarStyles,
        css `
      :host {
        display: flex;
        flex-direction: column;
        overflow-x: overlay;
        background-color: var(--main-section-background-color);
      }
      legend {
        margin:10px;
        text-transform: capitalize;
        padding: var(--legend-padding);
        font: var(--legend-font);
        color: var(--legend-text-color);
        border-bottom: var(--legend-border-bottom);
      }
      ox-input-code {
        margin:10px;
        overflow-y: auto;
        flex: 1;
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
      this.fetch_callback = this.codeEditorDataSet;

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
     * 기본 버튼을 사용 하기 위해 기본 버튼 필더 override
     *********************************** 
     * @returns 
     */
    filterBasicButton() {
      return this.actions.filter(action => action.type && action.type.startsWith("basic"));
    }

    render() {
        return html `
      <legend>${this.menu.title}</legend>
      ${this.getSearchFormHtml ? html`${this.getSearchFormHtml()}` : html``}
      <ox-input-code mode="javascript" value=${this.code_value_new} tab-size="4"></ox-input-code>
      ${this.getButtonHtml ? html`${this.getButtonHtml()}` : html``}
    `;
  }

  /**
   * 코드 에디터 
   */
  get getCodeEditor() {
    return this.shadowRoot.querySelector('ox-input-code')
  }

  /**
   * 에디터에 값 설정
   ***************************
   * @param {Object} fetchResult { total: Number, records: Object }
   */
  codeEditorDataSet(fetchResult) {
    let codeField = this.menuParamValue('code-editor-value-field');
    let resData = fetchResult.records;
    let codeValue = this.isEmpty(codeField) ? resData : resData[codeField];
    this.code_value_new = this.code_value_org = codeValue;
  }

  /**
   * 저장 버튼 클릭 핸들러 
   */
  async save() {
    if (this.code_value_org == this.getCodeEditor.value) {
      AnywareWcsUtil.showCustomAlert('title.info', 'text.NOTHING_CHANGED', 'info', 'button.confirm');
    } else {
      let saveObject = { id: this.parent_id };
      saveObject[this.menuParamValue('code-editor-value-field')] = this.getCodeEditor.value;
      await this.restPut(this.saveUrl, saveObject);
    }
  }
}

customElements.define('basic-code-editor-element', BasicCodeEditorElement)