import { i18next, localize } from '@operato/i18n'
import { PageView } from '@operato/shell'

import { MetaChartMixin } from '../mixin/meta-chart-mixin'

/**
 * 기본 차트 페이지 - 검색 폼 / 차트
 */
export class BasicChartPage extends MetaChartMixin(localize(i18next)(PageView)) {
  static get properties() {
    return {}
  }

  constructor() {
    super();
  }
}

customElements.define('basic-chart-page', BasicChartPage)