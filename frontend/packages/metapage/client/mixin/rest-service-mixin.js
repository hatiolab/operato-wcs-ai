import { MetaUtilMixin } from './meta-util-mixin'

import {
  operatoGetMenuMeta,
  currentRouteMenu,
  operatoGet,
  operatoGetData,
  operatoPut,
  operatoPutData,
  operatoPost,
  operatoPostData,
  operatoDelete,
  operatoDeleteData
} from '@things-factory/operatofill'

/**
 * Rest 서비스 호출 mixin
 * @param {Object} superClass 
 * @returns 
 */
export const RestServiceMixin = (superClass) => class extends MetaUtilMixin(superClass) {

  /**
   * 현재 document 의 routing 이름을 가져온다 
   ************************************* 
   * @returns {String}
   */
  get routingName() {
    if (this.is_page == undefined) {
      this.is_page = (this.route_name == undefined ? true : false);
    }

    return this.is_element ? this.route_name : currentRouteMenu();
  }

  /**
   * 메뉴 메타 정보를 서버에 요청 
   ************************ 
   * @returns {Object}
   */
  async getMenuMeta() {
    return await operatoGetMenuMeta(this.routingName);
  }

  /**
   * REST Get 서비스 
   ************************ 
   * @param {String} url 
   * @param {Array} params 
   */
  async restGet(url, params) {
    return await operatoGetData(url, params);
  }

  /**
   * Rest Post Cud restPostCudMultiple
   ***********************************************
   * @param {String} url 
   * @param {Array} params 
   * @returns 
   */
  async restPost(url, params) {
    return await operatoPost(url, params);
  }

  /**
   * Rest Put 서비스 
   ************************
   * @param {String} url 
   * @param {Object} params 
   */
  async restPut(url, params) {
    return await operatoPut(url, params);
  }

  /**
   * Rest Delete 서비스 
   ************************
   * @param {String} url 
   * @param {Array} params 
   * @returns 
   */
  async restDelete(url, params) {
    return await operatoDelete(url, params);
  }

  /**
   * method, url 로 레스트 서비스를 호출 한다 .
   ***********************************************
   * @param {String} method 
   * @param {String} url 
   * @param {Object} data 
   */
  async requestRestService(method, url, data) {
    var response = { status: '400' };

    if (method == 'GET') {
      response = await this.restGet(url, this.isEmpty(data) ? undefined : { name: 'param', value: encodeURI(JSON.stringify(data)) });
    } else {
      // 커스텀 서비스 연결시 데이터 변환 
      let params = this.isEmpty(data) ?
        undefined :
        (url.includes('diy_services/') ? { input: Array.isArray(data) ? { rows: data } : { data: data } } : data);

      // REST 요청 
      if (method == 'PUT') {
        response = await this.restPut(url, params);
      } else if (method == 'POST') {
        response = await this.restPost(url, params);
      } else if (method == 'DELETE') {
        response = await this.restDelete(url, params);
      }
    }

    // 메시지 처리 
    this.restResponseMessage(response);

    // 처리 결과 ....?
    if ([200, 201].includes(response.status)) {
      if (this.grist) {
        this.grist.fetch();
      } else if (this.useSearchForm()) {
        this.searchForm.submit();
      }
    }

    return response;
  }
}