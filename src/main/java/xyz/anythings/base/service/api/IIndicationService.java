package xyz.anythings.base.service.api;

import java.util.List;
import java.util.Map;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.gw.entity.Gateway;
import xyz.anythings.gw.service.api.IIndRequestService;

/**
 * 표시기 관련 서비스
 * 
 *  1. 표시기 관련
 *  	1) 작업 배치로 게이트웨이 리스트를 조회
 *  	2) 조회 조건으로 표시기 점등을 위한 작업 리스트 조회
 *  	3) 작업 리스트로 표시기 점등
 *  	4) 표시기에 피킹 점등 요청
 *  	5) 표시기에 풀 박스 점등 요청
 *  	6) 표시기에 분류 완료 표시 요청
 *  	7) 표시기에 검수 표시 요청
 *   	8) 표시기 소등 요청
 *  	9) 표시기에 박스 매핑 점등 요청
 *  	10) 표시기에 박스 매핑이 안 된 에러 표시
 *  	11) 표시기에 문자열 표시
 *  	12) 표시기에 셀 코드 표시
 *  	13) 표시기에 표시기 코드 표시
 *  	14) 투입 이벤트로 표시기 점등 
 *  	15) 설비별, 작업 존 별 작업 리스트 조회 후 표시기 재점등 - KIOSK 화면에서 재점등 
 *  	16) 작업 배치별, 게이트웨이 별 작업 리스트 조회 후 표시기 재점등 - 게이트웨이 리부팅 시
 *  	17) 투입 정보, 장비 별 작업 리스트 조회 후 표시기 재점등 - 모바일 장비 화면에서 재점등
 *  	18) 이전 표시기 버튼 색상으로 다음 표시기 색상을 결정
 *  	19) 표시기 교체
 *  
 * @author shortstop
 */
public interface IIndicationService {
	
	/**
	 * 1-0. 표시기 점, 소등 요청 서비스
	 * 
	 * @param batch
	 * @return
	 */
	public IIndRequestService getIndicatorRequestService(JobBatch batch);
	
	/**
	 * 1-0. 표시기 점, 소등 요청 서비스
	 * 
	 * @param batchId
	 * @return
	 */
	public IIndRequestService getIndicatorRequestService(String batchId);
	
	/**
	 * 1-1. 작업 배치로 게이트웨이 리스트를 조회
	 * 
	 * @param batch
	 * @return
	 */
	public List<Gateway> searchGateways(JobBatch batch);
	
	/**
	 * 1-2. 조회 조건으로 표시기 점등을 위한 작업 리스트 조회
	 * 
	 * @param batch
	 * @param condition
	 * @return
	 */
	public List<JobInstance> searchJobsForIndOn(JobBatch batch, Map<String, Object> condition);
	
	/**
	 * 게이트웨이 리부팅
	 * 
	 * @param batch
	 * @param router
	 */
	public void rebootGateway(JobBatch batch, Gateway router);
	
	/**
	 * 1-3. 작업 리스트로 표시기 점등
	 * 
	 * @param batch
	 * @param relight 재점등 여부
	 * @param jobList 작업 리스트
	 * @return 표시기 점등된 작업 리스트
	 */
	public List<JobInstance> indicatorsOn(JobBatch batch, boolean relight, List<JobInstance> jobList);
	
	/**
	 * 1-3. 작업 리스트로 표시기 점등
	 * 
	 * @param batch
	 * @param relight 재점등 여부
	 * @param action 표시기 작업 액션 
	 * @param jobList 작업 리스트
	 * @return 표시기 점등된 작업 리스트
	 */
	public List<JobInstance> indicatorsOn(JobBatch batch, boolean relight, String action, List<JobInstance> jobList);
	
	/**
	 * 1-4. 표시기에 피킹 정보 점등
	 * 
	 * @param job
	 * @param firstQty
	 * @param secondQty
	 * @param thirdQty
	 */
	public void indicatorOnForPick(JobInstance job, Integer firstQty, Integer secondQty, Integer thirdQty);
	
	/**
	 * 1-5. 표시기에 풀 박스 점등 요청
	 * 
	 * @param job
	 */
	public void indicatorOnForFullbox(JobInstance job);
	
	/**
	 * 1-6. 표시기에 분류 완료 표시 요청
	 * 
	 * @param job
	 * @param finalEnd
	 */
	public void indicatorOnForPickEnd(JobInstance job, boolean finalEnd);
	
	/**
	 * 1-7. 표시기에 검수를 위한 표시기 표시 요청
	 * 
	 * @param job
	 */
	public void indicatorOnForInspect(JobInstance job);
	
	/**
	 * 1-8. 작업 배치 전체 표시기 소등 요청
	 * 
	 * @param batch
	 */
	public void indicatorOffAll(JobBatch batch);
	
	/**
	 * 1-8. 작업 배치 전체 표시기 강제 소등 요청
	 * 
	 * @param batch
	 * @param forceOff
	 */
	public void indicatorOffAll(JobBatch batch, boolean forceOff);
	
	/**
	 * 1-8. 호기의 작업 스테이션 영역의 표시기 소등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 */
	public void indicatorListOff(Long domainId, String stageCd, String equipType, String equipCd, String stationCd);
	
	/**
	 * 1-8. 호기에 진행 중인 작업 배치에 작업 스테이션 영역의 피킹 중인 작업에 대한 표시기 소등 요청
	 * 
	 * @param batch
	 * @param stationCd
	 */
	public void indicatorListOff(JobBatch batch, String stationCd);
	
	/**
	 * 1-8. 표시기 리스트 소등 처리
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param indCdList
	 */
	public void indicatorListOff(Long domainId, String stageCd, String gwPath, List<String> indCdList);
	
	/**
	 * 1-8. 표시기 소등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param indCd
	 */
	public void indicatorOff(Long domainId, String stageCd, String gwPath, String indCd);
	
	/**
	 * 1-8. 표시기 소등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param indCd
	 */
	public void indicatorOff(Long domainId, String stageCd, String indCd);
	
	/**
	 * 1-9. 표시기에 박스 매핑 점등 요청
	 * 
	 * @param batch
	 * @param gwPath
	 * @param indCd
	 */
	public void displayForBoxMapping(JobBatch batch, String gwPath, String indCd);
	
	/**
	 * 1-9. 표시기에 박스 매핑 점등 요청
	 * 
	 * @param batch
	 * @param indCd
	 */
	public void displayForBoxMapping(JobBatch batch, String indCd);
	
	/**
	 * 1-10. 표시기에 박스 매핑이 안 된 에러
	 * 
	 * @param batch
	 * @param gwPath
	 * @param indCd
	 */
	public void displayForNoBoxError(JobBatch batch, String gwPath, String indCd);
	
	/**
	 * 1-10. 표시기에 박스 매핑이 안 된 에러
	 * 
	 * @param batch
	 * @param indCd
	 */
	public void displayForNoBoxError(JobBatch batch, String indCd);
	
	/**
	 * 1-11. 표시기에 문자열 표시
	 * 
	 * @param domainId
	 * @param batchId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param showStr
	 */
	public void displayForString(Long domainId, String batchId, String stageCd, String jobType, String gwPath, String indCd, String showStr);
	
	/**
	 * 1-11. 표시기에 문자열 표시
	 * 
	 * @param domainId
	 * @param batchId
	 * @param stageCd
	 * @param jobType
	 * @param indCd
	 * @param showStr
	 */
	public void displayForString(Long domainId, String batchId, String stageCd, String jobType, String indCd, String showStr);
	
	/**
	 * 1-12. 표시기에 셀 코드 표시
	 * 
	 * @param domainId
	 * @param batchId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param cellCd
	 */
	public void displayForCellCd(Long domainId, String batchId, String stageCd, String jobType, String gwPath, String indCd, String cellCd);
	
	/**
	 * 1-12. 표시기에 셀 코드 표시
	 * 
	 * @param domainId
	 * @param batchId
	 * @param stageCd
	 * @param jobType
	 * @param indCd
	 * @param cellCd
	 */
	public void displayForCellCd(Long domainId, String batchId, String stageCd, String jobType, String indCd, String cellCd);
	
	/**
	 * 1-13. 표시기에 표시기 코드 표시
	 * 
	 * @param domainId
	 * @param batchId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param cellCd
	 */
	public void displayForIndCd(Long domainId, String batchId, String stageCd, String jobType, String gwPath, String indCd, String cellCd);
	
	/**
	 * 1-13. 표시기에 표시기 코드 표시
	 * 
	 * @param domainId
	 * @param batchId
	 * @param stageCd
	 * @param indCd
	 * @param cellCd
	 */
	public void displayForIndCd(Long domainId, String batchId, String stageCd, String jobType, String indCd, String cellCd);
	
	/**
	 * 1-14. 투입 이벤트로 표시기 점등
	 * 
	 * @param batch
	 * @param input
	 * @param jobList
	 */
	public void indicatorsOnByInput(JobBatch batch, JobInput input, List<JobInstance> jobList);
	
	/**
	 * 1-15. 설비별 작업 리스트 조회 후 표시기 재점등
	 * 
	 * @param batch
	 * @param equipZone
	 */
	public void restoreIndicatorsOn(JobBatch batch, String equipZone);
	
	/**
	 * 1-16. 작업 배치별, 게이트웨이 별 작업 리스트 조회 후 표시기 재점등
	 * 
	 * @param batch
	 */
	public void restoreIndicatorsOn(JobBatch batch);
	
	/**
	 * 1-17. 표시기 : 작업 배치, 투입 시퀀스, 장비 존 별 작업 리스트 조회 후 모드에 따라서 표시기 재점등
	 * 
	 * @param batch 작업 배치
	 * @param inputSeq 투입 순서
	 * @param equipZone 장비 존 코드
	 * @param mode todo : 작업 처리 할 표시기, done : 이미 처리한 표시기 
	 */
	public void restoreIndicatorsOn(JobBatch batch, int inputSeq, String equipZone, String mode);
	
	/**
	 * 1-18. 표시기 : 이전 표시기 버튼 색상으로 다음 표시기 색상을 결정
	 * 
	 * @param job
	 * @param prevColor
	 * @return
	 */
	public String nextIndicatorColor(JobInstance job, String prevColor);
	
	/**
	 * 1-19. 표시기 : 작업의 이전 표시기 색상을 조회 
	 * 
	 * @param job
	 * @return
	 */
	public String prevIndicatorColor(JobInstance job);
	
	/**
	 * 표시기 교체
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param fromIndCd
	 * @param toIndCd
	 */
	public void changeIndicator(Long domainId, String stageCd, String gwPath, String fromIndCd, String toIndCd);
}
