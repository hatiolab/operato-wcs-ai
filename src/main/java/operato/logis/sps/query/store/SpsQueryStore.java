package operato.logis.sps.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * 단포 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class SpsQueryStore extends AbstractQueryStore {

	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/sps/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/sps/query/ansi/";
	}
	
	/**
	 * 현재 배치 실적 조회
	 * 
	 * @return
	 */
	public String getBatchProgressRateQuery() {
		return this.getQueryByPath("batch/BatchProgressRate");
	}

	/**
	 * 현재 배치 서머리 조회
	 * 
	 * @return
	 */
	public String getBatchResultSummaryQuery() {
		return this.getQueryByPath("batch/BatchResultSummary");
	}
	
	/**
	 * 대상 분류 쿼리
	 * 
	 * @return
	 */
	public String getClassifyOrdersQuery() {
		return this.getQueryByPath("batch/ClassifyOrders");
	}
	
	
	/**
	 * 작업 지시 시점에 주문 업데이트
	 * 
	 * @return
	 */
	public String getUpdateOrderByInstructQuery() {
		return this.getQueryByPath("batch/UpdateOrdersByInstruct");
	}
	
	/**
	 * 작업 병합 시점에 주문 업데이트
	 * 
	 * @return
	 */
	public String getUpdateOrderByMergeQuery() {
		return this.getQueryByPath("batch/UpdateOrdersByMerge");
	}
	
	/**
	 * 단포 작업 정보 생성
	 * 
	 * @return
	 */
	public String getCreateJobInstancesQuery() {
		return this.getQueryByPath("batch/CreateJobInstances");
	}
	
	/**
	 * 작업 지시 배치 주문 수 업데이트 쿼리 조회
	 *
	 * @return
	 */
	public String getUpdateOrderCountQuery() {
		return this.getQueryByPath("batch/UpdateBatchOrderCount");
	}
	
	/**
	 * 다음 맵핑할 작업 정보 조회
	 * 
	 * @return
	 */
	public String getFindNextMappingJobQuery() {
		return this.getQueryByPath("pick/FindNextMappingJob");
	}
	
	/**
	 * 피킹 작업 리스트를 조회
	 * 
	 * @return
	 */
	public String getSearchPickingJobListQuery() {
		return this.getQueryByPath("pick/SearchPickingJobList");
	}
	
	/**
	 * 단포 작업 화면 서머리 정보 조회 쿼리
	 * 
	 * @return
	 */
	public String getSearchSkuJobSummaryQuery() {
		return this.getQueryByPath("pick/SearchSkuJobSummary");
	}

	/**
	 * 박스 ID 유니크 여부를 확인 하는 쿼리
	 * 
	 * @return
	 */
	public String getBoxIdUniqueCheckQuery() {
		return this.getQueryByPath("pick/BoxIdUniqueCheck");
	}

}
