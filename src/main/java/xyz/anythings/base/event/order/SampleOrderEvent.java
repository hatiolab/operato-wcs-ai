package xyz.anythings.base.event.order;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderSampler;
import xyz.anythings.sys.event.model.SysEvent;

/**
 * 샘플 주문 이벤트
 * 
 * @author shortstop
 */
public class SampleOrderEvent extends SysEvent {

	/**
	 * 작업 배치
	 */
	private JobBatch batch;
	/**
	 * 배치 생성 여부
	 */
	private boolean createBatchFlag = true;
	/**
	 * 주문 샘플러
	 */
	private OrderSampler orderSampler;
	/**
	 * 작업 유형
	 */
	private String jobType;
	
	public SampleOrderEvent(OrderSampler orderSampler) {
		this.setOrderSampler(orderSampler);
	}

	public JobBatch getBatch() {
		return batch;
	}

	public void setBatch(JobBatch batch) {
		this.batch = batch;
	}

	public boolean isCreateBatchFlag() {
		return createBatchFlag;
	}

	public void setCreateBatchFlag(boolean createBatchFlag) {
		this.createBatchFlag = createBatchFlag;
	}

	public OrderSampler getOrderSampler() {
		return orderSampler;
	}

	public void setOrderSampler(OrderSampler orderSampler) {
		this.orderSampler = orderSampler;
		this.jobType = this.orderSampler.getJobType();
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

}
