package xyz.anythings.gw.service.mq.model.equip;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import xyz.anythings.gw.service.mq.model.Action;
import xyz.anythings.gw.service.mq.model.IMessageBody;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.FormatUtil;

/**
 * 장비 상태 알림 메시지 이벤트
 * 
 * @author shortstop
 */
@JsonTypeName(Action.Values.EquipStatusReport)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EquipStatusReport implements IMessageBody {

	@JsonIgnore
	private String action=Action.Values.EquipStatusReport;
	
	/**
	 * 설비 유형
	 */
	protected String equipType;
	/**
	 * 설비 코드
	 */
	protected String equipCd;
	/**
	 * 작업 스테이션 
	 */
	protected String stationCd;
	/**
	 * 장비 패스
	 */
	protected String equipPath;
	/**
	 * Message
	 */
	protected String message;
	
	@Override
	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String getAction() {
		return this.action;
	}
		
	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getEquipPath() {
		if(ValueUtil.isEmpty(this.equipPath)) {
			this.equipPath = this.equipType + SysConstants.DASH + this.equipCd;
			
			if(ValueUtil.isNotEmpty(this.stationCd)) {
				this.equipPath = this.equipPath + SysConstants.DASH + this.stationCd;
			}
		}
		
		return this.equipPath;
	}
	
	public void setEquipPath(String equipPath) {
		this.equipPath = equipPath;
	}

	public String toString() {
		return FormatUtil.toUnderScoreJsonString(this);
	}

}
