import { MetaGristMixin } from '../mixin/meta-grist-mixin'

import { LitElement } from 'lit-element'
import { i18next, localize } from '@operato/i18n'


export class BasicGristElement extends MetaGristMixin(localize(i18next)(LitElement)) {


  async connectedCallback() {
    if (super.connectedCallback) {
      await super.connectedCallback();
    }

    // 코딩... 
  }
}

customElements.define('basic-grist-element', BasicGristElement)