package operato.logis.pdas.service.util;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.AnyConstants;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * PDAS 서비스 유틸리티 클래스
 * 
 * @author shortstop
 */
public class PdasServiceUtil extends LogisServiceUtil {
	
	/**
	 * PDAS 유형의 작업 배치 체크
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public static JobBatch checkPdasRunningBatch(Long domainId, String equipType, String equipCd) {
		Rack rack = checkValidRack(domainId, equipCd);
		JobBatch batch = findBatch(domainId, rack.getBatchId(), false, true);
		
		if(ValueUtil.isNotEqual("PDAS", batch.getJobType())) {
			// 작업 유형은 지원하지 않습니다.
			throw ThrowUtil.newValidationErrorWithNoLog(true, "NOT_SUPPORTED_A", "terms.label.job_type");
		}
		
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_RUNNING)) {
			// 진행 중인 배치가 아닙니다.
			throw ThrowUtil.newValidationErrorWithNoLog(true, SysMessageConstants.DOES_NOT_PROCEED, "terms.label.job_batch");
		}
		
		return batch;
	}
	
	/**
	 * 작업 스테이션이 전체인지 아니면 선택된 스테이션인지 체크
	 * 
	 * @param stationCd
	 * @return
	 */
	public static boolean isValidStationCode(String stationCd) {
		return ValueUtil.isNotEmpty(stationCd) && !ValueUtil.isEqualIgnoreCase(stationCd, AnyConstants.ALL_CAP_STRING);
	}
	
}