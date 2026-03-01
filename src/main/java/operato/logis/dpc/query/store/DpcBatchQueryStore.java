package operato.logis.dpc.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * DPC 배치 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class DpcBatchQueryStore extends AbstractQueryStore {

	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/dpc/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/dpc/query/ansi/";
	}
	
	/**
	 * 작업 배치의 작업 진행율 조회
	 *
	 * @return
	 */
	public String getBatchProgressRateQuery() {
		return this.getQueryByPath("batch/BatchProgressRate");
	}
	
	/**
	 * 작업 마감을 위한 작업 데이터 요약 정보 조회
	 *
	 * @return
	 */
	public String getBatchResultSummaryQuery() {
		return this.getQueryByPath("batch/BatchResultSummary");
	}
	
	/**
	 * 대상 분류를 위한 쿼리 조회
	 *
	 * @return
	 */
	public String getClassifyMultiOrders() {
		return this.getQueryByPath("batch/ClassifyOrders");
	}
	
	/**
	 * 작업 지시 주문 정보 업데이트 쿼리 조회
	 *
	 * @return
	 */
	public String getOrderStatusByInstruct() {
		return this.getQueryByPath("batch/UpdateOrderStatusByInstruct");
	}
	
	/**
	 * 작업 지시 작업 정보 생성 쿼리 조회
	 *
	 * @return
	 */
	public String getGenerateJobsByInstructionQuery() {
		return this.getQueryByPath("batch/GenerateJobs");
	}
	
	/**
	 * 작업 지시 배치 주문 수 업데이트 쿼리 조회
	 *
	 * @return
	 */
	public String getUpdateOrderCount() {
		return this.getQueryByPath("batch/UpdateBatchOrderCount");
	}
}
