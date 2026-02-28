package xyz.anythings.base.event;

/**
 * 소분류 작업을 위한 설비 투입 최상위 이벤트
 * 
 * @author shortstop
 */
public interface IClassifyInEvent extends IClassifyEvent {
	
	/**
	 * 검수를 위한 투입 여부
	 * 
	 * @return
	 */
	public boolean isForInspection();
	
	/**
	 * 검수를 위한 투입 설정 
	 * 
	 * @param isForInspection
	 */
	public void setForInspection(boolean isForInspection);
	
	/**
	 * 투입 유형 리턴 - 아래 상수 참조 (상품 낱개 투입, 상품 완박스 투입, 상품 묶음 투입, 박스 투입)
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU, 
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU_BOX, 
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU_BUNDLE, 
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_BOX
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_TRAY
	 * @return
	 */
	public String getInputType();
	
	/**
	 * 투입 유형 설정
	 * 
	 * @param inputType
	 */
	public void setInputType(String inputType);
	
	/**
	 * 투입 코드 리턴 - SKU 코드, SKU_BOX 코드, BOX ID, ...
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU : SKU_CD 
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU_BOX : SKU BOX_BARCD
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU_BUNDLE : BUNDLE Code
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_BOX : BOX_ID
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_TRAY : TRAY_CD
	 * 
	 * @return
	 */
	public String getInputCode();
	
	/**
	 * 투입 코드 설정
	 * 
	 * @param inputCode
	 */
	public void setInputCode(String inputCode);
	
	/**
	 * 고객사 코드
	 * 
	 * @return
	 */
	public String getComCd();
	
	/**
	 * 고객사 코드 설정
	 * 
	 * @param comCd
	 */
	public void setComCd(String comCd);
	
	/**
	 * 투입 수량 리턴
	 * 
	 * @return
	 */
	public int getInputQty();
	
	/**
	 * 투입 수량 설정
	 * 
	 * @param inputQty
	 */
	public void setInputQty(int inputQty);
	
}
