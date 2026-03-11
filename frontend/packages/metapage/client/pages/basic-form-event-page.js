import { MetaFormEventMixin } from './../mixin/meta-form-event-mixin'

import { PageView } from '@operato/shell'
import { i18next, localize } from '@operato/i18n'

export class BasicFormEventPage extends MetaFormEventMixin(localize(i18next)(PageView)) { }

customElements.define('basic-form-event-page', BasicFormEventPage)