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
 * 미들웨어 Error 메시지 로그 저장을 위한 엔티티
 * 
 * @author yang
 */
@Table(name = "mq_trace_error_log")
public class TraceError implements ITraceModel {

	@Ignore
	private String type="trace_err";

	@PrimaryKey
	@Column(name = "id", nullable = false, length = 40)
	private String id;
	
	/**
	 * 에러 발생 일시 
	 */
	@Column (name = "err_date", type = ColumnType.DATETIME)
	private Date errDate;

	/**
	 * 에러 타입 
	 */
	@Column (name = "trace_type", length=100)
	private String traceType;
	
	/**
	 * 에러 발생 구간 prop
	 */
	@Column(name = "message_prop", type = ColumnType.TEXT)
	private String messageProp;

	/**
	 * 에러 발생 구간 전체 바디 
	 * 메시지를 건건이 처리하는 게 아니라 모아서 처리를 하므로 해당 구간에 대한 전체 메시지 가 포함 됨  
	 */
	@Column(name = "message_body", type = ColumnType.TEXT)
	private String messageBody;
	
	/**
	 * error 메시지 
	 */
	@Column(name = "err_trace", type = ColumnType.TEXT)
	private String errTrace;

	/**
	 * elastic 시간 정보 저정 하기 위한 타입 변경 
	 */
	@Ignore
	private long errDateLong;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Date getErrDate() {
		return errDate;
	}

	public void setErrDate(Date errDate) {
		this.errDate = errDate;
	}

	public String getTraceType() {
		return traceType;
	}

	public void setTraceType(String traceType) {
		this.traceType = traceType;
	}

	public String getMessageProp() {
		return messageProp;
	}

	public void setMessageProp(String messageProp) {
		this.messageProp = messageProp;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getErrTrace() {
		return errTrace;
	}

	public void setErrTrace(String errTrace) {
		this.errTrace = errTrace;
	}
	

	@Override
	public String toJsonString() {
		return FormatUtil.toJsonString(this, false);
	}

	public long getErrDateLong() {
		return errDateLong;
	}

	public void setErrDateLong(long errDateLong) {
		this.errDateLong = errDateLong;
	}
}