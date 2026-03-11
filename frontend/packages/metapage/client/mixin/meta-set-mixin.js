import { RestServiceMixin } from './rest-service-mixin'

/**
 * meta 데이터 셋팅 mixin 
 *********************************************** 
 * @param {Object} superClass 
 * @returns 
 */
export const MetaSetMixin = (superClass) => class extends RestServiceMixin(superClass) {

  /**
   * Lifecycle
   */
  async connectedCallback() {
    let menuMeta = await this.getMenuMeta();

    this.sort_fields = menuMeta.sort_fields;
    this.actions = menuMeta.actions;
    this.grid_config = menuMeta.grid_config;
    this.menu = menuMeta.menu;
    this.menu_params = menuMeta.menu_params.params;
    this.search_hidden_fields = menuMeta.search_hidden_fields;
    this.select_fields = menuMeta.select_fields;
    this.use_add_button = menuMeta.use_add_button;
    this.search_form_fields = menuMeta.search_form_fields;
    this.form_fields = menuMeta.form_fields;

    // Element 일 경우 URL 변경 
    if (this.is_element && this.isNotEmpty(this.parent_id)) {
      // url 치환 
      this.resource_url = this.menu.resource_url.replace(":id", this.parent_id);
      // 저장 URL 은 없을 수 있다.
      if (this.isNotEmpty(this.menu.save_url)) {
        this.save_url = this.menu.save_url.replace(":id", this.parent_id);
      }
    } else if (this.is_page) {
      this.resource_url = this.menu.resource_url;
      this.save_url = this.menu.save_url;
    }

    // grid-view-option 처리 
    let gridViewOption = this.menuParamValue('grid-view-options');

    if (!gridViewOption) { // 지정되지 않으면 기본 설정 ( PC : Grid,  Mobile : CARD).
      this.grid_mode = this.isMobile ? 'CARD' : 'GRID';
      this.grid_view_options = [this.isMobile ? 'CARD' : 'GRID'];
    } else {
      this.grid_view_options = gridViewOption.split(',');

      if (this.grid_view_options.length == 1) {
        this.grid_mode = this.grid_view_options[0];
      } else {
        // Mobile : 옵션[1] , PC : 옵션[0]
        this.grid_mode = this.isMobile ? this.grid_view_options[1] : this.grid_view_options[0];
      }
    }

    // cud flag 함수 생성 
    if (this.createChangeCudFlagFunc) {
      this.createChangeCudFlagFunc();
    }

    if (super.connectedCallback) {
      await super.connectedCallback();
    }
  }

  /**
   * 서버 호출 URL 
   ***********************************************
   * @returns {String}
   */
  get resourceUrl() {
    return this.resource_url;
  }

  /**
   * CUD Update multiple URL 
   ***********************************************
   * @returns {String}
   */
  get saveUrl() {
    return this.save_url;
  }
}


