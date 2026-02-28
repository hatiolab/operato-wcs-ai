package xyz.anythings.base.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import xyz.anythings.sys.service.AbstractExecutionService;

/**
 * 물류 최상위 서비스
 *  
 * @author shortstop
 */
public class AbstractLogisService extends AbstractExecutionService {

	/**
	 * 물류 서비스 디스패처
	 */
	@Autowired
	protected LogisServiceDispatcher serviceDispatcher;
	
}
