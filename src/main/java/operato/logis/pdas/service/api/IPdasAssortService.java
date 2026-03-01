package operato.logis.pdas.service.api;

import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.entity.JobInstance;

/**
 * PDAS 분류 처리 서비스 트랜잭션 API
 * 
 * @author shortstop
 */
public interface IPdasAssortService {

	/**
	 * 작업 유형 리턴
	 * 
	 * @return
	 */
	public String getJobType();
	
	/**
	 * 작업 배치별 작업 설정 정보
	 * 
	 * @param batchId
	 * @return
	 */
	public JobConfigSet getJobConfigSet(String batchId);
	
	/**
	 * 배치 시작 액션 처리
	 * 
	 * @param batch
	 */
	public void batchStartAction(JobBatch batch);
	
	/**
	 * 배치 마감시 액션
	 * 
	 * @param batch
	 */
	public void batchCloseAction(JobBatch batch);
	
	/**
	 * 셀의 작업 상태 체크
	 * 
	 * @param batch
	 * @param stationCd
	 * @param cellCd
	 * @return
	 */
	public JobInstance checkCellJobStatus(JobBatch batch, String stationCd, String cellCd);
	
	/**
	 * 박스 상태 체크
	 * 
	 * @param batch
	 * @param stationCd
	 * @param boxId
	 * @return
	 */
	public BoxPack checkBoxStatus(JobBatch batch, String stationCd, String boxId);
	
	/**
	 * 상품 코드를 스캔하여 분류할 작업을 조회한다.
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param stationCd
	 * @return
	 */
	public JobInstance findJobToAssort(JobBatch batch, String comCd, String skuCd, String stationCd);
	
	/**
	 * 상품 스캔시 조회한 작업(주문)과 셀을 매핑
	 * 
	 * @param batch
	 * @param jobInstanceId
	 * @param stationCd
	 * @param cellCd
	 * @return
	 */
	public JobInstance assignJobToCell(JobBatch batch, String jobInstanceId, String stationCd, String cellCd);
	
	/**
	 * 상품 스캔시 조회한 작업(주문)과 셀을 매핑
	 * 
	 * @param batch
	 * @param job
	 * @param stationCd
	 * @param cellCd
	 * @return
	 */
	public JobInstance assignJobToCell(JobBatch batch, JobInstance job, String stationCd, String cellCd);
	
	/**
	 * 셀에 주문과 박스 매핑
	 * 
	 * @param batch
	 * @param jobInstanceId
	 * @param stationCd
	 * @param cellCd
	 * @param boxId
	 * @return
	 */
	public JobInstance assignOrderToBox(JobBatch batch, String jobInstanceId, String stationCd, String cellCd, String boxId);
	
	/**
	 * 셀에 주문과 박스 매핑
	 * 
	 * @param batch
	 * @param jobInstance
	 * @param stationCd
	 * @param cellCd
	 * @param boxId
	 * @return
	 */
	public JobInstance assignOrderToBox(JobBatch batch, JobInstance jobInstance, String stationCd, String cellCd, String boxId);
	
	/**
	 * 중분류 확정 처리
	 * 
	 * @param batch
	 * @param jobInstanceId
	 * @return
	 */
	public JobInstance middleAssortJob(JobBatch batch, String jobInstanceId);
	
	/**
	 * 작업 분류 처리
	 * 
	 * @param batch
	 * @param jobInstanceId
	 * @param cellCd
	 * @param stationCd
	 * @param fromIndicator 표시기에서 처리 여부
	 * @return
	 */
	public JobInstance assortJob(JobBatch batch, String jobInstanceId, String cellCd, String stationCd);
	
	/**
	 * 주문 분류 완료 후 박싱 처리
	 * 
	 * @param batch
	 * @param jobInstanceId
	 * @param boxId
	 * @param boxReusable
	 * @param stationCd
	 * @return
	 */
	public JobInstance boxingJob(JobBatch batch, String jobInstanceId, String boxId, boolean boxReusable, String stationCd);
	
	/**
	 * 피킹 완료는 되었는데 박싱 처리가 안 된 주문을 완료 처리
	 * 
	 * @param batch
	 * @return
	 */
	// public int boxingUnboxedList(JobBatch batch);
	
	/**
	 * 표시기 사용 여부 리턴
	 * 
	 * @param domainId
	 * @return
	 */
	public boolean isUseIndicator(JobBatch batch);
	
	/**
	 * 주문 - 셀 시스템이 자동 매핑할 지 여부 (수동 매핑인 경우 작업자가 매핑하고자 하는 셀을 선택함)
	 * 
	 * @param domainId
	 * @return
	 */
	public boolean isCellMappingAutoMode(JobBatch batch);
	
	/**
	 * 중분류 실행 여부
	 * 
	 * @param domainId
	 * @return
	 */
	public boolean isUseMiddleAssorting(JobBatch batch);
	
	/**
	 * 주문 - 박스 매핑을 분류하기 전에 할 지(선 매핑) 여부
	 * 
	 * @param domainId
	 * @return
	 */
	public boolean isBoxMappingPreMode(JobBatch batch);
	
	/**
	 * 낱개 피킹 모드 여부
	 * 
	 * @param domainId
	 * @return
	 */
	public boolean isPiecePickingMode(JobBatch batch);
	
	/**
	 * 버킷 사용 여부 - 버킷 사용시는 버킷 투입시 버킷 Validation 필요
	 * 
	 * @param domainId
	 * @return
	 */
	public boolean isUseBucket(JobBatch batch);
}
