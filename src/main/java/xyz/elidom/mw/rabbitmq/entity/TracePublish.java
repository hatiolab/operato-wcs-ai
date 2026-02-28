package xyz.elidom.mw.rabbitmq.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.mw.rabbitmq.model.trace.ITraceModel;
import xyz.elidom.util.FormatUtil;

/**
 * publish 메시지 로그 저장을 위한 엔티티
 * 
 * @author yang
 */
@Table(name = "mq_trace_publish_log")
public class TracePublish implements ITraceModel {

	@Ignore
	private String type = "trace_pub";
	
	/**
	 * 메시지 아이디 
	 */
	@PrimaryKey
	@Column(name = "id", nullable = false, length = 40)
	private String id;

	/**
	 * 발신자 아이디 
	 */
	@PrimaryKey
	@Column(name = "source_id", nullable = false, length = 255)
	private String sourceId;

	/**
	 * 수신 대상 라우팅 키 
	 */
	@PrimaryKey
	@Column(name = "dest_id", nullable = false, length = 255)
	private String destId;

	/**
	 * Reply 여부 
	 */
	@Column(name = "is_reply")
	private Boolean isReply;
	
	/**
	 * NoAck 여부 
	 */
	@Column(name = "no_ack")
	private Boolean noAck;

	/**
	 * ACK 메시지 여부 
	 */
	@Column(name = "is_ack_msg")
	private Boolean isAckMsg;

	/**
	 * 메시지 전송  큐 숫자  
	 */
	@Column(name = "routed_count")
	private int routedCount;
	
	/**
	 * 메시지 전송 큐 리스트 
	 */
	@Column(name = "routed_queues", type = ColumnType.TEXT)
	private String routedQueues;
	
	/**
	 * 메시지 내용 
	 */
	@Column(name = "body", type = ColumnType.TEXT)
	private String body;
	
	/**
	 * 발신 시간 
	 */
	@Column (name = "pub_time", type = ColumnType.DATETIME)
	private Date pubTime;

	/**
	 * 기록 시간 
	 */
	@Column (name = "log_time", type = ColumnType.DATETIME)
	private Date logTime;
	
	/**
	 * 사이트 코드 
	 */
	@Column(name = "site", length=100)
	private String site;
	
	/**
	 * 설비 코드
	 */
	@Column(name = "equip_cd", length=255)
	private String equipCd;
	/**
	 * 설비 유형
	 */
	@Column(name = "equip_type", length=255)
	private String equipType;
	/**
	 * 설비 벤더
	 */
	@Column(name = "equip_vendor", length=255)
	private String equipVendor;
	/**
	 * 비지니스 유형
	 */
	@Column(name = "biz_type", length=255)
	private String bizType;

	/**
	 * action
	 */
	@Column(name = "action", length=255)
	private String action;
	
	/**
	 * elastic 시간 정보 저정 하기 위한 타입 변경 
	 */
	@Ignore
	private long pubTimeLong;
	
	/**
	 * elastic 시간 정보 저정 하기 위한 타입 변경 
	 */
	@Ignore
	private long logTimeLong;

	@Override
	public String toJsonString() {
		return FormatUtil.toJsonString(this, false);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Boolean getNoAck() {
		return noAck;
	}

	public void setNoAck(Boolean noAck) {
		this.noAck = noAck;
	}

	public Boolean getIsAckMsg() {
		return isAckMsg;
	}

	public void setIsAckMsg(Boolean isAckMsg) {
		this.isAckMsg = isAckMsg;
	}

	public int getRoutedCount() {
		return routedCount;
	}

	public void setRoutedCount(int routedCount) {
		this.routedCount = routedCount;
	}

	public String getRoutedQueues() {
		return routedQueues;
	}

	public void setRoutedQueues(String routedQueues) {
		this.routedQueues = routedQueues;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getPubTime() {
		return pubTime;
	}

	public void setPubTime(Date pubTime) {
		this.pubTime = pubTime;
	}

	public Date getLogTime() {
		return logTime;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
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

	public long getPubTimeLong() {
		return pubTimeLong;
	}

	public void setPubTimeLong(long pubTimeLong) {
		this.pubTimeLong = pubTimeLong;
	}

	public long getLogTimeLong() {
		return logTimeLong;
	}

	public void setLogTimeLong(long logTimeLong) {
		this.logTimeLong = logTimeLong;
	}
}