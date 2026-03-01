package operato.logis.sps.service.api;

import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.event.IClassifyRunEvent;

/**
 * 피킹 서비스 트랜잭션 API 
 * 
 * 1. 분류 모듈 정보
 * 		1) 작업 유형 
 * 		2) 작업 배치별 표시기 설정 정보
 * 		3) 작업 배치별 작업 설정 정보
 * 		4) 박스 처리 서비스 조회
 * 2. 투입
 * 		1) 스캔한 ID가 투입 유형에 따라 유효한 지 체크 및 어떤 투입 유형인 지 판단
 * 		2) 공 박스 투입
 * 		3) 공 트레이 투입
 * 3. 소분류 처리
 * 		1) 피킹 작업 처리
 * 		2) 피킹 작업 취소
 * 		3) 피킹 분할 작업 처리
 * 		4) 피킹 취소
 * 		5) 박싱 처리
 * 		6) 박싱 취소
 * 		7) 주문별 박스별 피킹 완료 여부 체크
 */
public interface ISpsPickingService {
	/**
	 * 분류 서비스 모듈의 작업 유형 (DAS, RTN, DPS, SPS) 리턴
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
	 * 작업 진행율 업데이트
	 * 
	 * @param batch
	 * @return
	 */
	public JobBatch updateProgressRate(JobBatch batch);
	
	/**
	 * 투입 ID로 유효성 체크 및 투입 유형을 찾아서 리턴
	 * 
	 * @param batch
	 * @param inputId
	 * @param params
	 * @return LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_...
	 */
	public String checkInput(JobBatch batch, String inputId, Object ... params);

	
	
	/**
	 * 단포 검수 박스 투입 전 체크 
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param boxId
	 * @param boxTypeCd
	 * @param params
	 * @return
	 */
	public Object inputBoxCheckWithInsp(JobBatch batch, String comCd, String skuCd, String boxId, String boxTypeCd, Integer orderQty, Object... params);
	
	/**
	 * 박스 투입
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param boxId
	 * @param boxTypeCd
	 * @param params
	 * @return
	 */
	public Object inputBox(JobBatch batch, String comCd, String skuCd, String boxId, String boxTypeCd, Object... params);

	/**
	 * 3-1. 소분류 : 피킹 작업 확정 처리
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 */
	public void confirmPick(IClassifyRunEvent exeEvent);
	
	/**
	 * 3-1. 소분류 : 피킹 작업 확정 처리 ( 검수 )
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 */
	public void confirmPickWithInsp(IClassifyRunEvent exeEvent);

	
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

}
