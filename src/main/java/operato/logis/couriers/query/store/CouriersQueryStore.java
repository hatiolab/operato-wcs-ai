package operato.logis.couriers.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * 택배 관련 모듈 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class CouriersQueryStore extends AbstractQueryStore {

	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/couriers/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/couriers/query/ansi/";
	}

}
