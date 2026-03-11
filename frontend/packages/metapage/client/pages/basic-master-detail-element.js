import { MetaMasterDetailMixin } from '../mixin/meta-master-detail-mixin'

import { i18next, localize } from '@operato/i18n'
import { LitElement } from 'lit-element'


export class BasicMasterDetailElement extends MetaMasterDetailMixin(localize(i18next)(LitElement)) {


  async connectedCallback() {
    if (super.connectedCallback) {
      await super.connectedCallback();
    }

    // 코딩... 
  }
}

customElements.define('basic-master-detail-element', BasicMasterDetailElement)