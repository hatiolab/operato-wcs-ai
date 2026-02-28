package xyz.elidom.rabbitmq.message;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * elings-rabbitmq 기본 메시지프로퍼티 
 * @author yang
 *
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MessageProperties {
	/**
	 * 메시지 아이디 
	 */
	private String id;
	/**
	 * 발송 시간 : timestamp
	 */
	private long time;
	/**
	 * 발신자 아이디 
	 */
	private String sourceId;
	/**
	 * 목적지 라우팅 키 
	 */
	private String destId;
	/**
	 * Ack 유무 
	 */
	private Boolean isReply;
	
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
}
