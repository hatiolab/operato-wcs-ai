import { css, html } from 'lit-element'

export const SearchFormRenderMixin = (superClass) => class extends superClass {
  /**
   * 서치폼 스타일 
   */
  static getSearchFormStyle() {
    return [
      css`
        search-form {
          overflow: visible;
        }
      `
    ]
  }

  async firstUpdated() {
    // 서치폼 사용시 체크박스 3가지 상태는 기본 값이 아니다..
    // 속성 추가... 
    this.search_form_fields.forEach(current => {
      if (current.type == 'checkbox') {
        current['attrs'] = ['indeterminate'];
      }
    })

    if (super.firstUpdated) {
      await super.firstUpdated();
    }
  }

  /**
   * 서치폼 렌더 정보 
   */
  getSearchFormHtml() {

    if (this.useSearchForm()) {
      return html`
              <search-form id="search-form" .fields=${this.search_form_fields} @submit=${e => (this.grist ? this.grist.fetch() :
          this.fetchHandler(this.searchForm))}>
              </search-form>`
    } else {
      return html``;
    }
  }

  /**
   * 서치 폼 가져오기 
   */
  get searchForm() {
    // 메뉴 파라미터 옵션에 따라 다른 처리..
    if (this.useSearchForm()) {
      return this.shadowRoot.querySelector('search-form')
    } else {
      return undefined;
    }

  }
}
