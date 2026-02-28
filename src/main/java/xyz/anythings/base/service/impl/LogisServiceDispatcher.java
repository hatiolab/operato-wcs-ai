package xyz.anythings.base.service.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.service.api.IAssortService;
import xyz.anythings.base.service.api.IBatchService;
import xyz.anythings.base.service.api.IBoxingService;
import xyz.anythings.base.service.api.IClassificationService;
import xyz.anythings.base.service.api.IDeviceService;
import xyz.anythings.base.service.api.IIndicationService;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.base.service.api.IInvoiceNoService;
import xyz.anythings.base.service.api.IJobConfigProfileService;
import xyz.anythings.base.service.api.IJobStatusService;
import xyz.anythings.base.service.api.IPreprocessService;
import xyz.anythings.base.service.api.IReceiveBatchService;
import xyz.anythings.base.service.api.ISkuSearchService;
import xyz.anythings.base.service.api.IStockService;

/**
 * 작업 유형에 따른 서비스를 찾아주는 컴포넌트
 * 
 * @author shortstop
 */
@Component
public class LogisServiceDispatcher implements BeanFactoryAware {

	/**
	 * BeanFactory
	 */
	protected BeanFactory beanFactory;
	/**
	 * 주문 수신 서비스
	 */
	@Autowired
	private ReceiveBatchService receiveBatchService;
	/**
	 * 설정 셋 서비스
	 */
	@Autowired
	private JobConfigProfileService configSetService;
	/**
	 * 상품 조회 서비스
	 */
	@Autowired
	private SkuSearchService skuSearchService;
	/**
	 * 재고 서비스
	 */
	@Autowired
	private StockService stockService;
	/**
	 * 송장 번호 서비스
	 */
	@Autowired
	private InvoiceNoService invoiceNoService;
	/**
	 * 장비 서비스
	 */
	@Autowired
	private DeviceService deviceService;
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	/**
	 * 주문 수신 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param batchReceiverName
	 * @return
	 */
	public IReceiveBatchService getReceiveBatchService() {
		return this.receiveBatchService;
	}
	
	/**
	 * 설정 셋 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @return
	 */
	public IJobConfigProfileService getConfigSetService() {
		return this.configSetService;
	}
	
	/**
	 * 배치 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @return
	 */
	public IBatchService getBatchService(JobBatch batch) {
		return this.getBatchService(this.mapJobType(batch));
	}
	
	/**
	 * 작업 유형에 따른 배치 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IBatchService getBatchService(JobInstance job) {
		String jobType = this.mapJobType(job);
		String batchSvcType = jobType + "BatchService";
		return (IBatchService)this.beanFactory.getBean(batchSvcType);
	}
	
	/**
	 * 작업 유형에 따른 배치 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IBatchService getBatchService(String jobType) {
		jobType = this.mapJobType(jobType);
		String batchSvcType = jobType + "BatchService";
		return (IBatchService)this.beanFactory.getBean(batchSvcType);
	}	
	
	/**
	 * 배치의 작업 유형에 따른 주문 가공 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param batch
	 * @return
	 */
	public IPreprocessService getPreprocessService(JobBatch batch) {
		return this.getPreprocessService(this.mapJobType(batch));
	}
	
	/**
	 * 작업 유형에 따른 주문 가공 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IPreprocessService getPreprocessService(String jobType) {
		jobType = this.mapJobType(jobType);
		String preprocessSvcType = jobType + "PreprocessService";
		return (IPreprocessService)this.beanFactory.getBean(preprocessSvcType);
	}
	
	/**
	 * 배치의 작업 유형에 따른 작업 지시 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param batch
	 * @return
	 */
	public IInstructionService getInstructionService(JobBatch batch) {
		String jobType = this.mapJobType(batch);
		return this.getInstructionService(jobType);
	}
	
	/**
	 * 작업 유형에 따른 작업 지시 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IInstructionService getInstructionService(String jobType) {
		jobType = this.mapJobType(jobType);
		String instSvcType = jobType + "InstructionService";
		return (IInstructionService)this.beanFactory.getBean(instSvcType);
	}
	
	/**
	 * 상품 조회 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @return
	 */
	public ISkuSearchService getSkuSearchService() {
		return this.skuSearchService;
	}
	
	/**
	 * 재고 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @return
	 */
	public IStockService getStockService() {
		return this.stockService;
	}
	
	/**
	 * 송장 번호 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @return
	 */
	public IInvoiceNoService getInvoiceNoService() {
		return this.invoiceNoService;
	}
	
	/**
	 * 장비 서비스를 찾아서 리턴
	 * 
	 * @return
	 */
	public IDeviceService getDeviceService() {
		return this.deviceService;
	}
	
	/**
	 * 배치의 작업 유형에 따른 분류 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param batch
	 * @return
	 */
	public IClassificationService getClassificationService(JobBatch batch) {
		return this.getClassificationService(this.mapJobType(batch));
	}
	
	/**
	 * 작업 정보로 분류 서비스를 찾아 리턴
	 * 
	 * @param job
	 * @return
	 */
	public IClassificationService getClassificationService(JobInstance job) {
		return this.getClassificationService(this.mapJobType(job));
	}
	
	/**
	 * 박스 정보로 분류 서비스를 찾아 리턴
	 * 
	 * @param box
	 * @return
	 */
	public IClassificationService getClassificationService(BoxPack box) {
		return this.getClassificationService(this.mapJobType(box));
	}
	
	/**
	 * 작업 유형에 따른 분류 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IClassificationService getClassificationService(String jobType) {
		// FIXME 아래 분기하는 것 외 다른 방법 찾기
		String svcType = (LogisConstants.isDpsJobType(jobType) || LogisConstants.isSngJobType(jobType)) ? "PickingService" : "AssortService";
		String classSvcType = jobType.toLowerCase() + svcType;
		return (IClassificationService)this.beanFactory.getBean(classSvcType);
	}
	
	/**
	 * 작업 정보로 박싱 서비스를 찾아 리턴
	 * 
	 * @param batch
	 * @return
	 */
	public IBoxingService getBoxingService(JobBatch batch) {
		return this.getBoxingService(this.mapJobType(batch));
	}
	
	/**
	 * 작업 정보로 박싱 서비스를 찾아 리턴
	 * 
	 * @param job
	 * @return
	 */
	public IBoxingService getBoxingService(JobInstance job) {
		return this.getBoxingService(this.mapJobType(job));
	}
	
	/**
	 * 박스 정보로 박싱 서비스를 찾아 리턴
	 * 
	 * @param box
	 * @return
	 */
	public IBoxingService getBoxingService(BoxPack box) {
		return this.getBoxingService(this.mapJobType(box));
	}
	
	/**
	 * 작업 유형에 따른 박싱 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IBoxingService getBoxingService(String jobType) {
		jobType = this.mapJobType(jobType);
		String boxingSvcType = jobType + "BoxingService";
		return (IBoxingService)this.beanFactory.getBean(boxingSvcType);
	}
	
	/**
	 * 작업 정보로 분류 서비스를 찾아 리턴
	 * 
	 * @param job
	 * @return
	 */
	public IAssortService getPickService(JobInstance job) {
		return this.getPickService(this.mapJobType(job));
	}
	
	/**
	 * 박스 정보로 분류 서비스를 찾아 리턴
	 * 
	 * @param box
	 * @return
	 */
	public IAssortService getPickService(BoxPack box) {
		return this.getPickService(this.mapJobType(box));
	}
	
	/**
	 * 작업 유형에 따른 분류 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IAssortService getPickService(String jobType) {
		jobType = this.mapJobType(jobType);
		String assortSvcType = jobType + "PickingService";
		return (IAssortService)this.beanFactory.getBean(assortSvcType);
	}
	
	/**
	 * 작업 정보로 분류 서비스를 찾아 리턴
	 * 
	 * @param job
	 * @return
	 */
	public IAssortService getAssortService(JobInstance job) {
		return this.getAssortService(this.mapJobType(job));
	}
	
	/**
	 * 배치 정보로 분류 서비스를 찾아 리턴
	 * 
	 * @param batch
	 * @return
	 */
	public IAssortService getAssortService(JobBatch batch) {
		return this.getAssortService(this.mapJobType(batch));
	}
	
	/**
	 * 작업 유형에 따른 분류 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IAssortService getAssortService(String jobType) {
		jobType = this.mapJobType(jobType);
		String assortSvcType = jobType + "AssortService";
		return (IAssortService)this.beanFactory.getBean(assortSvcType);
	}

	/**
	 * 배치 작업에 따라작업 상태 서비스 컴포넌트를 찾아 리턴
	 * 
	 * @param batch
	 * @return
	 */
	public IJobStatusService getJobStatusService(JobBatch batch) {
		return this.getJobStatusService(this.mapJobType(batch));
	}
	
	/**
	 * 작업 유형에 따른 작업 상태 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IJobStatusService getJobStatusService(String jobType) {
		jobType = this.mapJobType(jobType);
		String jobStatusSvcType = jobType + "JobStatusService";
		return (IJobStatusService)this.beanFactory.getBean(jobStatusSvcType);
	}
	
	/**
	 * 배치 작업에 따라 표시기 점,소등 서비스 컴포넌트를 찾아 리턴
	 * 
	 * @param batch
	 * @return
	 */
	public IIndicationService getIndicationService(JobBatch batch) {
		return this.getIndicationService(this.mapJobType(batch));
	}
	
	/**
	 * 작업에 따라 표시기 점,소등 서비스 컴포넌트를 찾아 리턴
	 * 
	 * @param job
	 * @return
	 */
	public IIndicationService getIndicationService(JobInstance job) {
		return this.getIndicationService(this.mapJobType(job));
	}
	
	/**
	 * 작업 유형에 따른 표시기 점,소등 서비스 컴포넌트를 찾아서 리턴
	 * 
	 * @param jobType
	 * @return
	 */
	public IIndicationService getIndicationService(String jobType) {
		jobType = this.mapJobType(jobType);
		String indicationSvcType = jobType + "IndicationService";
		return (IIndicationService)this.beanFactory.getBean(indicationSvcType);
	}

	private String mapJobType(JobBatch batch) {
		return this.mapJobType(batch.getJobType());
	}
	
	private String mapJobType(JobInstance job) {
		return this.mapJobType(job.getJobType());
	}
	
	private String mapJobType(BoxPack box) {
		return this.mapJobType(box.getJobType());
	}
	
	private String mapJobType(String jobType) {		
		return jobType.toLowerCase();
	}
}
