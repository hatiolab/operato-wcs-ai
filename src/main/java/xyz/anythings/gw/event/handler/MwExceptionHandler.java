package xyz.anythings.gw.event.handler;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.comm.rabbitmq.event.MwErrorEvent;
import xyz.anythings.gw.service.mq.model.MessageObject;
import xyz.anythings.gw.service.util.MwMessageUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.exception.ElidomException;
import xyz.elidom.rabbitmq.message.MessageProperties;
import xyz.elidom.sys.entity.ErrorLog;
import xyz.elidom.sys.rest.ErrorLogController;
import xyz.elidom.sys.util.ExceptionUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.ClassUtil;
import xyz.elidom.util.DateUtil;

/**
 * 메시지 처리 중 발생한 예외 처리를 위한 핸들러
 * 
 * @author shortstop
 */
@Component
public class MwExceptionHandler {

	/**
	 * logger
	 */
	private Logger logger = LoggerFactory.getLogger(MwExceptionHandler.class);
	/**
	 * 에러 로그 컨트롤러
	 */
	@Autowired
	private ErrorLogController errLogCtrl;
	
	/**
	 * 미들웨어 에러 메시지 처리
	 * 
	 * @param domainId
	 * @param mwEvent
	 * @param exception
	 * @param fileLoggingFlag
	 * @param dbLoggingFlag
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW) 
	public void handleMwException(MwErrorEvent errorEvent) {
		// 1. 예외 추출
		Exception ex = errorEvent.getException();

		// 1. 파일 로깅
		if(errorEvent.isFileLoggingFlag()) {
			this.logger.error(ex.getMessage(), ex);
		}
		
		// 2. DB 로깅 여부 확인
		if(!errorEvent.isDbLoggingFlag()) {
			return;
		}
		
		ElidomException ee = ExceptionUtil.wrapElidomException(ex);
		
		if (ee == null || !ee.isWritable()) {
			return;
		}
		
		ErrorLog errLog = new ErrorLog();
		MessageObject msgObj = MwMessageUtil.toMessageObject(errorEvent.getMwEvent());
		String code = ee.getCode();
		Field field = ClassUtil.getField(ErrorLog.class, "code");
		
		if (ValueUtil.isNotEmpty(field)) {
			int codeSize = field.getAnnotation(Column.class).length();
			if(code.length() > codeSize) {
				errLog.setHeader(code);
				code = null;
			}
		}
		
		errLog.setDomainId(errorEvent.getDomainId());
		errLog.setCode(code);
		errLog.setStatus(String.valueOf(ee.getStatus()));
		
		MessageProperties prop = msgObj.getProperties();
		errLog.setErrorType("Messaging Error");
		errLog.setUri(prop.getId());
		errLog.setMethod(prop.getIsReply() ? "Reply" : "Request");
		errLog.setMessage(MwMessageUtil.messageObjectToJson(msgObj));
		errLog.setParams(msgObj.getBody().getAction());
		errLog.setIssueDate(DateUtil.currentTimeStr());
		
		String traceStr = ExceptionUtil.getErrorStackTraceToString(ee.getCause() != null ? ee.getCause() : ee);
		errLog.setStackTrace(traceStr);
		errLog.setCreatorId("mpi");
		errLog.setUpdaterId("mpi");
		this.errLogCtrl.create(errLog);
	}
	
}
