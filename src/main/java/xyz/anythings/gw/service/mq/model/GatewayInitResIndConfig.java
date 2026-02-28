package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class GatewayInitResIndConfig {
	
	// 인디케이터 역할 부여
	private String[] segRole;

	// 인디케이터 표시 자리
	private String alignment;

	// 버튼 점등 방식 깜빡임,항상 켜짐
	private String btnMode;
	
	// 버튼 깜박임 주기
	private Integer btnIntvl;
	
	// 숫자 점등 전 인디케이터에 표시될 문자열
	private String bfOnMsg;
	
	// 숫자 점등 전 문자열 표시할 시간
	private Integer bfOnMsgT;
	
	// 숫자 점등 전 딜레이
	private Integer bfOnDelay;
	
	// 캔슬 버튼 시 소등까지의 딜레이
	private Integer cnclDelay;
	
	// full 점등 상태에서 버튼 깜박임 여부
	private Boolean blinkIfFull;
	
	// 인디케이터가 소등 상태일 때 소등 보고 메시지 사용 여부
	private Boolean offUseRes;
	
	// LED바 점등 방식
	private String ledBarMode;
	
	// LED바 깜박임 간격
	private Integer ledBarIntvl;
	
	// LED바 밝기
	private Integer ledBarBrtns;
	
	// 인디케이터 표시 형식
	// 0 : default (or undefined)
	// 1 : 박스입수, 낱개수량을 계산하여 박스수량 / 낱개수량 형태로 표시
	// 2 : 누적수량 / 낱개수량 (반품)
	// 3 : 작업잔량 / 낱개수량
	private String viewType;

	public String[] getSegRole() {
		return segRole;
	}

	public void setSegRole(String[] segRole) {
		this.segRole = segRole;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public String getBtnMode() {
		return btnMode;
	}

	public void setBtnMode(String btnMode) {
		this.btnMode = btnMode;
	}

	public Integer getBtnIntvl() {
		return btnIntvl;
	}

	public void setBtnIntvl(Integer btnIntvl) {
		this.btnIntvl = btnIntvl;
	}

	public String getBfOnMsg() {
		return bfOnMsg;
	}

	public void setBfOnMsg(String bfOnMsg) {
		this.bfOnMsg = bfOnMsg;
	}

	public Integer getBfOnMsgT() {
		return bfOnMsgT;
	}

	public void setBfOnMsgT(Integer bfOnMsgT) {
		this.bfOnMsgT = bfOnMsgT;
	}

	public Integer getBfOnDelay() {
		return bfOnDelay;
	}

	public void setBfOnDelay(Integer bfOnDelay) {
		this.bfOnDelay = bfOnDelay;
	}

	public Integer getCnclDelay() {
		return cnclDelay;
	}

	public void setCnclDelay(Integer cnclDelay) {
		this.cnclDelay = cnclDelay;
	}

	public Boolean getBlinkIfFull() {
		return blinkIfFull;
	}

	public void setBlinkIfFull(Boolean blinkIfFull) {
		this.blinkIfFull = blinkIfFull;
	}

	public Boolean getOffUseRes() {
		return offUseRes;
	}

	public void setOffUseRes(Boolean offUseRes) {
		this.offUseRes = offUseRes;
	}

	public String getLedBarMode() {
		return ledBarMode;
	}

	public void setLedBarMode(String ledBarMode) {
		this.ledBarMode = ledBarMode;
	}

	public Integer getLedBarIntvl() {
		return ledBarIntvl;
	}

	public void setLedBarIntvl(Integer ledBarIntvl) {
		this.ledBarIntvl = ledBarIntvl;
	}

	public Integer getLedBarBrtns() {
		return ledBarBrtns;
	}

	public void setLedBarBrtns(Integer ledBarBrtns) {
		this.ledBarBrtns = ledBarBrtns;
	}

	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
	}
}
