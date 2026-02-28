package xyz.anythings.base.query.store;

import org.springframework.stereotype.Component;

/**
 * 작업 배치 관련 쿼리 스토어 
 * - 주문 수신, 배치 작업 현황, 배치 마감 관련 쿼리 관리
 * 
 * @author shortstop
 */
@Component
public class BatchQueryStore extends LogisBaseQueryStore {
	
	/**
	 * 창고 전체의 최근 3개월 작업 진행율 쿼리
	 * 
	 * @return
	 */
	public String getLatestMonthlyRateQuery() {
		return this.getQueryByPath("batch/LatestMonthlyRate");
	}
	
	/**
	 * 스테이지 별 일일 작업 진행율 쿼리
	 * 
	 * @return
	 */
	public String getDailyProgressRateQuery() {
		return this.getQueryByPath("batch/DailyPrograssRate");
	}
	
	/**
	 * 작업 배치 그룹 범위 안에 있는 작업 데이터 중에서 넘어온 배치 정보와 상품 정보로 부터 SKU 정보를 조회
	 * 
	 * @return
	 */
	public String getSearchSkuInBatchGroupQuery() {
		return this.getQueryByPath("batch/SearchSkuInBatchGroup");
	}
	
	/**
	 * 작업 배치 범위 안에 있는 작업 데이터 중에서 넘어온 배치 정보와 상품 정보로 부터 SKU 정보를 조회
	 * 
	 * @return
	 */
	public String getSearchSkuInBatchQuery() {
		return this.getQueryByPath("batch/SearchSkuInBatch");
	}
	
	/**
	 * 작업 배치 그룹 내에 진행 또는 진행 예정인 배치 범위 내에서 상품 정보로 부터 SKU 정보를 조회 (반품용)
	 * 
	 * @return
	 */
	public String getSearchSkuInBatchGroupForReturnQuery() {
		return this.getQueryByPath("batch/SearchSkuInBatchGroupForReturn");
	}
	
	/**
	 * 배치 그룹내 배치에 대해서 끝나지 않은 정보가 있는지 체크
	 * 
	 * @return
	 */
	public String getCheckCloseBatchGroupQuery() {
		return this.getQueryByPath("batch/CheckCloseBatchGroup");
	}
	
	/**
	 * 기본 배치 진행율 쿼리
	 * 
	 * @return
	 */
	public String getBatchProgressRateQuery() {
		return this.getQueryByPath("batch/BatchProgressRate");
	}
	
	/*** BatchReceipt 관련 데이터 쿼리 ***/
	/**
	 * WMS I/F 테이블로 부터 BatchReceipt 데이터를 조회 한다.
	 * 
	 * @return
	 */
	public String getWmsIfToReceiptDataQuery() {
		return this.getQueryByPath("batch/WmsIfToReceiptData");
	}
	
	/**
	 * BatchReceipt 조회
	 * 
	 * @return
	 */
	public String getBatchReceiptOrderTypeStatusQuery() {
		return this.getQueryByPath("batch/BatchReceiptOrderTypeStatus");
	}
	
	/**
	 * 마지막 투입 작업 조회
	 * 
	 * @return
	 */
	public String getLatestJobInputQuery() {
		return this.getQueryByPath("batch/LastestJobInput");
	}

}
