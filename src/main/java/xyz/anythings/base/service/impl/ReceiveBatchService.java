package xyz.anythings.base.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.entity.BatchReceiptItem;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.event.main.BatchReceiveEvent;
import xyz.anythings.base.service.api.IReceiveBatchService;
import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.event.model.EventResultSet;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.service.AbstractExecutionService;
import xyz.elidom.core.entity.Code;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.core.rest.CodeController;
import xyz.elidom.util.ValueUtil;

/**
 * 배치 수신 서비스
 * 
 * @author shortstop
 */
@Component
public class ReceiveBatchService extends AbstractExecutionService implements IReceiveBatchService {

	/**
	 * 코드 컨트롤러
	 */
	@Autowired
	private CodeController codeCtrl;
	
	/**
	 * 상위 시스템으로 부터 구역, 스테이지, 고객사, 작업 일자로 배치 및 주문 수신을 위한 정보를 조회하여 리턴
	 * - 사용자가 수신 받을 배치가 있는지 확인한 후 수신하도록 하기 위함
	 * 
	 * @param domainId 도메인 ID
	 * @param areaCd 구역 코드 
	 * @param stageCd 스테이지 코드 
	 * @param comCd 고객사 코
	 * @param jobDate 작업 일자
	 * @param params 기타 파라미터
	 * @return
	 */
	public BatchReceipt readyToReceive(Long domainId, String areaCd, String stageCd, String comCd, String jobDate, Object ... params) {
		// 1. BatchReceipt 하나 생성
		int jobSeq = BatchReceipt.newBatchReceiptJobSeq(domainId, areaCd, stageCd, comCd, jobDate);
		BatchReceipt batchReceipt = new BatchReceipt();
		batchReceipt.setComCd(comCd);
		batchReceipt.setAreaCd(areaCd);
		batchReceipt.setStageCd(stageCd);
		batchReceipt.setJobDate(jobDate);
		batchReceipt.setJobSeq(ValueUtil.toString(jobSeq));
		batchReceipt.setStatus(LogisConstants.COMMON_STATUS_WAIT);
		//this.queryManager.insert(batchReceipt);
		
		if(ValueUtil.isEmpty(params)) {
			// 2. 모든 작업 유형을 찾는다. 공통 코드에서 찾음
			Code code = this.codeCtrl.findByName(domainId, LogisConstants.JOB_TYPE);
			List<CodeDetail> details = code.getItems();

			// 3. 각 작업 유형별로 이벤트 전달
			for(CodeDetail detail : details) {
				String jobType = detail.getName();
				this.readyToReceiveEvent(SysEvent.EVENT_STEP_BEFORE, domainId, jobType, areaCd, stageCd, comCd, jobDate, batchReceipt);
			}
			
		} else {
			// 2. 관련 작업 유형 추출 
			String jobTypes = ValueUtil.toString(params[0]);
			String[] jobTypeArr = jobTypes.split(LogisConstants.COMMA);
			
			// 3. 각 작업 유형별로 이벤트 전달
			for(int i = 0 ; i < jobTypeArr.length ; i++) {
				String jobType = jobTypeArr[i];
				this.readyToReceiveEvent(SysEvent.EVENT_STEP_BEFORE, domainId, jobType, areaCd, stageCd, comCd, jobDate, batchReceipt);
			}
		}
		
		// 4. 수신 정보가 있는지 체크 
		if(ValueUtil.isEmpty(batchReceipt.getItems())) {
			batchReceipt.setStatus(AnyConstants.COMMON_STATUS_FINISHED);
		}
		
		// 5. 수신 정보가 있다면 리턴
		return batchReceipt;
	}
	
	/**
	 * 상위 시스템으로 부터 배치, 주문을 수신
	 * 
	 * @param receipt
	 * @return
	 */
	public BatchReceipt startToReceive(BatchReceipt receipt) {
		// 1. 배치 수신 항목 추출 
		List<BatchReceiptItem> items = receipt.getItems();
		List<String> jobTypes = new ArrayList<String>();
		
		// 2. 작업 유형 추출 
		for(BatchReceiptItem item : items) {
			String jobType = item.getJobType();
			
			if(!jobTypes.contains(jobType)) {
				jobTypes.add(jobType);
			}
		}
		
		// 3. 수신 정보 상태 업데이트 - 진행 중 
		//receipt.updateStatusImmediately(LogisConstants.COMMON_STATUS_RUNNING);
		receipt.setStatus(LogisConstants.COMMON_STATUS_RUNNING);
		boolean isExceptionOccurred = false;
		
		// 4. 작업 유형별 순차적으로 주문 수신 
		for(String jobType : jobTypes) {
			this.startToReceiveEvent(SysEvent.EVENT_STEP_BEFORE, jobType, receipt);
			
			// 5. 에러 발생시 수신 중단 
			if(ValueUtil.isEqualIgnoreCase(receipt.getStatus(), LogisConstants.COMMON_STATUS_ERROR)) {
				isExceptionOccurred = true;
				break;
			}
		}
		
		// 6. 최종 상태 업데이트
		//receipt.updateStatusImmediately(isExceptionOccurred ? LogisConstants.COMMON_STATUS_ERROR : LogisConstants.COMMON_STATUS_FINISHED);
		receipt.setStatus(isExceptionOccurred ? LogisConstants.COMMON_STATUS_ERROR : LogisConstants.COMMON_STATUS_FINISHED);
		
		// 7. 배치 리턴
		return receipt;
	}
	
	/**
	 * 배치 수신 취소
	 * 
	 * @param batch
	 * @return
	 */
	public int cancelBatch(JobBatch batch) {
		// 취소 이벤트 발생 - 각 모듈에서 알아서 처리
		EventResultSet befResult = this.cancelBatchEvent(SysEvent.EVENT_STEP_BEFORE, batch);
		return (int)befResult.getResult();
	}
	
	/**
	 * 배치 수신 준비 이벤트 처리
	 * 
	 * @param eventStep
	 * @param domainId
	 * @param jobType
	 * @param areaCd
	 * @param stageCd
	 * @param comCd
	 * @param jobDate
	 * @param receiptData
	 * @param params
	 * @return
	 */
	private EventResultSet readyToReceiveEvent(
			short eventStep,
			Long domainId,
			String jobType,
			String areaCd,
			String stageCd,
			String comCd,
			String jobDate,
			BatchReceipt receiptData,
			Object ... params) {
		
		return this.publishBatchReceiveEvent(EventConstants.EVENT_RECEIVE_TYPE_RECEIPT, eventStep, domainId, jobType, areaCd, stageCd, comCd, jobDate, receiptData, null, params);
	}
	
	/**
	 * 배치 수신 이벤트 처리
	 * 
	 * @param eventStep
	 * @param jobType
	 * @param receipt
	 * @param params
	 * @return
	 */
	private EventResultSet startToReceiveEvent(short eventStep, String jobType, BatchReceipt receipt, Object ... params) {
		return this.publishBatchReceiveEvent(
				EventConstants.EVENT_RECEIVE_TYPE_RECEIVE,
				eventStep,
				receipt.getDomainId(),
				jobType,
				receipt.getAreaCd(),
				receipt.getStageCd(),
				receipt.getComCd(),
				receipt.getJobDate(),
				receipt,
				null,
				params);
	}
	
	/**
	 * 배치 취소 이벤트 처리
	 * 
	 * @param eventStep
	 * @param batch
	 * @param params
	 * @return
	 */
	private EventResultSet cancelBatchEvent(short eventStep, JobBatch batch, Object ... params) {
		return this.publishBatchReceiveEvent(
				EventConstants.EVENT_RECEIVE_TYPE_CANCEL,
				eventStep,
				batch.getDomainId(),
				batch.getJobType(),
				batch.getAreaCd(),
				batch.getStageCd(),
				batch.getComCd(),
				batch.getJobDate(),
				null,
				batch,
				params);
	}
	
	/**
	 * 배치 주문 수신 이벤트 처리
	 * 
	 * @param eventType
	 * @param eventStep
	 * @param domainId
	 * @param jobType
	 * @param areaCd
	 * @param stageCd
	 * @param comCd
	 * @param jobDate
	 * @param receiptData
	 * @param batch
	 * @param params
	 * @return
	 */
	private EventResultSet publishBatchReceiveEvent(
			short eventType, 
			short eventStep, 
			Long domainId, 
			String jobType, 
			String areaCd, 
			String stageCd, 
			String comCd, 
			String jobDate, 
			BatchReceipt receiptData, 
			JobBatch batch, 
			Object ... params) {
		
		// 1. 이벤트 생성 
		BatchReceiveEvent receiptEvent = new BatchReceiveEvent(domainId, eventType, eventStep);
		receiptEvent.setJobType(jobType);
		receiptEvent.setComCd(comCd);
		receiptEvent.setAreaCd(areaCd);
		receiptEvent.setStageCd(stageCd);
		receiptEvent.setJobDate(jobDate);
		receiptEvent.setJobBatch(batch);
		receiptEvent.setReceiptData(receiptData);
		receiptEvent.setPayload(params);
		
		// 2. Event Publish
		receiptEvent = (BatchReceiveEvent)this.eventPublisher.publishEvent(receiptEvent);
		return receiptEvent.getEventResultSet();
	}

}
