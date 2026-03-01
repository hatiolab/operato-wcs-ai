package operato.logis.das.service.api;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.service.api.IIndicationService;

/**
 * DAS 표시기 서비스 인터페이스
 * 
 * @author shortstop
 */
public interface IDasIndicationService extends IIndicationService {
	
	/**
	 * 작업 배치에 상품이 할당된 모든 셀에 박스 매핑 표시 
	 * 
	 * @param batch
	 */
	public void displayAllForBoxMapping(JobBatch batch);
	
	/**
	 * 작업 배치가 실행 중인 랙의 모든 셀에 자기 자신의 셀 코드 표시
	 * 
	 * @param batch
	 */
	public void displayAllForCellCode(JobBatch batch);
}
