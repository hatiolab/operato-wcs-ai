package operato.logis.pdas.service.util;

import operato.logis.pdas.PdasConfigConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.service.util.BatchJobConfigUtil;

/**
 * P-DAS 관련 작업 배치 관련 설정 프로파일
 * 
 * 작업 설정 프로파일 컨셉
 * 
 * DPS 작업 설정 항목
 * 
 * @author shortstop
 */
public class PdasBatchJobConfigUtil extends BatchJobConfigUtil {

	/**
	 * 소분류를 위해 박스에 할당할 대상 필드 (주문의 class_cd에 복사할 필드명 - 매장, 상품, 주문번호 …)
	 * 
	 * @param batch
	 * @return
	 */
	public static String getBoxMappingTargetField(JobBatch batch) {
		// pdas.box.mapping.field
		return getConfigValue(batch, PdasConfigConstants.PDAS_BOX_MAPPING_FIELD, true);
	}
	
	/**
	 * 방면분류를 위한 주문 매핑 필드 (주문의 box_class_cd에 복사할 필드명)
	 * 
	 * @param batch
	 * @return
	 */
	public static String getBoxOutClassTargetField(JobBatch batch) {
		// pdas.box.out.class.field
		return getBoxOutClassTargetField(batch, true);
	}
	
	/**
	 * 방면분류를 위한 주문 매핑 필드 (주문의 box_class_cd에 복사할 필드명)
	 * 
	 * @param batch
	 * @param exceptionWhenEmptyValue
	 * @return
	 */
	public static String getBoxOutClassTargetField(JobBatch batch, boolean exceptionWhenEmptyValue) {
		// pdas.box.out.class.field
		return getConfigValue(batch, PdasConfigConstants.PDAS_BOX_OUT_CLASS_FIELD, exceptionWhenEmptyValue);
	}

}
