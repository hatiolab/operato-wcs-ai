package xyz.anythings.base.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.JobBatch;

/**
 * 작업 지시 서비스 Facade
 * 
 * @author shortstop
 */
@Component
public class InstructionService extends AbstractLogisService {

	/**
	 * 작업 지시를 위한 거래처 별 호기/로케이션 할당 정보 조회
	 * 
	 * @param batch
	 * @param params
	 * @return
	 */
	public Map<String, Object> searchInstructionData(JobBatch batch, Object ... params) {
		return this.serviceDispatcher.getInstructionService(batch).searchInstructionData(batch, params);
	}
		
	/**
	 * 작업 배치에 대한 작업 지시 생성 
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	public int instructBatch(JobBatch batch, List<String> equipIdList, Object ... params) {
		return this.serviceDispatcher.getInstructionService(batch).instructBatch(batch, equipIdList, params);
	}
	
	/**
	 * 토털 피킹 지시
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object ... params) {
		return this.serviceDispatcher.getInstructionService(batch).instructBatch(batch, equipIdList, params);
	}
		
	/**
	 * 작업 병합 - 메인 작업 배치에서 선택한 작업 배치를 배치 병합 
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param params
	 * @return
	 */
	public int mergeBatch(JobBatch mainBatch, JobBatch newBatch, Object ... params) {
		return this.serviceDispatcher.getInstructionService(mainBatch).mergeBatch(mainBatch, newBatch, params);
	}
	
	/**
	 * 작업 지시 취소 
	 * 
	 * @param batch
	 * @return
	 */
	public int cancelInstructionBatch(JobBatch batch) {
		return this.serviceDispatcher.getInstructionService(batch).cancelInstructionBatch(batch);
	}

}
