import { MetaGristChartMixin } from '../mixin/meta-grist-chart-mixin'

import { PageView } from '@operato/shell'
import { i18next, localize } from '@operato/i18n'

/**
 *  그리드 & 차트 페이지 - 검색 폼 / 그리드 / 차트
 */
export class BasicGristChartPage extends MetaGristChartMixin(localize(i18next)(PageView)) {

  async connectedCallback() {
    if (super.connectedCallback) {
      await super.connectedCallback();
    }

    // 코딩... 
  }
}

customElements.define('basic-grist-chart-page', BasicGristChartPage)