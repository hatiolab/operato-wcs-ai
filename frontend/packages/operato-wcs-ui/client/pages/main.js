import { html, css } from 'lit-element'
import { connect } from 'pwa-helpers/connect-mixin.js'
import { store, PageView } from '@things-factory/shell'

import logo from '../../assets/images/hatiolab-logo.png'

class OperatoWcsUiMain extends connect(store)(PageView) {
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
      operatoLogis: Object
    }
  }

  get context() {
    return {
      title: `Home`
    }
  }

  render() {
    return html `
      <h2>Home</h2>
    `
  }

  stateChanged(state) {
    // this.operatoLogisManager = state.operatoLogisManager.state_main
  }
}

window.customElements.define('operato-wcs-ui-main', OperatoWcsUiMain)