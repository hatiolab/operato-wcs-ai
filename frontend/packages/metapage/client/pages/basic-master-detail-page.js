import { MetaMasterDetailMixin } from '../mixin/meta-master-detail-mixin'

import { PageView } from '@operato/shell'
import { i18next, localize } from '@operato/i18n'

export class BasicMasterDetailPage extends MetaMasterDetailMixin(localize(i18next)(PageView)) {


  async connectedCallback() {
    if (super.connectedCallback) {
      await super.connectedCallback();
    }

    // 코딩... 
  }
}

customElements.define('basic-master-detail-page', BasicMasterDetailPage)