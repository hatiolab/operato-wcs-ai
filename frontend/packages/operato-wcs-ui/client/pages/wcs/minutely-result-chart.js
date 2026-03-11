import { css, html } from 'lit-element'

import { CommonGristStyles } from '@operato/styles'
import { i18next, localize } from '@operato/i18n'

import { PageView } from '@things-factory/shell'
import '@things-factory/form-ui'
import { MetaChartMixin } from '@things-factory/metapage'

/**
 * WCS > 10분대별 실적 차트
 */
export class MinutelyResultChart extends MetaChartMixin(localize(i18next)(PageView)) {
  static get properties() {
    return {}
  }

  static get styles() {
    return [
      CommonGristStyles,
      css `
        :host {
          display: flex;
          flex-direction: column;
          overflow: hidden;
        }

        .container {
          overflow-y: auto;
          flex: 0.95;
        }
      `
    ]
  }

  constructor() {
    super();
  }

  /**
   * 화면 렌더링 
   * @returns HTML
   */
  render() {
    return html `
      ${this.getChartHtml}
    `
  }

  /**
   * 타이틀 / 버튼 설정 
   */
  get context() {
    return this.createPageContextObject();
  }
}

customElements.define('minutely-result-chart', MinutelyResultChart)