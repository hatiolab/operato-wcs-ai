package operato.logis.ops.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * 오더 피킹 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class OpsQueryStore extends AbstractQueryStore {

	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/ops/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/ops/query/ansi/";
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
	 * 현재 배치 최종 서머리를 위한 쿼리
	 * 
	 * @return
	 */
	public String getBatchSummaryByClosingQuery() {
		return this.getQueryByPath("batch/BatchSummaryByClosing");
	}

}
