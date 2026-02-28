package xyz.anythings.sys.rest;

//import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.SysRestEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.sys.SysConstants;

/**
 * Dynamic API를 지원하기 위한 메소드 제공
 * 
 * @author shortstop
 */
public class DynamicControllerSupport {

	/**
	 * 이벤트 퍼블리셔
	 */
	@Autowired
	protected EventPublisher eventPublisher;

	/**
	 * 디바이스 관련 이벤트 퍼블리셔
	 * 
	 * @param event
	 * @return
	 */
	public BaseResponse restEventPublisher(SysRestEvent event) {
		this.eventPublisher.publishEvent(event);
		return event.getReturnResult();
	}

	/**
	 * path variable request 정리
	 * 
	 * @param request
	 * @return
	 */
	public String getRequestFinalPath(HttpServletRequest request) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

		AntPathMatcher apm = new AntPathMatcher();
		String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

		if (!finalPath.startsWith(SysConstants.SLASH)) {
			finalPath = SysConstants.SLASH + finalPath;
		}

		return finalPath;
	}

}
