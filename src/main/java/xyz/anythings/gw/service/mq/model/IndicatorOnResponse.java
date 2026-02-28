package xyz.anythings.gw.service.mq.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeName(Action.Values.IndicatorOnResponse)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class IndicatorOnResponse implements IMessageBody {
	@JsonIgnore
	private String action = Action.Values.IndicatorOnResponse;

	// 작업 아이디
	private String bizId;

	// 작업 타입
	private String bizType;

	// 액션 타입
	private String actionType;

	// 주문 처리 flag
	// full : full Box
	// cancel : 작업취소
	// ok : 정상 처리
	private String bizFlag;
	// 표시 릴레이 번호
	private Integer orgRelay;
	// 표시 박스 수량
	private Integer orgBoxQty;
	// 표시 낱개 수량
	private Integer orgEaQty;
	// 처리 박스 수량
	private Integer resBoxQty;
	// 처리 낱개 수량
	private Integer resEaQty;
	
	// 보낸대로 돌려받는 인수 목록
	private Map<String, Object> retArgs;

	// 인디케이터 아이디
	private String id;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getBizId() {
		return bizId;
	}

	public void setBizId(String bizId) {
		this.bizId = bizId;
	}

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

	public String getBizFlag() {
		return bizFlag;
	}

	public void setBizFlag(String bizFlag) {
		this.bizFlag = bizFlag;
	}

	public Integer getOrgRelay() {
		return orgRelay;
	}

	public void setOrgRelay(Integer orgRelay) {
		this.orgRelay = orgRelay;
	}

	public Integer getOrgBoxQty() {
		return orgBoxQty;
	}

	public void setOrgBoxQty(Integer orgBoxQty) {
		this.orgBoxQty = orgBoxQty;
	}

	public Integer getOrgEaQty() {
		return orgEaQty;
	}

	public void setOrgEaQty(Integer orgEaQty) {
		this.orgEaQty = orgEaQty;
	}

	public Integer getResBoxQty() {
		return resBoxQty;
	}

	public void setResBoxQty(Integer resBoxQty) {
		this.resBoxQty = resBoxQty;
	}

	public Integer getResEaQty() {
		return resEaQty;
	}

	public void setResEaQty(Integer resEaQty) {
		this.resEaQty = resEaQty;
	}

	public Map<String, Object> getRetArgs() {
		return retArgs;
	}

	public void setRetArgs(Map<String, Object> retArgs) {
		this.retArgs = retArgs;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
