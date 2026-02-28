package xyz.anythings.base.service.api;

import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.event.IClassifyInEvent;
import xyz.anythings.base.event.IClassifyRunEvent;

/**
 * 피킹 서비스 트랜잭션 API 
 * 
 * 	1. 분류 모듈 정보
 * 		1) 작업 유형 
 * 		2) 작업 배치별 표시기 설정 정보
 * 		3) 작업 배치별 작업 설정 정보
 * 		4) 박스 처리 서비스 조회
 * 	2. 투입
 * 		1) 스캔한 ID가 투입 유형에 따라 유효한 지 체크 및 어떤 투입 유형인 지 판단
 * 		2) 공 박스 투입
 * 		3) 공 트레이 투입
 * 	3. 소분류 처리
 * 		1) 피킹 작업 처리
 * 		2) 피킹 작업 취소
 * 		3) 피킹 분할 작업 처리
 * 		4) 피킹 취소
 * 		5) 박싱 처리
 * 		6) 박싱 취소
 * 		7) 주문별 박스별 피킹 완료 여부 체크
 * 		8) 스테이션에 투입된 주문별 피킹 작업 완료 여부 체크
 */
public interface IPickingService extends IClassificationService {

	/**
	 * 1-4. 모듈별 박싱 처리 서비스
	 * 
	 * @param params
	 * @return
	 */
	public IBoxingService getBoxingService(Object ... params);
	
	/**
	 * 2-2. 투입 : 배치 작업에 공 박스 투입
	 * 
	 * @param inputEvent
	 * @return
	 */
	public Object inputEmptyBox(IClassifyInEvent inputEvent);

	/**
	 * 2-3. 투입 : 배치 작업에 공 트레이 투입
	 * 
	 * @param inputEvent
	 * @return
	 */
	public Object inputEmptyTray(IClassifyInEvent inputEvent);
	
	/**
	 * 3-1. 소분류 : 피킹 작업 확정 처리
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 */
	public void confirmPick(IClassifyRunEvent exeEvent);
	
	/**
	 * 3-2. 소분류 : 피킹 취소 (예정 수량보다 분류 처리할 실물이 작아서 처리할 수 없는 경우 취소 처리)
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 */
	public void cancelPick(IClassifyRunEvent exeEvent);
	
	/**
	 * 3-3. 소분류 : 수량을 조정하여 분할 피킹 처리
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 * @return
	 */
	public int splitPick(IClassifyRunEvent exeEvent);
	
	/**
	 * 3-4. 소분류 : 피킹 확정 처리된 작업 취소
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 * @return
	 */
	public int undoPick(IClassifyRunEvent exeEvent);
	
	/**
	 * 3-5. 소분류 : 박스 처리
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 * @return
	 */
	public BoxPack fullBoxing(IClassifyRunEvent exeEvent);
		
	/**
	 * 3-6. 소분류 : Boxing 취소
	 * 
	 * @param domainId
	 * @param boxPackId
	 * @return
	 */
	public BoxPack cancelBoxing(Long domainId, String boxPackId);
	
	/**
	 * 3-7. 소분류 : 주문별 박스별 피킹 완료 여부 체크
	 * 
	 * @param batch
	 * @param orderId
	 * @param boxId
	 * @return
	 */
	public boolean checkBoxingEnd(JobBatch batch, String orderId, String boxId);
	
	/**
	 * 3-8. 소분류 : 스테이션에 투입된 주문별 피킹 작업 완료 여부 체크
	 * 
	 * @param batch
	 * @param stationCd
	 * @param job
	 * @return
	 */
	public boolean checkStationJobsEnd(JobBatch batch, String stationCd, JobInstance job);

}