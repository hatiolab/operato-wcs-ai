package xyz.anythings.base.event;

import java.util.List;

/**
 * 중분류 이벤트
 * 
 * @author shortstop
 */
public interface ICategorizeEvent extends IBatchBasedEvent {

	/**
	 * 작업 배치 그룹 리턴 
	 * 
	 * @return
	 */
	public String getBatchGroupId();
	
	/**
	 * 작업 배치 그룹 설정
	 * 
	 * @param batchGroupId
	 */
	public void setBatchGroupId(String batchGroupId);
	
	/**
	 * 배치 ID 리스트
	 * 
	 * @return
	 */
	public List<String> getBatchIdList();
	
	/**
	 * 배치 ID 리스트 설정
	 * 
	 * @param batchIdList
	 */
	public void setBatchIdList(List<String> batchIdList);
	
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
	 * 투입 유형 리턴 - 아래 상수 참조 (상품 낱개 투입, 상품 완박스 투입, 상품 묶음 투입, 박스 투입)
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU, 
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU_BOX, 
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU_BUNDLE, 
	 * LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_BOX
	 * 
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
	 * 분류 모드 리턴 - TOTAL (전체 주문 대상) / NOT-ASSORTED (미분류 주문 대상) 
	 * 
	 * @return
	 */
	public String getAssortMode();
	
	/**
	 * 분류 모드 설정
	 * 
	 * @param mode
	 */
	public void setAssortMode(String mode);

}
