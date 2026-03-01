package operato.logis.pdas.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * DPS 피킹 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class PdasPickQueryStore extends AbstractQueryStore {
	
	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/pdas/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/dps/query/ansi/"; 
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
	public String getSinglePackSummaryQuery() {
		return this.getQueryByPath("pick/SearchSinglePackSummary");
	}
	
	/**
	 * 상품 스캔시 상품이 포함된 완료되지 않은 주문을 찾기 위한 쿼리
	 * 
	 * @return
	 */
	public String getFindPickingJobQuery() {
		return this.getQueryByPath("pick/FindPickingJob");
	}
	
	/**
	 * 상품 스캔시 피킹할 주문의 상품을 적치할 셀을 찾기 위한 쿼리
	 * 
	 * @return
	 */
	public String getFindLoadCellToPickQuery() {
		return this.getQueryByPath("pick/FindLoadCellToPick");
	}
	
	/**
	 * 주문 매핑 대기 중인 작업을 찾기 위한 쿼리
	 * 
	 * @return
	 */
	public String getFindWaitingPickingJobQuery() {
		return this.getQueryByPath("pick/FindWaitingPickingJob");
	}

	/**
	 * 중분류 정보를 찾기 위한 쿼리
	 * 
	 * @return
	 */
	public String getFindMiddleAssortQuery() {
		return this.getQueryByPath("pick/FindMiddleAssort");
	}
	
	/**
	 * 작업 스테이션에 진행 중인 작업이 있는지 찾기 위한 쿼리
	 * 
	 * @return
	 */
	public String getFindStationPickingJobQuery() {
		return this.getQueryByPath("pick/FindStationPickingJob");
	}
	
	/**
	 * 작업 스테이션에 매핑 중인 작업을 찾기 위한 쿼리
	 * 
	 * @return
	 */
	public String getCheckOtherStationMappingJobQuery() {
		return this.getQueryByPath("pick/CheckOtherStationMappingJob");
	}
	
	/**
	 * 잘 못 스캔한 상품인지 찾기 위한 쿼리
	 * 
	 * @return
	 */
	public String getCheckWrongSkuQuery() {
		return this.getQueryByPath("pick/CheckWrongSku");
	}
	
	/**
	 * 한 주문의 분류가 최종 완료되었는지 쿼리
	 * 
	 * @return
	 */
	public String getCheckOrderFinalEndQuery() {
		return this.getQueryByPath("pick/CheckOrderFinalEnd");
	}
	
	/**
	 * 한 주문의 분류가 최종 완료되었는지 쿼리
	 * 
	 * @return
	 */
	public String getBatchOrderResultQtyQuery() {
		return this.getQueryByPath("pick/FindBatchOrderResultPcs");
	}
	
	/**
	 * 한 주문의 작업 정보를 모두 박싱 완료 처리
	 * 
	 * @return
	 */
	public String getBoxingOrderQuery() {
		return this.getQueryByPath("pick/OrderBoxing");
	}
	
	/**
	 * 작업 배치 내 투입 리스트 조회 쿼리
	 * 
	 * @return
	 */
	public String getBatchJobInputListQuery() {
		return this.getQueryByPath("pick/BatchJobInputList");
	}
	
	/**
	 * 작업 배치 내 투입 상세 리스트 조회 쿼리
	 * 
	 * @return
	 */
	public String getBatchJobInputDetailsQuery() {
		return this.getQueryByPath("pick/BatchJobInputDetails");
	}
	
	/**
	 * 셀 별 피킹 현황 리스트를 조회
	 * 
	 * @return
	 */
	public String getSearchJobStatusByCellQuery() {
		return this.getQueryByPath("pick/SearchJobStatusByCell");
	}
}
