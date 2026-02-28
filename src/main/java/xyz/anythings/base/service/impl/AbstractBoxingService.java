package xyz.anythings.base.service.impl;

import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.service.api.IBoxingService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;

/**
 * 박스 처리 서비스 기본 구현
 * 
 * @author yang
 */
public abstract class AbstractBoxingService extends AbstractLogisService implements IBoxingService {

	/**
	 * 1-2. 분류 모듈 정보 : 작업 배치별 작업 설정 정보
	 * 
	 * @param batchId
	 * @return
	 */
	@Override
	public JobConfigSet getJobConfigSet(String batchId) {
		return BatchJobConfigUtil.getConfigSetService().getConfigSet(batchId);
	}

}
