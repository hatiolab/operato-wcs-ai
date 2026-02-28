package xyz.anythings.gw.event.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.anythings.comm.rabbitmq.event.MwErrorEvent;

/**
 * 메시지 관련 에러 이벤트 핸들러
 * 
 * @author shortstop
 */
@Component
public class MwErrorEventHandler {

	/**
	 * 메시지 관련 예외 핸들러
	 */
	@Autowired
	protected MwExceptionHandler mwExceptionHandler;

	/**
	 * 에러 이벤트를 처리
	 * 
	 * @param errorEvent
	 */
	@EventListener(classes = MwErrorEvent.class)
	public void handleMwErrorEvent(MwErrorEvent errorEvent) {
		this.mwExceptionHandler.handleMwException(errorEvent);
	}

}
