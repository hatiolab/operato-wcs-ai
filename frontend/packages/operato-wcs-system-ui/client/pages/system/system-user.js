import { html, css } from 'lit-element'
import { connect } from 'pwa-helpers/connect-mixin.js'
import { store, PageView } from '@things-factory/shell'
import { ScrollbarStyles, CommonButtonStyles } from '@things-factory/styles'
import { i18next, localize } from '@things-factory/i18n-base'

class SystemUser extends connect(store)(PageView) {
  static get styles() {
    return [
      css `
        :host {
          display: flex;
        }
      `
    ]
  }

  static get properties() {
    return {
      domainName: String
    }
  }

  get context() {
    return {
      title: `사용자 관리`,
      actions: [{
        title: i18next.t('button.delete'),
        ...CommonButtonStyles.delete
      }, {
        title: i18next.t('button.save')
      }]
    }
  }

  render() {
    return html `
      <section>
        <h2>사용자 관리</h2>
      </section>
    `
  }

  stateChanged(state) {
    // TODO 
  }
}

window.customElements.define('system-user', SystemUser)