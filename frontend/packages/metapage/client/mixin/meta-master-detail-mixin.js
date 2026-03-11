import { GristRenderMixin } from './grist-render-mixin'
import { SearchFormRenderMixin } from './search-form-render-mixin'

import { css, html } from 'lit-element'

export const MetaMasterDetailMixin = (superClass) => class extends SearchFormRenderMixin(GristRenderMixin(superClass)) {

    static get styles() {
      let styles = [
        css `
      :host {
        display: flex;
        flex-direction: column;
        overflow-x: auto;
      }
      .container {
        flex: 1;
        display: grid;
        overflow: hidden;
      }
      .container_detail {
        background-color: var(--main-section-background-color);
        display: flex;
        flex-direction: column;
        flex: 1;
        overflow-y: auto;
      }

      h2 {
        padding: var(--subtitle-padding);
        font: var(--subtitle-font);
        color: var(--subtitle-text-color);
        border-bottom: var(--subtitle-border-bottom);
      }

      .container_detail h2 {
        margin: var(--grist-title-margin);
        border: var(--grist-title-border);
        font: var(--grist-title-font);
        color: var(--secondary-color);
      }

      .container_detail h2 mwc-icon {
        vertical-align: middle;
        margin: var(--grist-title-icon-margin);
        font-size: var(--grist-title-icon-size);
        color: var(--grist-title-icon-color);
      }

      h2 {
        padding-bottom: var(--grist-title-with-grid-padding);
      }

      `
      ];

      // 그리스트 스타일 추가 
      if (this.getGristStyle) {
        styles.push(...this.getGristStyle());
      }

      // 버튼 콘테이너 스타일 추가 (엘리먼트로 로드시 사용 )
      if (this.getButtonContainerStyle) {
        styles.push(...this.getButtonContainerStyle());
      }

      return styles;
    }

    /**
     * 화면 렌더링 
     * @returns HTML
     */
    render() {
        let params = this.menuParamValueToObject('master-detail', '{}');

        let type = params.type;
        let size = params.size;

        // 레이아웃 타입에 따라 상하 좌우 그리드 설정 
        let layoutContainerStyle = '';
        if (type == 'left_right') {
          layoutContainerStyle += `grid-template-columns:${size};`;
        } else {
          layoutContainerStyle += `grid-template-rows:${size};`;
        }

        // 상세 element import 
        (async() => await this.dynamicImport(params.module, params.import))();

        // 상세 element 생성 
        let detailHtml = `<${params.tagname} id="${params.id}" route_name="${params.menu}" style='flex:1;'></${params.tagname}>`;
        let detailElement = this.htmlToElement(detailHtml);

        this.detailId = params.id;

        // 렌더링 
        return html `
      ${this.getSearchFormHtml ? html`${this.getSearchFormHtml()}` : html``}
      <div id="container" class="container" style="${ layoutContainerStyle }">
        <div class="container_detail">
          <h2><mwc-icon>list_alt</mwc-icon>${this.menu.title}</h2>
          ${this.getGridHtml()}
          ${this.getButtonHtml ? html`${this.getButtonHtml()}` : html``}
        </div>
        <div class="container_detail">
          <h2 id='detail_title'><mwc-icon>list_alt</mwc-icon>${this.convertLanguageCode(`menu.${params.menu}`)}</h2>
          ${detailElement}
        </div>
      </div>
    `
  }

  /**
   * 오버라이드 
   *******************************
   * @description 디테일의 리스트를 초기화 하기 위해 
   */
  async fetchHandler(fetchParam){
    if(this.detailId){
      let detail = this.shadowRoot.querySelector(`#${this.detailId}`);
      if(detail && detail.grist && detail.grist.data){
        detail.grist.data = {};
      }
    }
    
    return await super.fetchHandler(fetchParam);
  }
}