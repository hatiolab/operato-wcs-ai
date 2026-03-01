package operato.logis.pdas;

import xyz.anythings.base.LogisConfigConstants;

/**
 * PDAS 용 설정 관련 상수
 * 
 * @author shortstop
 */
public class PdasConfigConstants extends LogisConfigConstants {

	/**********************************************************************
	 * 								PDAS 설정
	 **********************************************************************/
	/**
	 * PDAS 설정 - PDAS 박스별 할당할 대상 필드 (매장, 상품, 주문번호…)
	 */
	public static final String PDAS_BOX_MAPPING_FIELD = "pdas.box.mapping.field";
	/**
	 * PDAS 설정 - 방면 분류를 위한 주문 매핑 필드 (주문의 box_class_cd에 복사할 필드명)
	 */
	public static final String PDAS_BOX_OUT_CLASS_FIELD = "pdas.box.out.class.field";

}
