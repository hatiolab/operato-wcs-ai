package xyz.anythings.comm.rabbitmq.event;

import xyz.anythings.sys.event.model.SysEvent;
import xyz.elidom.rabbitmq.client.event.SystemMessageReceiveEvent;

/**
 * 에러 이벤트 
 * 
 * @author shortstop
 */
public class MwErrorEvent extends SysEvent {

	/**
	 * 예외 유형 
	 */
	private String errorType;
	/**
	 * 예외
	 */
	private Exception exception;
	/**
	 * 미들웨어 이벤트
	 */
	private SystemMessageReceiveEvent mwEvent;
	/**
	 * 서비스 호출 파라미터
	 */
	private String parameters;
	/**
	 * 파일 로깅 여부
	 */
	private boolean fileLoggingFlag;
	/**
	 * DB 로깅 여부
	 */
	private boolean dbLoggingFlag;
	
	public MwErrorEvent() {
	}
	
	public MwErrorEvent(Long domainId, String errorType, Exception exception, String parameters, boolean fileLoggingFlag, boolean dbLoggingFlag) {
		super();
		this.setDomainId(domainId);
		this.setErrorType(errorType);
		this.setException(exception);
		this.setParameters(parameters);
		this.setFileLoggingFlag(fileLoggingFlag);
		this.setDbLoggingFlag(dbLoggingFlag);
	}
	
	public MwErrorEvent(Long domainId, SystemMessageReceiveEvent event, Exception exception, boolean fileLoggingFlag, boolean dbLoggingFlag) {
		super();
		this.setDomainId(domainId);
		this.setMwEvent(event);
		this.setException(exception);
		this.setFileLoggingFlag(fileLoggingFlag);
		this.setDbLoggingFlag(dbLoggingFlag);
	}
	
	public String getErrorType() {
		return errorType;
	}
	
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	
	public Exception getException() {
		return exception;
	}
	
	public void setException(Exception exception) {
		this.exception = exception;
	}

	public SystemMessageReceiveEvent getMwEvent() {
		return mwEvent;
	}

	public void setMwEvent(SystemMessageReceiveEvent mwEvent) {
		this.mwEvent = mwEvent;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public boolean isFileLoggingFlag() {
		return fileLoggingFlag;
	}

	public void setFileLoggingFlag(boolean fileLoggingFlag) {
		this.fileLoggingFlag = fileLoggingFlag;
	}

	public boolean isDbLoggingFlag() {
		return dbLoggingFlag;
	}

	public void setDbLoggingFlag(boolean dbLoggingFlag) {
		this.dbLoggingFlag = dbLoggingFlag;
	}

}
