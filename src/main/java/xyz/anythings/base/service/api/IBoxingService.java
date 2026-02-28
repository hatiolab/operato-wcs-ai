package xyz.anythings.base.service.api;

import java.util.List;

import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;

/**
 * 박스 처리 서비스 API
 * 	1. 분류 모듈 정보
 * 		1) 작업 유형 
 * 		2) 작업 배치별 표시기 설정 정보
 * 		3) 작업 배치별 작업 설정 정보
 * 	2. 작업 준비
 * 		1) 작업 유형에 따른 거래처, 상품, 주문 등이 매핑된 셀 리스트 조회
 * 		2) 로케이션 - 박스 매핑을 분류 처리 작업 전에 하는 경우 작업 할당된 로케이션의 박스 ID를 표시기에 표시하는 기능 (공박스 매핑이 안 된 로케이션은 nobox로 표시)
 * 		3) 로케이션에 박스를 매핑
 * 		4) 로케이션에 박스 매핑 클리어
 * 		5) 로케이션 - 박스 매핑 체크
 * 	3. 박스 처리
 * 		1) 박싱 처리 : 풀 박스 처리 (B2B), 주문별 분류 처리 완료 (B2C)
 * 		2) 박싱 취소
 * 		3) 배치 내 박싱 처리 안 된 박스 일괄 박싱 처리
 * 		4) 박스(송장) 라벨 재발행
 * 		5) 로케이션 별 박스 최종 완료 처리
 * 
 * @author shortstop
 */
public interface IBoxingService {
	
	/**
	 * 1-1. 분류 모듈 정보 : 분류 서비스 모듈의 작업 유형 (DAS, RTN, DPS, QPS) 리턴 
	 * 
	 * @return
	 */
	public String getJobType();
	
	/**
	 * 1-2. 분류 모듈 정보 : 작업 배치별 작업 설정 정보
	 * 
	 * @param batchId
	 * @return
	 */
	public JobConfigSet getJobConfigSet(String batchId);
		
	/**
	 * 2-1. 작업 준비 : 셀에 박스를 할당
	 * 
	 * @param batch
	 * @param cellCd
	 * @param boxId
	 * @param params
	 * @return
	 */
	public Object assignBoxToCell(JobBatch batch, String cellCd, String boxId, Object ... params);
	
	/**
	 * 2-2. 작업 준비 : 셀에 할당된 박스 ID 해제
	 * 
	 * @param batch
	 * @param cellCd
	 * @param params
	 * @return
	 */
	public Object resetBoxToCell(JobBatch batch, String cellCd, Object ... params);
	
	/**
	 * 박싱 처리
	 * 
	 * @param batch
	 * @param workCell b2b인 경우 필요, b2c인 경우 불필요
	 * @param jobList
	 * @param params
	 * @return
	 */
	public BoxPack fullBoxing(JobBatch batch, WorkCell workCell, List<JobInstance> jobList, Object ... params);
	
	/**
	 * 수량 조절 후 박싱 처리
	 * 
	 * @param batch
	 * @param workCell b2b인 경우 필요, b2c인 경우 불필요
	 * @param jobList
	 * @param fullboxQty
	 * @param params
	 * @return
	 */
	public BoxPack partialFullboxing(JobBatch batch, WorkCell workCell, List<JobInstance> jobList, Integer fullboxQty, Object ... params);
		
	/**
	 * 작업 배치에 대해서 박싱 작업이 안 된 모든 박스의 박싱을 완료한다.
	 * 
	 * @param batch
	 * @return
	 */
	public List<BoxPack> batchBoxing(JobBatch batch);
	
	/**
	 * 박싱 취소
	 * 
	 * @param box
	 * @return
	 */
	public BoxPack cancelFullboxing(BoxPack box);
	
	/**
	 * 박스 ID가 이미 사용된 것인지 체크
	 * 
	 * @param batch
	 * @param boxId
	 * @param exceptionWhenBoxIdUsed
	 * @return
	 */
	public boolean isUsedBoxId(JobBatch batch, String boxId, boolean exceptionWhenBoxIdUsed);
	
}
