package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import xyz.anythings.gw.service.model.IIndOnInfo;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class IndicatorOnInformation implements IIndOnInfo {

	// 인디케이터 아이디
	private String id;
	
	// 인디케이터 역할 분배
	private String[] segRole;
	
	// 작업 아이디
	private String bizId;
	
	// 표시 릴레이 번호
	private Integer orgRelay;

	// 표시 박스 수량
	private Integer orgBoxQty;
	
	// 표시 낱개 수량
	private Integer orgEaQty;
	
	// 박스 입수 수량
	private Integer orgBoxinQty;
	
	// 총 누적 수량
	private Integer orgAccmQty;
	
	// 표시 색상
	private String color;
	
	// 인디케이터 숫자 정렬
	private String alignment;

	// 점등 방식
	private String btnMode;
	
	// 반짝임 간격 
	private Integer btnIntvl;
	
	// 숫자 점등 전 인디케이터에 표시될 문자열
	private String bfOnMsg;
	
	// 숫자 점등 전 문자열 표시 시간
	private Integer bfOnMsgT;
	
	// 숫자 점등 전 딜레이
	private Integer bfOnDelay;
	
	// 캔슬 시 소등까지의 딜레이
	private Integer cnclDelay;
	
	// full 점등 시 버튼 반짝임 여부
	private Boolean blinkIfFull;
	
	// End 상태에서 Full Box 미 처리여부
	private Boolean endFullBox;
	
	// 표시할 문자값 - 7Segment로 표시할 수 있는 값만 출력 가능
	private String viewStr;
	
	// 인디케이터 표시 형식
	// 0 : default (or undefined)
	// 1 : 박스입수, 낱개수량을 계산하여 박스수량 / 낱개수량 형태로 표시
	// 2 : 누적수량 / 낱개수량 (반품)
	// 3 : 작업잔량 / 낱개수량
	private String viewType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getSegRole() {
		return segRole;
	}

	public void setSegRole(String[] segRole) {
		this.segRole = segRole;
	}

	public String getBizId() {
		return bizId;
	}

	public void setBizId(String bizId) {
		this.bizId = bizId;
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

	public Integer getOrgBoxinQty() {
		return orgBoxinQty;
	}

	public void setOrgBoxinQty(Integer orgBoxinQty) {
		this.orgBoxinQty = orgBoxinQty;
	}

	public Integer getOrgAccmQty() {
		return orgAccmQty;
	}

	public void setOrgAccmQty(Integer orgAccmQty) {
		this.orgAccmQty = orgAccmQty;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
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

	public Boolean getEndFullBox() {
		return endFullBox;
	}

	public void setEndFullBox(Boolean endFullBox) {
		this.endFullBox = endFullBox;
	}

	public String getViewStr() {
		return viewStr;
	}

	public void setViewStr(String viewStr) {
		this.viewStr = viewStr;
	}

	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
	}
}