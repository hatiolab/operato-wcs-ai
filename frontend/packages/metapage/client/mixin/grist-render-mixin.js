import { CustomGristButtonMixin } from './custom-grist-button-mixin'

import { css, html } from 'lit-element'
import { CommonGristStyles } from '@operato/styles'

/**
 * 그리스트 관련 mixin
 *************************************************** 
 * @param {Object} superClass 
 * @returns 
 */
export const GristRenderMixin = (superClass) => class extends CustomGristButtonMixin(superClass) {


    /** 
     * 그리스트 스타일 
     */
    static getGristStyle() {
      return [
        CommonGristStyles,
        css `
      ox-grist {
        overflow-y: auto;
        flex: 1;
      }`
      ]
    }

    createGristConfig() {
      let gristConfig = [];

      // 페이지 사용 여부에 따라 그리드 내 페이지네이션 설정 
      let pagination = undefined;
      let gridMultipleSelect = true;

      let paramMultiSelect = this.menuParamValue('grid_multiple_select');
      let paramPagination = this.menuParamValue('pagination');
      // 그리드 멀티 선택 옵션 
      if (paramMultiSelect) {
        gridMultipleSelect = (paramMultiSelect === 'true');
      }

      // 페이지 사용 안함 
      if (this.menu.use_pagination == false) {
        pagination = { infinite: true };
      } else if (paramPagination) { // 페이지 옵션 이 menu_param 에 설정 됨 
        let pagesString = paramPagination.split(",");
        let pagesInt = [];

        for (var idx in pagesString) {
          pagesInt.push(parseInt(pagesString[idx]));
        }
        pagination = { pages: pagesInt };
      } else pagination = { pages: [20, 50, 100, 500] }; // TODO Settings에 저장하고 해당 값을 사용 

      gristConfig = {
        list: {
          fields: [this.menu.title_field, this.menu.desc_field],
        },
        pagination: pagination,
        rows: { selectable: { multiple: gridMultipleSelect }, appendable: this.use_add_button },
        sorters: [...this.sort_fields],
        columns: [
          { type: 'gutter', gutterName: 'dirty' },
          { type: 'gutter', gutterName: 'sequence' },
          { type: 'gutter', gutterName: 'row-selector', multiple: gridMultipleSelect }
        ]
      };
      return gristConfig;
    }

    /**
     * 공통 그리드 엘레멘트 
     */
    getGridHtml() {
      let grid_mode = this.grid_mode;

      let element;

      if (this.useGristAutoFetch()) {
        element = html `
                <ox-grist id="ox-grist" .config=${this.config} .mode=${grid_mode} auto-fetch .fetchHandler=${this.fetchHandler.bind(this)}>
                  ${this.getGridDetailHtml()}
                </ox-grist>
              `
      } else {
        element = html `
                <ox-grist id="ox-grist" .config=${this.config} .mode=${grid_mode} .fetchHandler=${this.fetchHandler.bind(this)}>
                  ${this.getGridDetailHtml()}
                </ox-grist>
              `
      }

      return element;
    }

    /**
     * 그리드 오토 패치 옵션 처리를 위해 그리드 상세 내용 html 생성 부분 분리.
     * @returns {HTML}
     */
    getGridDetailHtml() {
        return html `
          ${this.useFilterForm() 
          ? html`
            <div slot="headroom">
              <div id="filters"><ox-filters-form></ox-filters-form></div>
              <div id="sorters">
                <mwc-icon @click=${e=> {
                  const target = e.currentTarget
                  this.renderRoot.querySelector('#sorter-control').open({
                    right: 0,
                    top: target.offsetTop + target.offsetHeight
                  })
                }}
                >sort</mwc-icon>
                <ox-popup id="sorter-control">
                  <ox-sorters-control> </ox-sorters-control>
                </ox-popup>
              </div>
              ${this.getGridViewOption}
              <ox-record-creator id="add" ?hidden="${!this.use_add_button}" .callback=${this.recordCreationCallback.bind(this)}> <button><mwc-icon>add</mwc-icon></button></ox-record-creator>
            </div>
            ` 
          : html``}`
  }

  /**
   * 그리드 보기 옵션 버튼을 그린다.
   */
  get getGridViewOption() {
      let grid_mode = this.grid_mode;

      if (this.grid_view_options.length == 1) {
          return html``;
      }

      return html`
      <div id="modes">
          ${this.grid_view_options.includes('GRID') ? html`<mwc-icon @click="${() => (this.grid_mode = 'GRID')}"
              ?active="${grid_mode == 'GRID'}">grid_on</mwc-icon>` : ``}
          ${this.grid_view_options.includes('LIST') ? html`<mwc-icon @click="${() => (this.grid_mode = 'LIST')}"
              ?active="${grid_mode == 'LIST'}">format_list_bulleted</mwc-icon>` : ``}
          ${this.grid_view_options.includes('CARD') ? html`<mwc-icon @click="${() => (this.grid_mode = 'CARD')}"
              ?active="${grid_mode == 'CARD'}">apps</mwc-icon>` : ``}
      </div>
    `
  }


  /**
   * 서치 폼 가져오기 
   */
  get searchForm() {
    // 메뉴 파라미터 옵션에 따라 다른 처리..
    if (this.useFilterForm()) {
      return this.shadowRoot.querySelector('ox-filters-form')
    } else {
      return undefined;
    }
  }

  /**
   * 그리드 가져오기 
   */
  get grist() {
    if(!this.shadowRoot){
      return undefined;
    } else if (!this.shadowRoot.querySelector){
      return undefined;
    }
    return this.shadowRoot.querySelector('ox-grist')
  }

  /**
   * 그리드에서 신규/ 변경 된 레코드를 가져온다 
   * @returns 
   */
  getPatches() {
    let cudRecords = this.grist.dirtyRecords;

    if (cudRecords && cudRecords.length) {
      let patches = cudRecords.map(record => {
        record.cud_flag_ = (record.__dirty__ == 'M' ? 'u' : 'c');
        delete record['__dirty__'];
        return record;
      });

      return patches;
    }

    return undefined;
  }
}