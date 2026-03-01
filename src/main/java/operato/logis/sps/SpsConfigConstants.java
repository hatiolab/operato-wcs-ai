package operato.logis.sps;

import xyz.anythings.base.LogisConfigConstants;

/**
 * 단포 관련 설정 관련 상수
 * 
 * @author shortstop
 */
public class SpsConfigConstants extends LogisConfigConstants {
	
	/**********************************************************************
	 * 								SPS 설정
	 **********************************************************************/
	/**
	 * SPS 설정 - SPS 셀에 할당할 대상 필드 (매장, 상품, 주문번호…)
	 */
	public static final String SPS_PREPROCESS_CELL_MAPPING_FIELD = "sps.preproces.cell.mapping.field";
	/**
	 * SPS 설정 - 방면분류를 위한 주문 매핑 필드 (주문의 box_class_cd에 복사할 필드명)
	 */
	public static final String SPS_BOX_OUT_CLASS_FIELD = "sps.box.out.class.field";
	/**
	 * SPS 설정 - 투입 박스 유형 (box / tray)
	 */
	public static final String SPS_INPUT_BOX_TYPE = "sps.input.box.type";
	/**
	 * SPS 설정 - 박스 투입시 박스 타입 split 기준 (1,2)
	 */
	public static final String SPS_INPUT_BOX_TYPE_SPLIT_INDEX = "sps.input.box.type.split.index";
	/**
	 * SPS 설정 - 피킹과 동시에 검수 처리할 것인지 여부
	 */
	public static final String SPS_PICK_WITH_INSPECTION_ENABLED = "sps.pick.with-inspection.enabled";

	/**
	 * SPS 설정 - 작업 모드 (picking / manual)
	 */
	public static final String SPS_WORK_MODE = "sps.work.mode";
}
