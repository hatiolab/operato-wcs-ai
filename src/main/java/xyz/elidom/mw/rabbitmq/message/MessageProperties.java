package xyz.elidom.mw.rabbitmq.message;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import xyz.elidom.sys.entity.Domain;
import xyz.elidom.util.ValueUtil;

/**
 * 기본 메시지 프로퍼티 구현
 * 
 * @author yang
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessageProperties {
	/**
	 * 메시지 아이디 
	 */
	private String id;
	/**
	 * 발송 시간
	 */
	private long time;
	/**
	 * 사이트 코드
	 */
	private String site;
	/**
	 * 메시지 소스 ID (소스 큐 이름) 
	 */
	private String sourceId;
	/**
	 * 목적지 라우팅 키
	 */
	private String destId;
	/**
	 * Reply 메시지 여부
	 */
	private Boolean isReply = false;
	/**
	 * 설비 타입
	 */
	private String equipType;
	/**
	 * 설비 벤더
	 */
	private String equipVendor;
	/**
	 * 설비 코드
	 */
	private String equipCd;
	/**
	 * 비지니스 유형 (예: 입고, 출고, ...)
	 */
	private String bizType;
	/**
	 * 액션
	 */
	private String action;
	/**
	 * ACK를 사용하지 않을지 여부
	 */
	private boolean noAck = true;
	/**
	 * ACK 메시지 여부
	 */
	private boolean isAckMsg = false;
	
	/**
	 * 생성자
	 */
	public MessageProperties() {
	}
	
	/**
	 * 생성자
	 * 
	 * @param site
	 * @param sourceId
	 * @param destId
	 * @param isReply
	 * @param equipType
	 * @param equipVendor
	 * @param equipCd
	 * @param action
	 */
	public MessageProperties(String site, String sourceId, String destId, Boolean isReply, String equipType, String equipVendor, String equipCd, String action) {
		this(UUID.randomUUID().toString(), site, sourceId, destId, isReply, equipType, equipVendor, equipCd, null, action);
	}
	
	/**
	 * 생성자
	 * 
	 * @param id
	 * @param siteId
	 * @param sourceId
	 * @param destId
	 * @param isReply
	 * @param equipType
	 * @param equipVendor
	 * @param equipCd
	 * @param bizType
	 * @param action
	 */
	public MessageProperties(String id, String site, String sourceId, String destId, Boolean isReply, String equipType, String equipVendor, String equipCd, String bizType, String action) {
		this.id = ValueUtil.isEmpty(id) ? UUID.randomUUID().toString() : id;
		this.site = ValueUtil.isEmpty(site) ? Domain.currentDomain().getMwSiteCd() : site;
		this.time = new Date().getTime();
		this.sourceId = sourceId;
		this.destId = destId;
		this.isReply = (isReply == null) ? false : isReply;
		this.equipType = equipType;
		this.equipVendor = equipVendor;
		this.equipCd = equipCd;
		this.bizType = bizType;
		this.action = action;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public String getSite() {
		return site;
	}
	
	public void setSite(String site) {
		this.site = site;
	}
	
	public String getSourceId() {
		return sourceId;
	}
	
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
	public String getDestId() {
		return destId;
	}
	
	public void setDestId(String destId) {
		this.destId = destId;
	}
	
	public Boolean getIsReply() {
		return isReply;
	}
	
	public void setIsReply(Boolean isReply) {
		this.isReply = isReply;
	}
	
	public String getEquipType() {
		return equipType;
	}
	
	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}
	
	public String getEquipVendor() {
		return equipVendor;
	}
	
	public void setEquipVendor(String equipVendor) {
		this.equipVendor = equipVendor;
	}
	
	public String getEquipCd() {
		return equipCd;
	}
	
	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}
	
	public String getBizType() {
		return bizType;
	}
	
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}

	public boolean getNoAck() {
		return noAck;
	}

	public void setNoAck(boolean noAck) {
		this.noAck = noAck;
	}

	public boolean getIsAckMsg() {
		return isAckMsg;
	}

	public void setIsAckMsg(boolean isAckMsg) {
		this.isAckMsg = isAckMsg;
	}
}