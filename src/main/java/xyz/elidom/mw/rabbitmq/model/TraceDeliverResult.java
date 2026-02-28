package xyz.elidom.mw.rabbitmq.model;

import java.util.Date;

import xyz.elidom.mw.rabbitmq.entity.TraceDead;
import xyz.elidom.mw.rabbitmq.entity.TraceDeliver;

/**
 * 메시지 트레이스 뷰어 상세 화면 리스트 모델
 * 
 * @author yang
 */
public class TraceDeliverResult {
	/**
	 * 목적지 ID
	 */
	private String destId;
	/**
	 * 로그 시간
	 */
	private Date logTime;
	/**
	 * Delivery 실패 여부
	 */
	private Boolean isFailed;
	
	/**
	 * 생성자
	 * 
	 * @param trace
	 */
	public TraceDeliverResult(TraceDeliver trace) {
		this.setDestId(trace.getDestId());
		this.setLogTime(trace.getLogTime());
		this.setIsFailed(false);
	}
	
	/**
	 * 생성자
	 * 
	 * @param trace
	 */
	public TraceDeliverResult(TraceDead trace) {
		this.setDestId(trace.getDestId());
		this.setLogTime(trace.getDeadTime());
		this.setIsFailed(true);
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
	
	public Boolean getIsFailed() {
		return isFailed;
	}
	
	public void setIsFailed(Boolean isFailed) {
		this.isFailed = isFailed;
	}
}
