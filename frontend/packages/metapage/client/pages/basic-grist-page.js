import { MetaGristMixin } from '../mixin/meta-grist-mixin'

import { PageView } from '@operato/shell'
import { i18next, localize } from '@operato/i18n'

export class BasicGristPage extends MetaGristMixin(localize(i18next)(PageView)) {


  async connectedCallback() {
    if (super.connectedCallback) {
      await super.connectedCallback();
    }

    // 코딩... 
  }
}

customElements.define('basic-grist-page', BasicGristPage)