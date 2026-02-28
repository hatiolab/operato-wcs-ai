package xyz.anythings.gw.service.mq.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeName(Action.Values.IndicatorOnRequest)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class IndicatorOnRequest implements IMessageBody {
	@JsonIgnore
	private String action=Action.Values.IndicatorOnRequest;
	
	public IndicatorOnRequest() {
	}
	
	public IndicatorOnRequest(String bizType, String actionType, List<IndicatorOnInformation> indOn) {
		this.bizType = bizType;
		this.actionType = actionType;
		this.indOn = indOn;
	}
		
	// 작업 타입 
	private String bizType;
	
	// 액션 타입 
	private String actionType;
	
	// 보낸대로 돌려받는 인수 목록
	private Map<String, Object> retArgs;
	
	// 읽기 전용 ( 버튼 동작 무시 )
	private Boolean readOnly = false;
	
	//인디케이터 표시 정보 
	private List<IndicatorOnInformation> indOn;

	public String getBizType() {
		return bizType;
	}
	
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	
	public String getActionType() {
		return actionType;
	}
	
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public List<IndicatorOnInformation> getIndOn() {
		return indOn;
	}
	
	public void setIndOn(List<IndicatorOnInformation> indOn) {
		this.indOn = indOn;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Map<String, Object> getRetArgs() {
		return retArgs;
	}

	public void setRetArgs(Map<String, Object> retArgs) {
		this.retArgs = retArgs;
	}
}
