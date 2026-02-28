package xyz.anythings.base.query.store;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * Logis Base 모듈의 쿼리 스토어 기본 구현
 * 
 * @author shortstop
 */
public class LogisBaseQueryStore extends AbstractQueryStore {
	
	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "xyz/anythings/base/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "xyz/anythings/base/query/ansi/"; 
	}

}
