package operato.logis.pdas.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * PDAS 배치 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class PdasBatchQueryStore extends AbstractQueryStore {

	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/pdas/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/pdas/query/ansi/";
	}
	
	/**
	 * BatchReceiptItem에 itemType이 Order인 케이스에 대한 BatchReceipt 조회
	 *  
	 * @return
	 */
	public String getBatchReceiptOrderTypeStatusQuery() {
		return this.getQueryByPath("batch/BatchReceiptOrderTypeStatus");
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
	public String getPdasBatchResultSummaryQuery() {
		return this.getQueryByPath("batch/BatchResultSummary");
	}
	
	/**
	 * 작업 생성 쿼리 조회
	 *
	 * @return
	 */
	public String getPdasGenerateInstances() {
		return this.getQueryByPath("batch/CreateJobInstances");
	}
	
	/**
	 * 작업 배치 주문 수 업데이트 쿼리 조회
	 *
	 * @return
	 */
	public String getPdasUpdateOrderCount() {
		return this.getQueryByPath("batch/UpdateBatchOrderCount");
	}
}
