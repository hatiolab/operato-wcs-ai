import '@material/mwc-icon'
import '@things-factory/barcode-ui'

import { LitElement, css, html } from 'lit-element'
import { getEditor } from '@operato/data-grist'

export class OperatoInputEditor extends LitElement {
  static get styles() {
    return [
      css`

      ox-input-code {
        display: grid;
        height: 31vh;
        margin-bottom:7px;
        overflow: auto;
      }

      textarea {
        flex: 1;
        resize: none;
        border-color: var(--primary-color);
        margin-bottom:7px;
        height: 20vh;
        outline: none;
      }
      `
    ]
  }
  static get properties() {
    return {
      column: Object,
      rowIndex: Number,
      editable: false
    }
  }

  render() {
    return html`
      ${this.getOperatoEditor()}
    `
  }

  /**
   * 인풋에 값을 비교해 변경 되었으면 값을 넘겨주고, 변경 된게 없으면 '' 를 리턴한다. 
   */
  get _dirtyValue() {
    if (this.column.type == 'code-editor' || this.column.type == 'textarea') {
      if (this.getInput().value == this.getInput().org_value) {
        return '';
      }

      return this.getInput().value;
    } else {
      return this.getInput()._dirtyValue;
    }
  }

  get _getValue() {
    let element = this.getLeafInput();
    return element.value;
  }

  /**
   * 값을 에디터에 전달 한다.
   * readonly 처리를 여기서 한다..
   ****************************** 
   * @param {String} newVal 
   */
  setValue(newVal) {

    if (this.column.type == 'select-combo') {
      this.clear();

      let initValue = '';
      let optionHtml = '';
      for (let idx = 0; idx < newVal.length; idx++) {
        if (idx == 0) {
          initValue = newVal[idx].value;
        }
        optionHtml += `<div option value="${newVal[idx].value}">${newVal[idx].name}</div>`;
      }
      this.getLeafInput().innerHTML = optionHtml;
      this.getInput().value = initValue;

    } else {
      this.getInput().value = newVal;
      this.getInput().org_value = newVal;
    }

    this.setReadOnly();
  }


  /**
   * 모든 내용을 지운다. 
   */
  clear() {
    this.getInput().value = '';
    this.getInput().org_value = '';

    // select 콤보의 경우 option 도 삭제 
    if (this.column.type == 'select') {
      let element = this.getLeafInput();
      element.options.length = 0;
    } else if (this.column.type == 'select-combo') {
      let element = this.getLeafInput();
      element.innerHTML = '';
    } else if (this.column.type == 'barcode') {
      let element = this.getLeafInput();
      element.value = '';
    }
  }

  /**
   * 인풋 객체를 가져온다. 
   * @returns {HTMLElement}
   */
  getInput() {
    if (this.column.type == 'code-editor'
      || this.column.type == 'textarea') {
      return this.renderRoot.firstElementChild.firstElementChild;
    } else {
      return this.renderRoot.firstElementChild;
    }
  }


  /**
   * 엘리먼트 내 최 하단 인풋 객체를 가져온다. (readonly 처리용.. )
   * @returns {HTMLElement}
   */
  getLeafInput() {
    if (this.column.type == 'code-editor') {
      return this.renderRoot.querySelector('ox-input-code');
    } else if (this.column.type == 'textarea') {
      return this.renderRoot.querySelector('textarea');
    } else if (this.column.type == 'select-combo') {
      return this.renderRoot.querySelector('ox-select').firstElementChild;
    } else if (this.column.type == 'barcode') {
      return this.renderRoot.querySelector('ox-input-barcode').renderRoot.querySelector('input');
    } else {
      return this.renderRoot.firstElementChild.editor;
    }
  }

  /**
   * 에디터를 생성 한다. 
   * @returns {HTML}
   */
  getOperatoEditor() {
    if (this.column.type == 'code-editor') {
      return html`
        <div>
          <ox-input-code mode="javascript" tab-size="2"></ox-input-code>
        </div>`
    } else if (this.column.type == 'textarea') {
      return html`
        <div style="display:flex">
          <textarea></textarea>
        </div>
      `
    } else if (this.column.type == 'barcode') {
      return html`
          <ox-input-barcode></ox-input-barcode>
      `
    } else if (this.column.type == 'select-combo') {
      return html`
          <ox-select>
            <ox-popup-list align-left nowrap></ox-popup-list>
          </ox-select>
      `

    } else {
      return getEditor(this.column.type)('', this.column, this.column.record, this.rowIndex, {});
    }
  }

  /**
   * input 오브젝트에 readOnly 옵션 적용  
   */
  setReadOnly() {
    if (this.editable === true) return;

    let editable = String(this.editable);

    if (editable == 'false') {
      let inputObj = this.getLeafInput();
      let tagName = inputObj.tagName.toUpperCase();

      if (tagName === 'SELECT') {
        inputObj.disabled = true;
      } else if (this.column.type == 'code-editor') {
        inputObj.editor.options.readOnly = true;
      } else {
        inputObj.readOnly = true;
      }
    }
  }
}

customElements.define('operato-input-editor', OperatoInputEditor)