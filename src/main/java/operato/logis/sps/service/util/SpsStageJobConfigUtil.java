package operato.logis.sps.service.util;

import operato.logis.sps.SpsConfigConstants;
import operato.logis.sps.SpsConstants;
import xyz.anythings.base.service.util.StageJobConfigUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 단포 관련 Stage 공통 설정 프로파일
 * 
 * @author shortstop
 */
public class SpsStageJobConfigUtil extends StageJobConfigUtil {
	
	/**
	 * 단포 투입 박스 유형
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getInputBoxType(String stageCd) {
		// dps.input.box.type
		return getConfigValue(stageCd, SpsConstants.MODULE_DEFAULT_JOB_TYPE, SpsConfigConstants.DPS_INPUT_BOX_TYPE, true);
	}
	
	/**
	 * 단포 단포 대상 분류 여부
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static boolean isSingleSkuNpcsClassEnabled(String stageCd) {
		// dps.pick.1sku.npcs.enabled
		String boolVal = getConfigValue(stageCd, SpsConstants.MODULE_DEFAULT_JOB_TYPE, SpsConfigConstants.DPS_PICK_1SKU_NPCS_ENABLED, true);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 단포 박스에 할당할 대상 필드 (매장, 상품, 주문번호 …)
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getBoxMappingTargetField(String stageCd) {
		// dps.box.mapping.field
		return getConfigValue(stageCd, SpsConstants.MODULE_DEFAULT_JOB_TYPE, SpsConfigConstants.DPS_BOX_MAPPING_FIELD, true);
	}

}
