import '@things-factory/form-ui'
import './system-sub-menu-popup'
import '@operato/data-grist/ox-grist.js'
import '@operato/data-grist/ox-filters-form.js'
import '@operato/data-grist/ox-sorters-control.js'
import '@operato/data-grist/ox-record-creator.js'

import { css, html } from 'lit-element'

import { CommonGristStyles, CommonButtonStyles } from '@operato/styles'
import { isMobileDevice } from '@operato/utils'
import { i18next, localize } from '@operato/i18n'

import { client, CustomAlert, PageView } from '@things-factory/shell'
import { openPopup } from '@things-factory/layout-base'

import { operatoGetMenuMeta, operatoGetData, operatoUpdateMultiple, currentRouteMenu } from '@things-factory/operatofill'

class SystemMenu extends localize(i18next)(PageView) {
  static get properties() {
    return {
      config: Object,
      data: Object,
      total: 0,
      records: Array,
      sort_fields: Object,
      actions: Object,
      grid_config: Object,
      menu: Object,
      menu_params: Object,
      search_hidden_fields: Object,
      select_fields: Object,
      sort_fields: Object,
      use_add_button: Boolean
    }
  }

  static get styles() {
    return [
      CommonGristStyles,
      css `
        :host {
          display: flex;
          flex-direction: column;
          overflow: hidden;
        }

        ox-grist {
          overflow-y: auto;
          flex: 1;
        }
      `
    ]
  }

  constructor() {
    super();
  }



  /**
   * 메뉴 메타 정보 가져오기 때문에 async 처리 
   * 부모의 connectedCallback 을 먼저 실행 하면 lifecycle 순서가 꼬임
   * 아래 순서로 진행 필수 
   */
  async connectedCallback() {
    let menuName = currentRouteMenu();
    let menuMeta = await operatoGetMenuMeta(menuName);

    this.sort_fields = menuMeta.sort_fields;
    this.actions = menuMeta.actions;
    this.grid_config = menuMeta.grid_config;
    this.menu = menuMeta.menu;
    this.menu_params = menuMeta.menu_params.params;
    this.search_hidden_fields = menuMeta.search_hidden_fields;
    this.select_fields = menuMeta.select_fields;
    this.use_add_button = menuMeta.use_add_button;

    super.connectedCallback();
  }

  /**
   * 화면 렌더링 
   * @returns HTML
   */
  render() {
    return html `
      <ox-grist id="ox-grist" .config=${this.config} .mode=${isMobileDevice() ? 'LIST' : 'GRID'} auto-fetch
        .fetchHandler=${this.fetchHandler.bind(this)}>
        <div slot="headroom">
          <div id="filters">
            <ox-filters-form></ox-filters-form>
          </div>
      
          <div id="sorters">
            <mwc-icon @click=${e => {
                const target = e.currentTarget
                this.renderRoot.querySelector('#sorter-control').open({
                  right: 0,
                  top: target.offsetTop + target.offsetHeight
                })
              }}
              >sort</mwc-icon>
            <ox-popup id="sorter-control">
              <ox-sorters-control> </ox-sorters-control>
            </ox-popup>
          </div>
      
          <ox-record-creator id="add" ?hidden="${!this.use_add_button}" .callback=${this.recordCreationCallback.bind(this)}>
            <button>
              <mwc-icon>add</mwc-icon>
            </button>
          </ox-record-creator>
        </div>
      </ox-grist>
    `
  }

  /**
   * 타이틀 / 버튼 설정 
   */
  get context() {
    // 버튼과 function 연결 
    this.actions.forEach((currentElement, index, array) => {
      currentElement.action = this[currentElement.action].bind(this);
      if (currentElement.style) {
        Object.assign(currentElement, CommonButtonStyles[currentElement.style]);
      }
    });

    return {
      title: this.menu.title,
      actions: this.actions
    };
  }

  /**
   * 서치 폼 가져오기 
   */
  get searchForm() {
    return this.shadowRoot.querySelector('ox-filters-form')
  }

  /**
   * 그리드 가져오기 
   */
  get dataGrist() {
    return this.shadowRoot.querySelector('ox-grist')
  }

  /**
   * 서버 호출 URL 
   */
  get getResourceUrl() {
    return this.menu.resource_url;
  }

  pageUpdated(_changes, _lifecycle) {}

  /**
   * 페이지 init 이 완료 되면 그리드/ 서치폼에 대한 설정을 한다.
   */
  firstUpdated() {
    let menu = this.menu;
    let menuParams = this.menu_params;
Í
    // 상세 폼 여부에 따라 상세 보기 아이콘 설정 
    let detailIcon = menu.use_detail_form ? {
      type: 'gutter',
      gutterName: 'button',
      icon: 'reorder',
      handlers: {
        click: (_columns, _data, _column, record, _rowIndex) => {
          if (record.id) this.openDetailPopup(record);
        }
      }
    } : undefined;

    // 페이지 사용 여부에 따라 그리드 내 페이지네이션 설정 
    let pagination = undefined;
    let gridMultipleSelect = true;

    // 그리드 멀티 선택 옵션 
    if (menuParams.grid_multiple_select) {
      gridMultipleSelect = (menuParams.grid_multiple_select === 'true');
    }

    // 페이지 사용 안함 
    if (menu.use_pagination == false) {
      pagination = { infinite: true };
    } else if (menuParams.pagination) { // 페이지 옵션 이 menu_param 에 설정 됨 
      let pagesString = menuParams.pagination.split(",");
      let pagesInt = [];

      for (var idx in pagesString) {
        pagesInt.push(parseInt(pagesString[idx]));
      }
      pagination = { pages: pagesInt };
    } else pagination = { pages: [20, 30, 50, 100] }; // default 

    this.config = {
      rows: { selectable: { multiple: gridMultipleSelect }, appendable: this.use_add_button },
      columns: [
        { type: 'gutter', gutterName: 'dirty' },
        { type: 'gutter', gutterName: 'sequence' },
        { type: 'gutter', gutterName: 'row-selector', multiple: gridMultipleSelect },
      ]
    };

    if (detailIcon) {
      this.config.columns.push(detailIcon);
    }
    this.config.columns.push(...this.grid_config);
    this.config['pagination'] = pagination;
  }

  /**
   * 리스트 조회 
   * @param {*} param0
   * @returns 
   */
  async fetchHandler({ page, limit, sorters = [], filters = [] }) {

    // 페이지네이션 사용 여부 
    if (this.menu.use_pagination == false) {
      page = 0;
      limit = 0;
    }

    let query = [...this.search_hidden_fields, ...filters];
    sorters.push(...this.sort_fields);

    let sort = [];

    sorters.forEach((currentElement, index, array) => {
      sort.push({
        field: currentElement.name,
        ascending: currentElement.desc ? false : true
      })
    });

    let params = [{
        'name': 'select',
        'value': encodeURI(this.select_fields.join(","))
      },
      {
        'name': 'sort',
        'value': encodeURI(JSON.stringify(sort))
      },
      {
        'name': 'query',
        'value': encodeURI(JSON.stringify(query))
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

    var response = await operatoGetData(this.getResourceUrl, params);

    let total = 0;
    let records = [];

    if (this.menu.total_res_field) {
      total = response[this.menu.total_res_field] || 0;
    }

    if (this.menu.items_res_field) {
      records = response[this.menu.items_res_field] || [];
    } else {
      records = response;
    }

    return {
      total: total,
      records: records
    }
  }

  /**
   * 저장 버튼 처리 
   */
  async save() {
    const patches = this.getPatches()

    // 변경된 내용이 없음 
    if (!patches || patches.length == 0) {
      CustomAlert({
        title: i18next.t('text.nothing_changed'),
        text: i18next.t('text.there_is_nothing_to_save')
      })
      return;
    }

    // insert / update 요청 
    var response = await this.requestCudMultiple(patches);
    if (!response.errors) this.dataGrist.fetch();
  }


  /**
   * 삭제 버튼 처리 
   * @returns 
   */
  async delete() {
    const ids = this.dataGrist.selected.map(record => record.id)

    if (!ids || ids.length == 0) {
      CustomAlert({
        title: i18next.t('text.nothing_selected'),
        text: i18next.t('text.there_is_nothing_to_delete')
      })
      return;
    }

    const anwer = await CustomAlert({
      type: 'warning',
      title: i18next.t('button.delete'),
      text: i18next.t('text.are_you_sure'),
      confirmButton: { text: i18next.t('button.delete') },
      cancelButton: { text: i18next.t('button.cancel') }
    })

    if (!anwer.value) return


    let delList = [];

    ids.forEach((currentElement, index, array) => {
      delList.push({
        id: currentElement,
        cud_flag_: 'd'
      })
    });

    // delete 요청 
    var response = await this.requestCudMultiple(patches);
    if (!response.errors) this.dataGrist.fetch();
  }


  /**
   * 팝업으로 한건 생성 
   * @param {*} patches 
   * @returns 
   */
  async recordCreationCallback(patches) {
    try {
      patches = [{...patches, cud_flag_: 'c' }];

      var response = await this.requestCudMultiple(patches);

      if (!response.errors) {
        this.dataGrist.fetch()
        document.dispatchEvent(
          new CustomEvent('notify', {
            detail: {
              message: i18next.t('text.data_created_successfully')
            }
          })
        );

        return true;
      }
    } catch (e) {
      console.error(e)
      document.dispatchEvent(
        new CustomEvent('notify', {
          detail: {
            type: 'error',
            message: i18next.t('text.error')
          }
        })
      )
    }
  }

  /**
   * Insert / Update / Delete 요청
   * @param {*} patches 
   */
  async requestCudMultiple(patches) {
    // insert / update / delete 요청 
    var response = await operatoUpdateMultiple(this.menu.save_url, patches);
    return response;
  }

  /**
   * 캐쉬 삭제 버튼 처리 
   */
  async clearCache() {

  }

  /**
   * 그리드에서 신규/ 변경 된 레코드를 가져온다 
   * @returns 
   */
  getPatches() {
    let cudRecords = this.dataGrist.dirtyRecords;

    if (cudRecords && cudRecords.length) {
      let patches = [];

      cudRecords.forEach((currentElement, index, array) => {
        currentElement.cud_flag_ = (currentElement.__dirty__ == 'M' ? 'u' : 'c');
        patches.push(currentElement)
      });

      return patches
    }

    return undefined;
  }

  /**
   * 서브 메뉴 팝업을 연다 
   * @param {*} menuId 
   * @param {*} menuName 
   */
  openDetailPopup(record) {
    let htmlText = `<${this.menu.detail_form_resource} menu_name='${this.menu.detail_form_name}' parent_id='${record.id}'></${this.menu.detail_form_resource}>`;
    let htmlElemnts = this.htmlToElements(htmlText);

    openPopup(htmlElemnts, {
      backdrop: true,
      size: 'large',
      title: record[this.menu.title_field]
    })
  }

  /**
   * HTML 문자열을 elements 로 반환 
   * @param {String} htmlString 
   * @returns 
   */
  htmlToElements(htmlString) {
    var template = document.createElement('template');
    template.innerHTML = htmlString;
    var elements = template.content.childNodes;

    return elements;
  }
}

customElements.define('system-menu', SystemMenu)