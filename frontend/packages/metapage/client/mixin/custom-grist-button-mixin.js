import { CustomButtonMixin } from './custom-button-mixin'

/**
 * 그리스트 내 버튼 관련 mixin
 * @param {Object} superClass 
 * @returns 
 */
export const CustomGristButtonMixin = (superClass) => class extends CustomButtonMixin(superClass) {

  /**
   * 메뉴 버튼 설정에서 그리드 버튼만 가져와 설정 셋을 만든다.
   ************************************** 
   * @description get grist 가 없으면 그리드가 없는것으로 보고 빈 array 를 리턴한다. 
   * @returns {Array} 그리스트 버튼 설정 셋트 
   */
  getGristButtons() {
    if (!this.grist) {
      return [];
    }

    // 버튼 설정에서 grid 버튼만 필터 
    let configGutters = this.actions.filter(action => action.type && action.type.startsWith("grid"));
    let gristButtons = [];

    configGutters.forEach((currentElement) => {
      let gutter = {
        type: 'gutter',
        gutterName: 'button',
        icon: currentElement.style,
        handlers: {
          click: (_columns, _data, _column, record, _rowIndex) => {
            if (record.id) this.gristButtonEventHandler(currentElement, record);
          }
        }
      }

      // 그리드 상태 체크서비스 일때 
      // 아이콘 변경 및 서비스 url 별도로 서비스 가능 
      if (currentElement.type == 'grid-status-service') {
        let logics = JSON.parse(currentElement.logic);

        gutter.icon = (record) => {
          if (!record) return currentElement.style;

          for (var idx = 0; idx < logics.length; idx++) {
            let logic = logics[idx]
            let icon = logic.icon ? logic.icon : currentElement.style;
            let useIcon = true;
            Object.keys(logic).filter(x => x != 'url' && x != 'icon').forEach(key => {
              let compareValue = logic[key];

              if (compareValue == '') {
                if ((record[key] == undefined || record[key] == null || record[key] == '') == false) {
                  useIcon = false;
                }
              } else {
                if (compareValue != record[key]) {
                  useIcon = false;
                }
              }
            })

            if (useIcon) {
              return icon;
            }
          }
        }
      }

      gristButtons.push(gutter);
    });

    return gristButtons;
  }

  /**
   * 그리스트 gutter 버튼 이벤트 처리자 
   *****************************************
   * @param {Object} action 
   * @param {Object} record 
   */
  async gristButtonEventHandler(action, record) {

    if (action.type == 'grid-page-link') { // 페이지 이동 
      this.pageNavite(action.logic.replace(":id", record[this.menu.id_field]), record);
    } else if (action.type == 'grid-popup-link') { // 팝업 오픈 
      let logic = JSON.parse(action.logic);
      await this.openDynamicPopup(this.isEmpty(logic.title_field) ? record[this.menu.title_field] : record[logic.title_field], JSON.parse(action.logic), record, record[this.menu.id_field]);
    } else if (action.type == 'grid-service') { // 서비스 호출 
      await this.requestRestService(action.method, action.logic.replace(":id", record[this.menu.id_field]), record);
    } else if (action.type == 'grid-pass-param') {
      this.pass_param(JSON.parse(action.logic), record);
    } else if (action.type == 'grid-status-service') { // 상태 서비스 
      let logics = JSON.parse(action.logic);

      for (var idx = 0; idx < logics.length; idx++) {
        let logic = logics[idx]
        let isMatch = true;
        Object.keys(logic).filter(x => x != 'url' && x != 'icon').forEach(key => {
          let compareValue = logic[key];

          if (compareValue == '') {
            if ((record[key] == undefined || record[key] == null || record[key] == '') == false) {
              isMatch = false;
            }
          } else {
            if (compareValue != record[key]) {
              isMatch = false;
            }
          }
        })

        if (isMatch) {
          await this.requestRestService(action.method, logic.url.replace(":id", record[this.menu.id_field]), record);
          return;
        }
      }
    }
  }

  /**
   * 메뉴 파라미터 설정에 따라 record 데이터를 logic[idx].dest_element.dest_attr 로 전달 한다.
   *********************************************************************************
   * @param {Array} logic 메뉴 파리미터 설정 
   * @param {Object} data 그리드 선택 record 
   */
  pass_param(logic, data) {
    // 배열이 아니면 배열로 변환 
    let logicArr = Array.isArray(logic) ? logic : [{logic}];
    
    // loop 를 돌면서 파라미터 전달 
    logicArr.forEach(param => {
      let destElement = this.shadowRoot.querySelector(`#${param.dest_element}`);
      if (destElement) {
        if (destElement[`set_${param.dest_attr}`]) {
          destElement[`set_${param.dest_attr}`](data[param.source_attr]);
        }
      }
    });
  }
}