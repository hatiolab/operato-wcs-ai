package operato.logis.dpc.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * DPS 피킹 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class DpcPickQueryStore extends AbstractQueryStore {
	
	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/dpc/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/dpc/query/ansi/"; 
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
	 * 셀 별 작업 리스트를 조회
	 * 
	 * @return
	 */
	public String getSearchCellBoxListQuery() {
		return this.getQueryByPath("pick/CellBoxPickList");
	}
	
	/**
	 * 표시기 END, ENDED 재점등을 위한 쿼리 
	 * 
	 * @return
	 */
	public String getRestoreEndIndicators() {
		return this.getQueryByPath("pick/RestoreEndIndicators");
	}
	
	/**
	 * 투입한 상품 코드로 투입 시퀀스를 조회
	 * 
	 * @return
	 */
	public String getFindInputSeqBySkuQuery() {
		return this.getQueryByPath("pick/InputSeqBySku");
	}

	/**
	 * 표시기 검수 작업 현황 조회
	 * 
	 * @return
	 */
	public String getSearchInspectionJobListQuery() {
		return this.getQueryByPath("pick/InspectionJobListByIndicator");
	}

}
