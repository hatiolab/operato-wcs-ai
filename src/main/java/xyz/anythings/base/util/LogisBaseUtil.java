package xyz.anythings.base.util;

import java.util.Date;
import java.util.HashMap;

import xyz.anythings.base.entity.BatchReceiptItem;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.event.main.IdGenerationEvent;
import xyz.anythings.base.model.CurrentDbTime;
import xyz.anythings.base.query.store.EtcQueryStore;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ThreadUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 물류 베이스 유틸리티
 * 
 * @author shortstop
 */
public class LogisBaseUtil {
	
	/**
	 * 배치 ID 생성을 위한 날짜 포맷
	 */
	private static final String DATE_FORMAT_FOR_BATCH_ID = "yyMMddHHmmssSSS";
	
	/**
	 * 데이터베이스 현재 시간
	 * 
	 * @return
	 */
	public static Date currentDbTime() {
		IQueryManager queryMgr = BeanUtil.get(IQueryManager.class);
		String query = BeanUtil.get(EtcQueryStore.class).getCurrentTimeQuery();
		return queryMgr.selectBySql(query, new HashMap<String, Object>(1), Date.class);
	}
	
	/**
	 * 데이터베이스 현재 시간 정보를 CurrentDbTime 객체로 변환
	 * 
	 * @return
	 */
	public static CurrentDbTime currentDbDateTime() {
		IQueryManager queryMgr = BeanUtil.get(IQueryManager.class);
		String query = BeanUtil.get(EtcQueryStore.class).getCurrentDateHourMinQuery();
		return queryMgr.selectBySql(query, new HashMap<String, Object>(1), CurrentDbTime.class);
	}
	
	/**
	 * 작업 배치 ID 생성
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static synchronized String newJobBatchId(Long domainId, String stageCd) {
		
		// 사이트 별 이벤트 생성 룰이 있다면 사용
		IdGenerationEvent event = new IdGenerationEvent(domainId, stageCd, EventConstants.EVENT_ID_GENERATION_BATCH_ID);
		event = (IdGenerationEvent)BeanUtil.get(EventPublisher.class).publishEvent(event);
		
		if(event.isAfterEventCancel()) {
			return ValueUtil.toString(event.getResult());
		}
		
		// 없다면 기본 배치 ID 생성 룰 사용
		String newBatchId = null;
		IQueryManager queryMgr = BeanUtil.get(IQueryManager.class);
		int count = 1;
		
		while(count > 0) {
			String currentTime = DateUtil.dateTimeStr(new Date(), DATE_FORMAT_FOR_BATCH_ID);
			newBatchId = domainId + SysConstants.DASH + currentTime;
			Query condition = AnyOrmUtil.newConditionForExecution(domainId, SysConstants.ENTITY_FIELD_ID);
			condition.addFilter("id", newBatchId);
			count = queryMgr.selectSize(JobBatch.class, condition);
			
			if(count > 0) {
				ThreadUtil.sleep(10);
			}
		}
		
		return newBatchId;
	}
	
	/**
	 * 작업 배치 ID 
	 * 
	 * @domainId
	 * @return
	 */
	public static synchronized String newReceiptJobBatchId(Long domainId) {
		String newBatchId = null;
		IQueryManager queryMgr = BeanUtil.get(IQueryManager.class);
		int count = 1;
		
		while(count > 0) {
			String currentTime = DateUtil.dateTimeStr(new Date(), DATE_FORMAT_FOR_BATCH_ID);
			newBatchId = domainId + SysConstants.DASH + currentTime;	
			Query condBatch = AnyOrmUtil.newConditionForExecution(domainId, SysConstants.ENTITY_FIELD_ID);
			condBatch.addFilter("id", newBatchId);
			
			Query condReceipt = AnyOrmUtil.newConditionForExecution(domainId, SysConstants.ENTITY_FIELD_ID);
			condReceipt.addFilter("batchId",newBatchId);
			
			count = queryMgr.selectSize(JobBatch.class, condBatch) + queryMgr.selectSize(BatchReceiptItem.class, condReceipt);
			
			if(count > 0) {
				ThreadUtil.sleep(10);
			}
		}
		
		return newBatchId;
	}
	
}
