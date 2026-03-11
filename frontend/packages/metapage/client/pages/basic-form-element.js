import { MetaFormMixin } from '../mixin/meta-form-mixin'

import { LitElement } from 'lit-element'
import { i18next, localize } from '@operato/i18n'

import '@operato/input/ox-input-code.js'

export class BasicFormElement extends MetaFormMixin(localize(i18next)(LitElement)) {
}


customElements.define('basic-form-element', BasicFormElement)