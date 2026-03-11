import { MetaFormMixin } from './../mixin/meta-form-mixin'

import { PageView } from '@operato/shell'
import { i18next, localize } from '@operato/i18n'

export class BasicFormPage extends MetaFormMixin(localize(i18next)(PageView)) { }

customElements.define('basic-form-page', BasicFormPage)