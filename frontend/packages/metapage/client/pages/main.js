import { html } from 'lit-element'
import { connect } from 'pwa-helpers/connect-mixin.js'
import { store, PageView } from '@things-factory/shell'

class MetapageMain extends connect(store)(PageView) {
  static get properties() {
    return {
      metapage: String
    }
  }
  render() {
    return html `
      <section>
        <h2>Metapage</h2>
      </section>
    `
  }

  stateChanged(state) {
    this.metapage = state.metapage.state_main
  }
}

window.customElements.define('metapage-main', MetapageMain)