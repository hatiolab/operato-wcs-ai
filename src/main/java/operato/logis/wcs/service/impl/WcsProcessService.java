package operato.logis.wcs.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.logis.wcs.entity.Wave;
import operato.logis.wcs.event.WaveReceiveEvent;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.entity.BatchReceiptItem;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.event.main.BatchInstructEvent;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.EventResultSet;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * WCS Wave 수신용 서비스
 * 
 * @author shortstop
 */
@Component
public class WcsProcessService extends AbstractQueryService {
	
	/**
	 * Wave 수신을 위한 정보 조회 커스텀 서비스
	 */
	public static final String DIY_READY_TO_RECEIVE_WAVE = "diy-ready-to-receive-wave";
	/**
	 * Wave 수신 커스텀 서비스
	 */
	public static final String DIY_START_TO_RECEIVE_WAVE = "diy-start-to-receive-wave";
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	
	/**
	 * 이벤트 퍼블리셔 
	 */
	@Autowired
	protected EventPublisher eventPublisher;

	
	/**
	 * Wave 수신을 위한 수신 서머리 정보 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = WaveReceiveEvent.class, condition = "#event.eventType == 10 and #event.eventStep == 1")
	public void handleReadyToReceiveWave(WaveReceiveEvent event) {
		BatchReceipt receipt = event.getReceiptData();
		receipt = this.readyToReceiveWave(receipt);
		event.setReceiptData(receipt);
	}
	
	/**
	 * Wave 수신 시작
	 * 
	 * @param event
	 */
	@EventListener(classes = WaveReceiveEvent.class, condition = "#event.eventType == 20 and #event.eventStep == 1")
	public void handleStartToReceiveWave(WaveReceiveEvent event) {
		BatchReceipt receipt = event.getReceiptData();
		List<BatchReceiptItem> items = receipt.getItems();
		WcsProcessService self = BeanUtil.get(WcsProcessService.class);
		
		for(BatchReceiptItem item : items) {
			if(!item.getSkipFlag()) {
				self.startToReceiveWave(receipt, item);
			}
		}
	}
	
	/**
	 * Wave 수신 취소
	 * 
	 * @param event
	 */
	@EventListener(classes = WaveReceiveEvent.class, condition = "#event.eventType == 30 and #event.eventStep == 1")
	public void handleCancelReceived(WaveReceiveEvent event) {
		// 1. 작업 배치 추출 
		JobBatch batch = event.getJobBatch();
		
		// 2. 배치 상태 체크
		String sql = "select status from job_batches where domain_id = :domainId and id = :id";
		Map<String, Object> params = ValueUtil.newMap("domainId,id", batch.getDomainId(), batch.getId());
		String currentStatus = AnyEntityUtil.findItem(batch.getDomainId(), true, String.class, sql, params);
		
		// 3. Wave 상태 체크
		if(ValueUtil.isNotEmpty(currentStatus)) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("CANCEL_POSSIBLE_WAVE_STATE_NULL"));
		}
		
		// 4. Wave 취소 체크
		if(ValueUtil.isEqualIgnoreCase(currentStatus, JobBatch.STATUS_CANCEL)) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("ALREADY_CANCELED"));
		}
		
		// 3. 주문 취소시 데이터 유지 여부에 따라서
		boolean isKeepData = BatchJobConfigUtil.isDeleteWhenOrderCancel(batch);
		int cancelledCnt = isKeepData ? this.cancelOrderKeepData(batch) : this.cancelOrderDeleteData(batch);
		event.setResult(cancelledCnt);
	}
	
	/**
	 * Wave 대상 분류
	 * 
	 * @param wave
	 * @param classifyCodes
	 * @return
	 */
	public Wave classifyWaves(Wave wave, List<String> classifyCodes) {
		
		// 1. 작업 타입별 대상분류 이벤트 전송 
		int jobTypeCount = classifyCodes.size();
		for(int i = 0 ; i < jobTypeCount ; i++) {
			String jobType = classifyCodes.get(i);
			this.publishWaveClassifyEvent(wave, jobType, (i == (jobTypeCount - 1) ? true : false));
		}
		
		// 2. 대상분류 결과 wave 데이터 조회 
		Query query = AnyOrmUtil.newConditionForExecution(wave.getDomainId());
		query.setSelect(ValueUtil.newStringList("orderNo"));
		query.setGroup(ValueUtil.newStringList("orderNo"));
		query.setFilter("batchId", wave.getId());
		int orderQty = this.queryManager.selectSize(Order.class, query);
		
		query.setSelect(ValueUtil.newStringList("comCd", "skuCd"));
		query.setGroup(ValueUtil.newStringList("comCd", "skuCd"));
		int skuQty = this.queryManager.selectSize(Order.class, query);
		
		String sql = "select sum(order_qty) from orders where domain_id = :domainId and batch_id = :batchId";
		int totalPcs = this.queryManager.selectBySql(sql, ValueUtil.newMap("domainId,batchId", wave.getDomainId(), wave.getId()), Integer.class);
		
		// 3. 대상분류 결과 wave 데이터 반영  
		wave.setOrderQty(orderQty);
		wave.setSkuQty(skuQty);
		wave.setTotalPcs(totalPcs);
		
		// 4. 대상 분류가 완료 되었을 경우에만 상태 update 
		if(totalPcs == 0 ) { 
			wave.setStatus("CONFIRMED");
		}
		
		// 5. Wave 업데이트
		wave.setUpdatedAt(new Date());
		this.queryManager.update(wave, "orderQty", "skuQty", "totalPcs", "status", "updatedAt");
		
		// 6. wave 리턴
		return wave;
	}
	
	/**
	 * Wave 분할 개수로 균등 분할
	 * 
	 * @param mainWave
	 * @param splitCount
	 * @return
	 */
	public List<Wave> splitWavesByEvenly(Wave mainWave, int splitCount) {
		// TODO
		return null;
	}
	
	/**
	 * Wave 주문 수량으로 분할
	 * 
	 * @param mainWave
	 * @param splitOrderQty
	 * @return
	 */
	public List<Wave> splitWavesByOrderQty(Wave mainWave, int splitOrderQty) {
		// TODO
		return null;
	}
	
	/**
	 * Wave 병합
	 * 
	 * @param mainWave
	 * @param targetWave
	 * @return
	 */
	public Wave mergeWave(Wave mainWave, Wave targetWave) {
		// TODO
		return null;
	}
	
	/**
	 * Wave 확정 & 설비 전송
	 * 
	 * @param batch
	 * @return
	 */
	public Wave confirmWave(Wave batch) {
		// TODO
		return null;
	}
	
	/**
	 * 상위 Legacy 시스템으로 부터 수신할 Wave 조회
	 * 상위 Legacy 시스템은 정해지지가 않았으므로 커스텀 서비스로 구현한다.
	 * 
	 * @param receipt
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BatchReceipt readyToReceiveWave(BatchReceipt receipt, Object ... params) {
		// 1. ID 설정
		if(ValueUtil.isEmpty(receipt.getId())) {
			receipt.setId(receipt.getDomainId() + LogisConstants.DASH + receipt.getJobDate());
		}
		
		// 1. 파라미터 설정 및 커스텀 서비스 호출
		Map<String, Object> parameters = ValueUtil.newMap("condition", receipt);
		Object resultObj = this.customService.doCustomService(receipt.getDomainId(), DIY_READY_TO_RECEIVE_WAVE, parameters);
		if(ValueUtil.isNotEmpty(resultObj)) {
			receipt.setItems((List<BatchReceiptItem>)resultObj);
		}
		List<BatchReceiptItem> receiptItems = receipt.getItems();
		
		// 2 수신 아이템 데이터 생성
		if(ValueUtil.isNotEmpty(receiptItems)) {
			for(BatchReceiptItem item : receiptItems) {
				item.setBatchReceiptId(receipt.getId());
			}
		}
		
		// 3. 수신 아이템 설정 및 리턴
		return receipt;
	}
	
	/**
	 * Wave 수신
	 * 
	 * @param receipt
	 * @param item
	 * @param params
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private Wave startToReceiveWave(BatchReceipt receipt, BatchReceiptItem item, Object ... params) {
		Wave wave = null;
		
		try {
			Map<String, Object> parameters = ValueUtil.newMap("item", item);
			wave = (Wave)this.customService.doCustomService(receipt.getDomainId(), DIY_START_TO_RECEIVE_WAVE, parameters);
			item.setStatus(LogisConstants.COMMON_STATUS_FINISHED);
			
		} catch(Throwable th) {
			String errMsg = th.getCause() != null ? th.getCause().getMessage() : th.getMessage();
			errMsg = errMsg.length() > 400 ? errMsg.substring(0,400) : errMsg;
			item.setStatus(LogisConstants.COMMON_STATUS_ERROR);
			item.setMessage(errMsg);
			receipt.setStatus(LogisConstants.COMMON_STATUS_ERROR);
		}

		return wave;
	}
	
	/**
	 * 주문 데이터 삭제 update
	 * 
	 * seq = 0
	 * @param batch
	 * @return
	 */
	private int cancelOrderKeepData(JobBatch batch) {
		int cnt = 0;
		
		// 1. 배치 상태  update
		batch.updateStatus(JobBatch.STATUS_CANCEL);
		
		// 2. 주문 조회
		List<Order> orderList = AnyEntityUtil.searchEntitiesBy(batch.getDomainId(), false, Order.class, "id", "batchId", batch.getId());
		
		// 3. 취소 상태 , seq = 0 셋팅
		for(Order order : orderList) {
			order.setStatus(Order.STATUS_CANCEL);
			order.setJobSeq("0");
		}
		
		// 4. 배치 update
		this.queryManager.updateBatch(orderList, "jobSeq", "status");
		cnt += orderList.size();
		
		// 5. 주문 가공 데이터 삭제
		cnt += this.deleteBatchPreprocessData(batch);
		return cnt;
	}
	
	/**
	 * 주문 데이터 삭제
	 * 
	 * @param batch
	 * @return
	 */
	private int cancelOrderDeleteData(JobBatch batch) {
		int cnt = 0;
		
		// 1. 삭제 조건 생성
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		
		// 2. 삭제 실행
		cnt+= this.queryManager.deleteList(Order.class, condition);
		
		// 3. 주문 가공 데이터 삭제
		cnt += this.deleteBatchPreprocessData(batch);
		
		// 4. 배치 삭제
		this.queryManager.delete(batch);
		
		return cnt;
	}
	
	/**
	 * 주문 가공 데이터 삭제
	 * 
	 * @param batch
	 * @return
	 */
	private int deleteBatchPreprocessData(JobBatch batch) {
		// 1. 삭제 조건 생성
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		
		// 2. 삭제 실행
		return this.queryManager.deleteList(OrderPreprocess.class, condition);
	}

	
	/**
	 * 대상 분류 이벤트 퍼블리쉬 
	 * @param wave
	 * @param jobType
	 * @param isLastType : 대상 분류 마지막 작업 타입 ( Wave 에서 남은 주문을 모두 가져가야 한다. )
	 * @return
	 */
	private EventResultSet publishWaveClassifyEvent(Wave wave, String jobType, boolean isLastType) {
		// 1. 이벤트 생성 
		BatchInstructEvent event = new BatchInstructEvent(wave.getDomainId(), EventConstants.EVENT_INSTRUCT_TYPE_CLASSIFICATION, SysEvent.EVENT_STEP_ALONE);
		event.setJobType(jobType);
		event.setPayload(ValueUtil.newList(wave.getId(), isLastType).toArray());
		
		// 2. event publish
		event = (BatchInstructEvent)this.eventPublisher.publishEvent(event);
		return event.getEventResultSet();

	}
}
