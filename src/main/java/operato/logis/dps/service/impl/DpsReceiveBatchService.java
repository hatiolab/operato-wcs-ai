package operato.logis.dps.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.entity.BatchReceiptItem;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.OrderLabel;
import xyz.anythings.base.event.main.BatchReceiveEvent;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.base.util.LogisBaseUtil;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.ElidomException;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * DPS 주문 수신용 서비스
 * 
 * @author shortstop
 */
@Component
public class DpsReceiveBatchService extends AbstractQueryService {

	/**
	 * DPS용 주문 수신 준비 서비스 (수신할 주문의 웨이브별 주문 건수 카운팅을 위한 커스텀 서비스)
	 */
	private static final String DIY_DPS_READY_TO_RECEIVE_ORDER_SERVICE = "diy-dps-ready-to-receive-order";
	/**
	 * DPS용 웨이브별 주문 수신 처리 서비스
	 */
	private static final String DIY_DPS_START_TO_RECEIVE_ORDER_SERVICE = "diy-dps-start-to-receive-order";
	/**
	 * DPS용 웨이브별 주문 수신 취소 처리 서비스
	 */
	private static final String DIY_DPS_CANCEL_BATCH_SERVICE = "diy-dps-cancel-batch";
	
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	private ICustomService customService;

	/**
	 * DPS 주문 정보 수신을 위한 수신 서머리 정보 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.eventType == 10 and #event.eventStep == 1 and #event.jobType == 'DPS'")
	public void handleReadyToReceive(BatchReceiveEvent event) {
		
		BatchReceipt receipt = event.getReceiptData();
		receipt.setDomainId(event.getDomainId());
		String jobType = event.getJobType();
		receipt = this.readyToReceiveOrders(receipt,jobType);
		event.setReceiptData(receipt);
	}
	
	/**
	 * DPS 주문 정보 수신 시작
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.eventType == 20 and #event.eventStep == 1 and #event.jobType == 'DPS'")
	public void handleStartToReceive(BatchReceiveEvent event) {
		
		BatchReceipt receipt = event.getReceiptData();
		List<BatchReceiptItem> items = receipt.getItems();
		 
		for(BatchReceiptItem item : items) {
			if(ValueUtil.isEqualIgnoreCase(LogisConstants.JOB_TYPE_DPS, item.getJobType())) {
				this.startToReceiveOrders(receipt, item);
			}
		}
		
		event.setReceiptData(receipt);
	}
	
	/**
	 * DPS 주문 수신 취소 처리
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.eventType == 30 and #event.eventStep == 1 and #event.jobType == 'DPS'")
	public void handleCancelReceived(BatchReceiveEvent event) {
		
		// 1. 작업 배치 추출
		JobBatch batch = event.getJobBatch();
		
		// 2. 배치 상태 체크
		Long domainId = event.getDomainId();
		String sql = "select status from job_batches where domain_id = :domainId and id = :id";
		Map<String, Object> params = ValueUtil.newMap("domainId,id", domainId, batch.getId());
		String currentStatus = AnyEntityUtil.findItem(domainId, true, String.class, sql, params);
		
		if(ValueUtil.isNotEqual(currentStatus, JobBatch.STATUS_WAIT) && ValueUtil.isNotEqual(currentStatus, JobBatch.STATUS_READY)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CANCEL_POSSIBLE_WAIT_OR_READY_STATE"));
		}
		
		// 3. 주문 취소시 데이터 유지 여부에 따라서
		Map<String, Object> batchParam = ValueUtil.newMap("batch", batch);
		boolean isDeleteData = BatchJobConfigUtil.isDeleteWhenOrderCancel(batch);
		int cancelledCnt = isDeleteData ? this.cancelOrderDeleteData(batch) : this.cancelOrderKeepData(batch);
		
		// 4. 커스텀 서비스 호출
		this.customService.doCustomService(domainId, DIY_DPS_CANCEL_BATCH_SERVICE, batchParam);
		
		// 5. 이벤트에 결과 설정
		event.setResult(cancelledCnt);
	}
	
	/**
	 * 배치 수신 서머리 데이터 생성
	 * 
	 * @param receipt
	 * @param jobType
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BatchReceipt readyToReceiveOrders(BatchReceipt receipt, String jobType, Object ... params) {
		
		// 1. 커스텀 로직으로 부터 수신 대상 데이터 확인
		List<BatchReceiptItem> receiptItems = null;
		Object retObj = this.customService.doCustomService(receipt.getDomainId(), DIY_DPS_READY_TO_RECEIVE_ORDER_SERVICE, ValueUtil.newMap("batchReceipt", receipt));
		
		if(retObj != null) {
			receiptItems = (List<BatchReceiptItem>) retObj;
		} else {
			throw ThrowUtil.newValidationErrorWithNoLog("Not found custom service [" + DIY_DPS_READY_TO_RECEIVE_ORDER_SERVICE + "]");
		}
		
		// 2. 수신 아이템 데이터 생성
		for(BatchReceiptItem item : receiptItems) {
			if(ValueUtil.isEmpty(item.getBatchId())) {
				item.setBatchId(LogisBaseUtil.newReceiptJobBatchId(receipt.getDomainId()));
			}
			
			item.setBatchReceiptId(receipt.getId());
			receipt.addItem(item);
		}
		
		// 3. 수신 아이템 설정 및 리턴
		return receipt;
	}
	
	/**
	 * 각 수신 항목별 주문 수신 처리
	 * 
	 * @param receipt
	 * @param item
	 * @param params
	 * @return
	 */
	private BatchReceipt startToReceiveOrders(BatchReceipt receipt, BatchReceiptItem item, Object ... params) {
		
		try {
			// 1. skip 이면 pass
			if(item.getSkipFlag()) {
				item.setStatus(LogisConstants.COMMON_STATUS_SKIPPED);
				return receipt;
			}
			
			// 2. BatchReceiptItem 상태 업데이트 - 진행 중
			item.setStatus(LogisConstants.COMMON_STATUS_RUNNING);
			
			// 3. JobBatch 생성
			JobBatch batch = JobBatch.createJobBatch(item.getBatchId(), ValueUtil.toString(item.getJobSeq()), receipt, item);
			
			// 4. 데이터 복사
			Map<String, Object> customParams = ValueUtil.newMap("batchReceipt,receiptItem,batch", receipt, item, batch);
			this.customService.doCustomService(receipt.getDomainId(), DIY_DPS_START_TO_RECEIVE_ORDER_SERVICE, customParams);
			
			// 5. batchReceiptItem 상태 업데이트
			item.setStatus(LogisConstants.COMMON_STATUS_FINISHED);
			
		} catch(Throwable th) {
			// 6. 에러 처리
			this.handleReceiveError(th, receipt, item);
		}
		
		return receipt;
	}
	
	/**
	 * 주문 수신시 에러 핸들링
	 * 
	 * @param th
	 * @param receipt
	 * @param item
	 */
	public void handleReceiveError(Throwable th, BatchReceipt receipt, BatchReceiptItem item) {
		
		// 예외 처리
		String errMsg = th.getCause() != null ? th.getCause().getMessage() : th.getMessage();
		errMsg = errMsg.length() > 400 ? errMsg.substring(0,400) : errMsg;
		item.setStatus(LogisConstants.COMMON_STATUS_ERROR);
		item.setMessage(errMsg);
		receipt.setStatus(LogisConstants.COMMON_STATUS_ERROR);
		
		// 예외 전달
		throw th instanceof ElidomException ? (ElidomException)th : new ElidomRuntimeException(th);
	}

	/**
	 * 주문 데이터 삭제 처리
	 * 
	 * @param batch
	 * @return
	 */
	private int cancelOrderKeepData(JobBatch batch) {
		int cnt = 0;
		
		// 1. 배치 상태 업데이트
		batch.updateStatus(JobBatch.STATUS_CANCEL);
		
		// 2. 주문 조회 
		List<Order> orderList = AnyEntityUtil.searchEntitiesBy(batch.getDomainId(), false, Order.class, "id", "batchId", batch.getId());
		
		// 3. 취소 상태
		for(Order order : orderList) {
			order.setStatus(Order.STATUS_CANCEL);
		}
		
		// 4. 배치 update
		this.queryManager.updateBatch(orderList, "status");
		cnt += orderList.size();

		// 5. 처리 수량 리턴
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
		cnt += this.queryManager.deleteList(Order.class, condition);
		this.queryManager.deleteList(OrderLabel.class, condition);
		
		// 3. 배치 삭제 
		this.queryManager.delete(batch);
		
		// 4. 삭제 처리 수량 리턴
		return cnt;
	}

}
