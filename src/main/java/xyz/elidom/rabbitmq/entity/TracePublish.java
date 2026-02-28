package xyz.elidom.rabbitmq.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.rabbitmq.model.trace.ITraceModel;
import xyz.elidom.util.FormatUtil;


/**
 * publish 메시지 로그 
 * @author yang
 *
 */
@Table(name = "mq_trace_publish_log")
public class TracePublish implements ITraceModel{

	@Ignore
	private String type="trace_pub";
	
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
	 * 수신 대상 라우팅키 
	 */
	@PrimaryKey
	@Column(name = "dest_id", nullable = false, length = 255)
	private String destId;

	/**
	 * Ack 유무 
	 */
	@Column(name = "is_reply")
	private Boolean isReply;

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
	 * 장비 아이디 (mpi, gw, mobile, ecs ....... )
	 */
	@Column(name = "equip_id", length=255)
	private String equipId;

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

	@Override
	public String toJsonString() {
		/*
		String jsonStr = "{\"type\": \"trace_pub\",\"id\": \"%s\",\"source_id\": \"%s\",\"dest_id\": \"%s\",\"is_reply\": %s,\"routed_count\": %s,\"routed_queues\": \"%s\",\"body\": \"%s\",\"pub_time\": %s,\"log_time\": %s,\"site\": \"%s\"}";
		return String.format(jsonStr, this.getId(), this.getSourceId(), this.getDestId(), 
				this.isReply(), this.getRoutedCount(), this.getRoutedQueues(), 
				this.getBody().replaceAll("\\\"", "\\\\\""), 
				this.getPubTime().getTime(), this.getLogTime().getTime(), 
				this.getSite());
				
		*/
		
		
//		return FormatUtil.toUnderScoreJsonString(this);
		return FormatUtil.toJsonString(this, false);
	}

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}


/*
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TracePublishModel {
	
	private TracePropertiesModel properties;

	private String site;
	private int routedCount;
	private String routedQueues;
	private long logTime;
	private String body;
	
	
	
//    var publishQry = 'insert into trace_publish_log (id,is_reply,source_id,dest_id,pub_time,routed_count,routed_queues,log_time,body,site) ';
//    publishQry += ' values( $1, $2, $3, $4, to_timestamp($5::double precision/1000), $6, $7, now(), $8, $9); '

	
	public TracePropertiesModel getProperties() {
		return properties;
	}
	public void setProperties(TracePropertiesModel properties) {
		this.properties = properties;
	}
	
	public String getId() {
		return this.properties.getId();
	}
	public boolean isReply() {
		return this.properties.isReply();
	}
	public String getSourceId() {
		return this.properties.getSourceId();
	}
	public String getDestId() {
		return this.properties.getDestId();
	}
	public long getPubTime() {
		return this.properties.getTime();
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
	public long getLogTime() {
		return logTime;
	}
	public void setLogTime(long logTime) {
		this.logTime = logTime;
	}
	public String getBody() {
		return body;
	}
	public void setBody(Object body) {
		
//		JSONWrappedObject
		
		if(body == null) {
			this.body = "";
		} else if(body.getClass().equals(String.class)) {
			this.body = body.toString();
		} else {
			this.body = body.toString();
		}
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
}
*/