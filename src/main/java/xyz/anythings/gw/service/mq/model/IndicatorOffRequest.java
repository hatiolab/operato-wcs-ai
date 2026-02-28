package xyz.anythings.gw.service.mq.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeName(Action.Values.IndicatorOffRequest)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class IndicatorOffRequest implements IMessageBody{
	@JsonIgnore
	private String action=Action.Values.IndicatorOffRequest;
	
	// END 상태 필터링
	private Boolean endOffFlag;
	
	// 전체 강제 소등 
	private Boolean forceFlag;
	private List<String> indOff;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
	public Boolean getEndOffFlag() {
		return endOffFlag;
	}
	public void setEndOffFlag(Boolean endOffFlag) {
		this.endOffFlag = endOffFlag;
	}
	public Boolean getForceFlag() {
		return forceFlag;
	}
	public void setForceFlag(Boolean forceFlag) {
		this.forceFlag = forceFlag;
	}
	public List<String> getIndOff() {
		return indOff;
	}
	public void setIndOff(List<String> indOff) {
		this.indOff = indOff;
	}
}