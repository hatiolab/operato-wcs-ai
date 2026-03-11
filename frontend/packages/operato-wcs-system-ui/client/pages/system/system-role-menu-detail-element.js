import { MetaGristMixin } from '@things-factory/metapage/client/mixin/meta-grist-mixin'

import { LitElement } from 'lit-element'
import { i18next, localize } from '@operato/i18n'


export class SystemRoleMenuDetailElement extends MetaGristMixin(localize(i18next)(LitElement)) {


  async connectedCallback() {
    // 조회 데이터 변경 callBack 함수 지정 
    this.fetchResultSetCallback = this.responseDataSet;


    if (super.connectedCallback) {
      await super.connectedCallback();
    }

    // 팝업을 오픈한 페이지의 선택된 행 ID 를 가져오기 위해 상위 객체의 parent_id 를 불러온다. 
    let hostId = this.parentNode.parentNode.parentNode.host.parent_id;
    this.menu.resource_url = this.menu.resource_url.replace(':host_id', hostId);
    this.menu.save_url = this.menu.save_url.replace(':host_id', hostId);
  }

  /**
   * 데이터 변경 
   *********************************** 
   * @param {Object} response 
   * @returns 
   */
  responseDataSet(response) {
    let mergeResult = { total: 0, records: [] };

    let idMergeObject = {};
    response.items.forEach(record => {
      if (this.isEmpty(idMergeObject[record.id])) {
        idMergeObject[record.id] = {
          menu_id: record.id,
          parent_id: record.parent_id,
          name: record.name,
          show: false,
          update: false,
          create: false,
          delete: false
        }
      }

      if (record.action_name) {
        idMergeObject[record.id][record.action_name] = true;
      }

    });

    Object.keys(idMergeObject).forEach(key => {
      mergeResult.records.push(idMergeObject[key]);
    })

    return mergeResult;
  }


  /**
   * 저장 버튼 override 
   */
  async save() {
    const patches = this.getPatches()

    // 변경된 내용이 없음 
    if (!patches || patches.length == 0) {
      this.showCustomAlert('title.info', 'text.NOTHING_CHANGED');
      return;
    }

    let gristData = this.grist.dirtyData.records;
    let patchDatas = [];

    gristData.filter(x => x.show == true || x.create == true || x.update == true || x.delete == true).forEach(data => {
      let patchData = {
        menu_id: data.menu_id,
        parent_id: data.parent_id
      }

      if (data.show === true) patchData.show = true;
      if (data.create === true) patchData.create = true;
      if (data.update === true) patchData.update = true;
      if (data.delete === true) patchData.delete = true;

      patchDatas.push(patchData);
    })

    // 여러 건의 레코드에 대해서 insert / update 동시 요청
    var response = await this.restPost(this.saveUrl + (this.isEmpty(patchDatas) ? '&delete_all=true' : ''), patchDatas);
    if (response && response.status == 200 && this.grist) {
      this.grist.fetch();
    }
  }
}

customElements.define('system-role-menu-detail-element', SystemRoleMenuDetailElement)