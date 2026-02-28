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
 * deliver 메시지 로그 
 * @author yang
 *
 */
@Table(name = "mq_trace_deliver_log")
public class TraceDeliver implements ITraceModel{

	@Ignore
	private String type="trace_sub";
	
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
	 * 소비 큐 이름 
	 */
	@PrimaryKey
	@Column(name = "dest_id", nullable = false, length = 255)
	private String destId;
	
	/**
	 * 로그 기록 시간 
	 */
	@Column (name = "log_time", type = ColumnType.DATETIME)
	private Date logTime;
	
	/**
	 * 시이트 코드 
	 */
	@Column(name = "site", length=100)
	private String site;
	
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
	public long getLogTimeLong() {
		return logTimeLong;
	}

	public void setLogTimeLong(long logTimeLong) {
		this.logTimeLong = logTimeLong;
	}

	@Override
	public String toJsonString() {
		return FormatUtil.toJsonString(this, false);
	}
}
