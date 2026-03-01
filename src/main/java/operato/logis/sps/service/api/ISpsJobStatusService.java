package operato.logis.sps.service.api;

import java.util.List;
import java.util.Map;

import operato.logis.sps.model.SpsSkuSummary;
import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.elidom.dbist.dml.Page;

/**
 * 단포 작업 현황 조회 서비스
 * 
 * @author shortstop
 */
public interface ISpsJobStatusService {
	
	/**
	 * 작업 배치 내에 다음 배치 시퀀스를 조회
	 * 
	 * @param batch
	 * @return
	 */
	public Integer findNextInputSeq(JobBatch batch);
	
	/**
	 * 작업 배치 내에 피킹 중인 작업 리스트를 조회
	 * 
	 * @param batch 작업 배치
	 * @param stationCd 작업 스테이션
	 * @param classCd 소분류 코드 (일반적으로 B2C인 경우 주문 번호, B2B 출고인 경우 매장 코드, 반품인 경우 상품 코드
	 * @return
	 */
	public List<JobInstance> searchPickingJobList(JobBatch batch, String stationCd, String classCd);
	
	/**
	 * 작업 배치 내에 모든 조회 조건으로 작업 리스트를 조회
	 * 
	 * @param batch 작업 배치
	 * @param condition 조회 조건
	 * @return
	 */
	public List<JobInstance> searchPickingJobList(JobBatch batch, Map<String, Object> condition);
	
	/**
	 * 피킹 처리를 위한 작업 인스턴스 ID로 작업 데이터 조회
	 * 
	 * @param domainId 도메인 ID
	 * @param jobInstanceId 작업 인스턴스 ID
	 * @return
	 */
	public JobInstance findPickingJob(Long domainId, String jobInstanceId);
	
	/**
	 * 작업조회 조건으로 작업 리스트 조회
	 * 
	 * @param batch
	 * @param condition
	 * @return
	 */
	public List<JobInstance> searchJobList(JobBatch batch, Map<String, Object> condition);
	
	/**
	 * 조회 조건으로 박스 페이지네이션 조회
	 * 
	 * @param batch
	 * @param condition
	 * @param page
	 * @param limit
	 * @return
	 */
	public Page<BoxPack> paginateBoxList(JobBatch batch, Map<String, Object> condition, int page, int limit);
	
	/**
	 * 조회 조건으로 박스 리스트 조회
	 * 
	 * @param batch
	 * @param condition
	 * @return
	 */
	public List<BoxPack> searchBoxList(JobBatch batch, Map<String, Object> condition);
	
	/**
	 * 박스 ID로 박스 내품 내역 조회
	 * 
	 * @param domainId
	 * @param boxPackId
	 * @return
	 */
	public List<BoxItem> searchBoxItems(Long domainId, String boxPackId);

	/**
	 * 상품별 단포 작업을 위한 서머리 조회
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	public List<SpsSkuSummary> searchSkuJobSummary(JobBatch batch, String comCd, String skuCd);

}
