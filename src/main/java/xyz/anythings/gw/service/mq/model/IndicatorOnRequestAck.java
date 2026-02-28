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

@JsonTypeName(Action.Values.IndicatorOnRequestAck)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class IndicatorOnRequestAck implements IMessageBody{
	@JsonIgnore
	private String action=Action.Values.IndicatorOnRequestAck;
	
	// 작업 타입 
	private String bizType;
	
	// 액션 타입 
	private String actionType;

	// 보낸대로 돌려받는 인수 목록
	private Map<String, Object> retArgs;
	
	//인디케이터 표시 정보 
	private List<IndicatorOnAckInformation> indOn;
	

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
	public List<IndicatorOnAckInformation> getIndOn() {
		return indOn;
	}
	public void setIndOn(List<IndicatorOnAckInformation> indOn) {
		this.indOn = indOn;
	}
	public Map<String, Object> getRetArgs() {
		return retArgs;
	}
	public void setRetArgs(Map<String, Object> retArgs) {
		this.retArgs = retArgs;
	}
}
