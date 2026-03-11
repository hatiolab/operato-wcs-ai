import { GristRenderMixin } from './grist-render-mixin'

import { html } from 'lit-element'


export const MetaGristMixin = (superClass) => class extends GristRenderMixin(superClass) {

    /**
     * 화면 렌더링 
     * @returns HTML
     */
    render() {
        return html `
      ${this.getGridHtml ? html`${this.getGridHtml()} ` : html``}
      ${this.getButtonHtml ? html`${this.getButtonHtml()}` : html``}
    `
  }
}