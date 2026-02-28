package xyz.anythings.sys.service;

import org.springframework.beans.factory.annotation.Autowired;

import xyz.anythings.sys.event.EventPublisher;

/**
 * 실행 가능 기능
 * 
 * @author shortstop
 */
public class AbstractExecutionService extends AbstractQueryService {

	@Autowired
	protected EventPublisher eventPublisher;
}
