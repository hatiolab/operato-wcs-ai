package xyz.anythings.base.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.event.main.BatchInstructEvent;
import xyz.anythings.sys.event.model.EventResultSet;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.util.ValueUtil;

/**
 * 작업 지시 최상위 서비스
 * 
 * @author yang
 */
public class AbstractInstructionService extends AbstractLogisService {

	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	
	/**
	 * 배치 데이터에 대해 설비 정보 여부 를 찾아 대상 설비 리스트를 리턴
	 * 
	 * @param jobBatch
	 * @param equipIdList
	 * @return
	 */
	protected List<?> searchEquipListByBatch(JobBatch batch, List<String> equipIdList) {
		
		Class<?> masterEntity = null;
		
		//1. 설비 타입에 대한 마스터 엔티티 구분
		if(ValueUtil.isEqual(batch.getEquipType(), LogisConstants.EQUIP_TYPE_RACK)) {
			masterEntity = Rack.class;
		} else {
			// TODO : 소터 등등등 추가
			return null;
		}
		
		// 2. 작업 대상 설비 ID가 있으면 
		if(ValueUtil.isNotEmpty(equipIdList)) {
			return this.searchEquipListByEquipIds(batch.getDomainId(), masterEntity, equipIdList);
			
		// 3. 작업 대상 설비 타입만 지정되어 있으면
		} else {
			return this.searchEquipListByEquipType(masterEntity, batch);
		}
	}
	
	/**
	 * ID 리스트로 설비 마스터 조회 
	 * 
	 * @param domainId
	 * @param clazz
	 * @param equipIdList
	 * @return
	 */
	protected <T> List<T> searchEquipListByEquipIds(long domainId, Class<T> clazz, List<String> equipIdList) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter(LogisConstants.ENTITY_FIELD_ID, LogisConstants.IN, equipIdList);
		return this.queryManager.selectList(clazz,condition);
	}
	
	/**
	 * 작업 배치 정보로 설비 마스터 리스트 조회
	 *  
	 * @param clazz
	 * @param batch
	 * @return
	 */
	protected <T> List<T> searchEquipListByEquipType(Class<T> clazz, JobBatch batch) {
		return AnyEntityUtil.searchEntitiesBy(batch.getDomainId(), false, clazz, null
				, "areaCd,stageCd,activeFlag,jobType,batchId"
				, batch.getAreaCd(), batch.getStageCd(), Boolean.TRUE, batch.getJobType(), batch.getId());
	}	
	
	/*******************************************************************************************************************
	 * 											배치 작업 지시 이벤트 처리
	 *******************************************************************************************************************/
	
	/**
	 * 작업 지시 이벤트 전송
	 * 
	 * @param eventStep
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	protected EventResultSet publishInstructionEvent(short eventStep, JobBatch batch, List<?> equipList, Object... params) {
		return this.publishInstructEvent(EventConstants.EVENT_INSTRUCT_TYPE_INSTRUCT, eventStep, batch, equipList, params);
	}
	
	/**
	 * 작업 지시 취소 이벤트 전송
	 * 
	 * @param eventStep
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	protected EventResultSet publishInstructionCancelEvent(short eventStep, JobBatch batch, List<?> equipList, Object... params) {
		return this.publishInstructEvent(EventConstants.EVENT_INSTRUCT_TYPE_INSTRUCT_CANCEL, eventStep, batch, equipList, params);
	}
	
	/**
	 * 작업 병합 이벤트 전송
	 * 
	 * @param eventStep
	 * @param mainBatch
	 * @param newBatch
	 * @param equipList
	 * @param params
	 * @return
	 */
	protected EventResultSet publishMergingEvent(short eventStep, JobBatch mainBatch, JobBatch newBatch, List<?> equipList, Object... params) {
		return this.publishInstructEvent(EventConstants.EVENT_INSTRUCT_TYPE_MERGE, eventStep, mainBatch, equipList, newBatch, params);
	}
	
	/**
	 * 작업 지시 공통 이벤트 전송
	 * 
	 * @param eventType
	 * @param eventStep
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	protected EventResultSet publishInstructEvent(short eventType, short eventStep, JobBatch batch, List<?> equipList, Object... params) {
		
		// 1. 이벤트 생성 
		BatchInstructEvent event = new BatchInstructEvent(batch.getDomainId(), eventType, eventStep);
		event.setJobBatch(batch);
		event.setJobType(batch.getJobType());
		event.setEquipType(batch.getEquipType());
		event.setEquipList(equipList);
		event.setPayload(params);
		
		// 2. event publish
		event = (BatchInstructEvent)this.eventPublisher.publishEvent(event);
		return event.getEventResultSet();
	}

}
