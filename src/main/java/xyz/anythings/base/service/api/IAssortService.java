package xyz.anythings.base.service.api;

import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.IClassifyInEvent;
import xyz.anythings.base.event.IClassifyOutEvent;
import xyz.anythings.base.event.IClassifyRunEvent;

/**
 * 기본 분류 (Assorting) 서비스 트랜잭션 API 
 * 
 * 	1. 분류 모듈 정보
 * 		1) 작업 유형 
 * 		2) 작업 배치별 표시기 설정 정보
 * 		3) 작업 배치별 작업 설정 정보
 *      4) 박스 처리 서비스
 * 	2. 투입
 * 		1) 스캔한 ID가 투입 유형에 따라 유효한 지 체크 및 어떤 투입 유형인 지 판단
 * 		2) 단건 상품 투입
 * 		3) 묶음 상품 투입
 * 		4) 박스 단위 상품 투입
 * 		5) 검수 투입
 * 	3. 소분류 처리
 * 		1) 분류 작업 처리
 * 		2) 분류 작업 취소
 * 		3) 수량 분할 작업 처리
 * 		4) 분류 확정 취소
 * 		5) 박싱 처리
 * 		6) 수량 조정 후 박싱 처리
 * 		7) 박싱 취소
 * 		8) 박스 처리를 위한 작업 수량 분할 처리
 * 		9) 스테이션 영역에 투입된 작업 분류 처리 완료 여부 체크
 * 		10) 셀 분류 완료 여부 체크
 * 		11) 셀 분류 최종 완료 처리
 * 
 * @author shortstop
 */
public interface IAssortService extends IClassificationService {

	/**
	 * 1-4. 모듈별 박싱 처리 서비스
	 * 
	 * @param params
	 * @return
	 */
	public IBoxingService getBoxingService(Object ... params);
	
	/**
	 * 2-2. 투입 : 배치 작업에 단건 상품 투입 
	 * 
	 * @param inputEvent
	 * @return
	 */
	public Object inputSkuSingle(IClassifyInEvent inputEvent);

	/**
	 * 2-3. 투입 : 배치 작업에 묶음 단위 상품 투입 
	 * 
	 * @param inputEvent
	 * @return
	 */
	public Object inputSkuBundle(IClassifyInEvent inputEvent);
	
	/**
	 * 2-4. 투입 : 배치 작업에 완박스 단위 상품 투입 
	 * 
	 * @param inputEvent
	 * @return
	 */
	public Object inputSkuBox(IClassifyInEvent inputEvent);
	
	/**
	 * 2-5. 검수 투입
	 * 
	 * @param inputEvent
	 * @return
	 */
	public Object inputForInspection(IClassifyInEvent inputEvent);

	/**
	 * 3-1. 소분류 : 분류 작업 확정 처리
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 */
	public void confirmAssort(IClassifyRunEvent exeEvent);
	
	/**
	 * 3-2. 소분류 : 작업 취소 (예정 수량보다 분류 처리할 실물이 작아서 처리할 수 없는 경우 취소 처리)
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 */
	public void cancelAssort(IClassifyRunEvent exeEvent);
	
	/**
	 * 3-3. 소분류 : 수량을 조정하여 분할 확정 처리
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 * @return
	 */
	public int splitAssort(IClassifyRunEvent exeEvent);
	
	/**
	 * 3-4. 소분류 : 피킹 확정 처리된 작업 취소
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 * @return
	 */
	public int undoAssort(IClassifyRunEvent exeEvent);

	/**
	 * 3-5. 소분류 : 박스 처리
	 * 
	 * @param outEvent 분류 작업 이벤트
	 * @return
	 */
	public BoxPack fullBoxing(IClassifyOutEvent outEvent);
	
	/**
	 * 3-6. 소분류 : 수량 조정 후 박스 처리  
	 * 
	 * @param outEvent 분류 작업 이벤트
	 * @return 박스 처리 결과
	 */
	public BoxPack partialFullboxing(IClassifyOutEvent outEvent);
	
	/**
	 * 3-7. 소분류 : Boxing 취소
	 * 
	 * @param domainId
	 * @param boxPack
	 * @return
	 */
	public BoxPack cancelBoxing(Long domainId, BoxPack boxPack);
	
	/**
	 * 3-8. 기타 : 작업 정보의 처리 수량을 splitQty 수량으로 분할 처리 후 분할 처리한 작업을 리턴
	 * 
	 * @param batch
	 * @param job
	 * @param location
	 * @param splitQty
	 * @return
	 */
	public JobInstance splitJob(JobBatch batch, JobInstance job, WorkCell workCell, int splitQty);
	
	/**
	 * 3-9. 소분류 : 스테이션 영역에 투입된 작업 분류 처리 완료 여부 체크
	 * 
	 * @param job
	 * @param stationCd
	 * @return
	 */
	public boolean checkStationJobsEnd(JobInstance job, String stationCd);
	
	/**
	 * 3-10. 소분류 : 셀 분류 완료 여부 체크
	 * 
	 * @param job
	 * @param finalEndCheck
	 * @return
	 */
	public boolean checkCellAssortEnd(JobInstance job, boolean finalEndCheck);

	/**
	 * 3-11. 소분류 : 셀 별 분류 작업에 대한 최종 완료 처리
	 * 
	 * @param job
	 * @param workCell
	 * @param finalEndFlag 최종 완료인 지 (true) 아니면 셀의 분류는 종료되었지만 최종 완료 버튼을 눌러야 할 상황의 완료(false)인 지 여부 
	 * @return
	 */
	public boolean finishAssortCell(JobInstance job, WorkCell workCell, boolean finalEndFlag);
	
	/**
	 * 박싱 처리를 위해 작업 배치 내 셀 내에서 처리할 작업 조회
	 * 
	 * @param domainId
	 * @param batchId
	 * @param cellCd
	 * @return
	 */
	public JobInstance findLatestJobForBoxing(Long domainId, String batchId, String cellCd);
}
