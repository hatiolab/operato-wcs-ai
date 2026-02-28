package xyz.anythings.base.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderSampler;
import xyz.anythings.base.event.order.SampleOrderEvent;
import xyz.anythings.sys.event.EventPublisher;

/**
 * 샘플 오더 생성기
 * 
 * @author shortstop
 */
@Component
public class SampleOrderService {
	
	/**
	 * 이벤트 퍼블리셔
	 */
	@Autowired
	protected EventPublisher eventPublisher;
	
	/**
	 * 샘플 오더 생성
	 * 
	 * @param sampler
	 * @return 생성한 배치 ID
	 */
	@Transactional
	public JobBatch createSampleOrder(OrderSampler sampler) {
		
		SampleOrderEvent event = new SampleOrderEvent(sampler);
		this.eventPublisher.publishEvent(event);
		return event.getBatch();
	}

}
