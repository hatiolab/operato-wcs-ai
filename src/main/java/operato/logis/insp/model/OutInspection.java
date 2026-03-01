package operato.logis.insp.model;

import java.util.List;

import xyz.anythings.base.entity.BoxPack;
import xyz.elidom.core.entity.Code;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.core.rest.CodeController;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 출고 검수 모델
 * 
 * @author shortstop
 */
public class OutInspection extends BoxPack {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 피킹 작업 상태 - W 작업 대기 > I 투입 > P 피킹 시작 > F 피킹 완료 > B 주문 완료 > E 검수 완료 > O 출고 완료
	 */
	private String statusStr;
	/**
	 * 박스 타입 - box / tray
	 */
	private String boxType;
	
	public String getStatusStr() {
		Code code = BeanUtil.get(CodeController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, "JOB_STATUS");
		if(code != null) {
			List<CodeDetail> codeItems = code.getItems();
			for(CodeDetail item : codeItems) {
				if(ValueUtil.isEqualIgnoreCase(item.getName(), this.getStatus())) {
					this.statusStr = item.getDescription();
					break;
				}
			}
		}
		
		return this.statusStr;
	}
		
	public String getBoxType() {
		return boxType;
	}
	
	public void setBoxType(String boxType) {
		this.boxType = boxType;
	}

}
