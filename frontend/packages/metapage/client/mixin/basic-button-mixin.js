import { MetaSetMixin } from './meta-set-mixin'

import { CommonButtonStyles } from '@operato/styles'
import { css } from 'lit-element'

/**
 * Grid 기본 버튼 처리
 * : save, delete, fetchHandler, recordCreationCallback
 * @param {*} superClass 
 * @returns 
 */
export const BasicButtonMixin = (superClass) => class extends MetaSetMixin(superClass) {

  /** 
   * 버튼 컨테이너 스타일 
   */
  static getButtonContainerStyle() {
    return [css`
      .button-container {
        padding: var(--button-container-padding);
        margin: var(--button-container-margin);
        text-align: var(--button-container-align);
        background-color: var(--button-container-background);
        height: var(--button-container-height);
        text-align: right;
        padding-right: 12px;
      }
    `]
  }

  /**
   * Page Context 생성에서 사용될 버튼 액션 정의
   ************************************************
   * @description 기본버튼은 그리스트를 기반으로 한다. : get grist() 함수가 페이지내에 없으면
   * @description 버튼을 그리지 않는다 ( ex) CHART Page )
   * @returns {Object} {actions: Array , exportable : Object}
   */
  getBasicActions() {
    let actions = [];

    let use_export_button = false;

    // 멀티 레이아웃이면 기본 버튼을 사용하지 않음
    if (this.isMultiLayoutPage()) {
      return {};
    }

    // 버튼과 function 연결 
    let configButtons = this.filterBasicButton();

    configButtons.forEach((currentElement) => {
      if (currentElement.name == 'export') {
        use_export_button = true;
      } else {
        currentElement.action = this[currentElement.name].bind(this);

        if (currentElement.style) {
          Object.assign(currentElement, CommonButtonStyles[currentElement.style]);
        }
        actions.push(currentElement);
      }
    });

    let reslut = {
      actions: actions,
    };

    // 엑셀 내보내기 버튼 
    if (use_export_button && this._exportableData) {
      reslut['exportable'] = {
        name: this.menu.title,
        data: () => {
          return this._exportableData(this.grist);
        }
      }
    }

    return reslut;
  }

  /**
   * 기본 버튼 element를 생성 한다. 
   *********************************** 
   * 문서가 페이지로 로딩된 경우에는 빈 배열 리턴 아니면 버튼을 생성해 리턴한다. 
   * @returns {Array}
   */
  getBasicButtons() {
    if (this.is_page && this.isMultiLayoutPage() == false) return [];

    let configButtons = this.filterBasicButton();
    let buttons = [];

    configButtons.forEach((currentElement) => {
      let btnEle = this.createButtonElement(currentElement.title);
      btnEle["rank"] = currentElement.rank;

      // 버튼 클릭 이벤트 설정 ( 내보내기와 다른 기본 버튼은 함수가 다름.)
      btnEle.onclick = currentElement.name != 'export' ?
        this[currentElement.name].bind(this) :
        btnEle.onclick = () => {
          this._exportableData(this.grist);
        };

      buttons.push(btnEle);
    });

    return buttons;
  }

  /**
   * 메타 actions 에서 기본 버튼 조건으로 filtering 
   ************************ 
   * @returns {Array} 
   */
  filterBasicButton() {
    return this.actions.filter(action => (action.name == 'add' || action.name == 'save' || action.name == 'delete' || action.name == 'export') && this.grist);
  }


  /**
   * 파라미터 셋팅 
   ********************************
   * @param {Object} fetchParam (grist parameter or search-form)
   * @returns { page: Number, limit: Number, sorters: Array, filters: Array }
   */
  async fetchParamSetter(fetchParam) {

    let resultParams = {};

    // 파라미터가 없는 경우 
    if (!fetchParam) {
      resultParams = { page: 0, limit: 0, sorters: [], filters: [] };
    } else if (fetchParam.tagName && fetchParam.tagName.toLowerCase() == 'search-form') { // 파라미터가 서치폼 
      resultParams.filters = await fetchParam.getQueryFilters();
    } else {
      // 두가지가 모두 있는 경우  filter 조건은 searchForm 을 기준으로 한다.
      if (this.grist && this.searchForm) {
        fetchParam.filters = await this.searchForm.getQueryFilters();
      }

      resultParams = fetchParam;
    }

    var { page = 0, limit = 0, sorters = [], filters = [] } = resultParams;

    let operChangeFilters = [];
    // 조회 조건 숨겨진 필드와 그리드 / 서치폼에 셋팅된 파라미터 병합 
    filters.forEach(filter => {
      let operChangeFilter = Object.assign({}, filter);
      operChangeFilter.operator = this.search_form_fields.filter(x => x.name == filter.name)[0].props.searchOper;
      operChangeFilters.push(operChangeFilter);
    })

    resultParams.filters = [...this.search_hidden_fields, ...operChangeFilters];


    // 정렬 조건 변켱 
    resultParams.sorters = [];
    sorters.forEach((currentElement, index, array) => {
      resultParams.sorters.push({
        field: currentElement.name,
        ascending: currentElement.desc ? false : true
      });
    });

    // 페이지네이션 사용 여부 
    resultParams.page = this.menu.use_pagination ? page : 0;
    resultParams.limit = this.menu.use_pagination ? limit : 0;

    return resultParams;
  }

  /**
   * rest index 서비스 리소스 조회 
   *******************************
   * @param {Object} fetchParam (grist parameter or search-form)
   * @returns {Object}
   */
  async fetchByResource(fetchParam) {

    var { page = 0, limit = 0, sorters = [], filters = [] } = fetchParam;

    let params = [{
      'name': 'select',
      'value': encodeURI(this.select_fields.join(","))
    },
    {
      'name': 'sort',
      'value': encodeURI(JSON.stringify(sorters))
    },
    {
      'name': 'query',
      'value': encodeURI(JSON.stringify(filters))
    },
    {
      'name': 'page',
      'value': page
    },
    {
      'name': 'limit',
      'value': limit
    }
    ];

    var response = await this.restGet(this.resourceUrl, params);
    return response
  }

  /**
   * 커스텀 서비스 리소스 조회 
   *******************************
   * @param {Object} fetchParam (grist parameter or search-form)
   * @returns {Object}
   */
  async fetchByCustom(fetchParam) {
    if (!fetchParam) {
      return null;
    }

    var { filters } = fetchParam;
    let params = this.isEmpty(filters) ? null :
      filters.map(filter => {
        return { name: filter.name, value: filter.value ? filter.value : null }
      }).filter(item => item.value != null);


    /*if (!params || params.length == 0) {
      return null;
    }*/

    return await this.restGet(this.resourceUrl, params);
  }

  /**
   * 리스트 조회
   *******************************
   * @description callback 이 지정되면 콜백 함수 호출 없으면 결과값 return 
   * @param {Object} fetchParam (grist parameter or search-form)
   * @returns {Object} 
   * @callback this.fetch_callback
   */
  async fetchHandler(fetchParam) {
    // 1. url 에 ':' 치환 필드가 포함되어 있으면 스킵
    if (this.isEmpty(this.resourceUrl) || this.resourceUrl.indexOf(':') > 0) {
      return;
    }

    // 2. fetch parameter 종류에 따라 fetchParam 추출 
    fetchParam = await this.fetchParamSetter(fetchParam);

    // 3. 서비스 호출
    let response =
      ((this.menu.resource_url != "diy_services/:id" && this.resourceUrl.startsWith('diy_services/')) ||
        this.menuParamValue('search-field-param-type', 'resource') == 'custom') ?
        await this.fetchByCustom(fetchParam) :
        await this.fetchByResource(fetchParam);


    let fetchResultSet = { total: 0, records: [] };

    // 4. 조회 데이터를 핸들링하여 fetchResultSet을 구성할 파서가 있다면 처리
    if (this.fetchResultSetCallback) {
      fetchResultSet = this.fetchResultSetCallback(response);

      // 5. 그렇지 않다면 fetchResultSet을 기본으로 구성
    } else {
      let records = response ? (this.menu.items_res_field ? response[this.menu.items_res_field] : response) : [];
      let total = (response && this.menu.total_res_field) ? response[this.menu.total_res_field] : records.length;
      fetchResultSet = {
        total: total,
        records: records
      }
    }

    // 6. fetchResultSet을 핸들링 할 콜백이 있다면 처리
    if (this.fetch_callback) {
      this.fetch_callback(fetchResultSet);
      return fetchResultSet;

      // 7. fetchResultSet을 리턴
    } else {
      return fetchResultSet;
    }
  }


  /**
   * 저장 버튼 처리 
   */
  async save() {
    const patches = this.getPatches()

    // 변경된 내용이 없음 
    if (!patches || patches.length == 0) {
      this.showCustomAlert('title.info', 'text.NOTHING_CHANGED');
      return;
    }

    // insert / update 요청 
    await this.requestCudMultiple(patches);
  }


  /**
   * 삭제 버튼 처리 
   */
  async delete() {
    const records = this.grist.selected;

    if (!records || records.length == 0) {
      this.showCustomAlert('title.info', 'text.NOTHING_SELECTED');
      return;
    }

    const answer = await this.showCustomAlert('title.confirm', 'text.Sure to Delete', 'warning', 'button.delete', 'button.cancel');
    if (!answer.value) return

    records.forEach(record => {
      record.cud_flag_ = 'd';
    })

    // delete 요청 
    await this.requestCudMultiple(records);
  }


  /**
   * 팝업으로 한건 생성
   ***********************************************
   * @param {Object} patches 
   * @returns {Boolean}
   */
  async recordCreationCallback(patches) {
    patches = [{ ...patches, cud_flag_: 'c' }];

    await this.requestCudMultiple(patches);
    return true;
  }

  /**
   * Insert / Update / Delete 요청
   ***********************************************
   * @param {Object} patches 
   * @return {Object}
   */
  async requestCudMultiple(patches) {

    // element 로드시 부모 객체의 ID 데이터에 반영 
    patches = this.setParentIdFieldByElement(patches);
    // cud 플래그 옵션 처리 
    patches = this.changeCudFlagByOptions(patches);

    // 여러 건의 레코드에 대해서 insert / update 동시 요청
    var response = await this.restPost(this.saveUrl, patches);
    if (response && response.status == 200 && this.grist) {
      this.grist.fetch();
    }

    return response;
  }

  /**
   * CUD 대상 데이터의 cud_flag_ 메뉴 파라미터의 option 에 따라 변경 
   ************************************************************************** 
   * @param {Array} patches 
   */
  changeCudFlagByOptions(patches) {

    if (this.isEmpty(this.cud_flag_converter)) {
      return patches;
    }

    // option 에 설정된 KEY 값 (변경전 flag) 로 loop 
    patches.forEach(record => {
      record.cud_flag_ = this.cud_flag_converter[record.cud_flag_](record);
    })

    return patches;
  }

  /**
   * cud-flag-option 
   ***************************************************** 
   * @description 메뉴 파라미터의 cud-flag-option 
   * @description ex ) {"u":[{"has_permission":true,"flag":"c"},{"has_permission":false,"flag":"d"}]}  update 대상 데이터중 has_permission 값이 true 이면 c / false 이면 d
   * @description ex ) {"u":{"has_permission":true,"flag":"c"}}  update 대상 데이터중 has_permission 값이 true 일때만 c 로 변경 나머지 값은 u 유지 함 .
   * @description ex ) {"u": "c"} update 대상 데이터를 c 로 변경 
   * @returns 
   */
  createChangeCudFlagFunc() {
    let options = this.menuParamValueToObject('cud-flag-option', undefined);

    // option 이 설정 되지 않았으면 return;
    if (!options) {
      this.cud_flag_converter = undefined;
      return;
    }

    // flag 기본 컨버터 
    let flagConverter = {
      c: (record) => { return 'c' },
      u: (record) => { return 'u' },
      d: (record) => { return 'd' }
    };

    // flag 별 컨버터 생성 
    Object.keys(flagConverter).forEach(key => {
      if (this.isEmpty(options[key])) return;

      let option = options[key];

      // option 이 string 이면 변경 값 사용 
      if (typeof option === 'string') {
        flagConverter[key] = (record) => {
          return option;
        };
      } else if (Array.isArray(option)) {
        // option 이 Array 이면 ....

        flagConverter[key] = (record) => {
          for (var idx = 0; idx < option.length; idx++) {
            let optRow = option[idx];
            let isConv = true;
            Object.keys(optRow).forEach(optKey => {
              if (optKey == 'flag') return;
              if (this.isEmpty(record[optKey]) || record[optKey] != optRow[optKey]) {
                isConv = false;
              }
            });

            if (isConv) {
              return optRow.flag;
            }
          }

          return record.cud_flag_;
        };

      } else if (typeof option === 'object') {
        // option 이 Object 이면 ....

        flagConverter[key] = (record) => {
          let isConv = true;

          Object.keys(option).forEach(optKey => {
            if (optKey == 'flag') return;
            if (this.isEmpty(record[optKey]) || record[optKey] != option[optKey]) {
              isConv = false;
            }
          });

          return isConv ? option.flag : record.cud_flag_;
        };
      }
    });

    this.cud_flag_converter = flagConverter;
  }
}