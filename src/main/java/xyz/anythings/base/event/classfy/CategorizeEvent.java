package xyz.anythings.base.event.classfy;

import java.util.List;

import xyz.anythings.base.event.ICategorizeEvent;
import xyz.anythings.base.event.main.BatchRootEvent;

/**
 * 중분류 이벤트 구현
 * 
 * @author shortstop
 */
public class CategorizeEvent extends BatchRootEvent implements ICategorizeEvent {
	/**
	 * 배치 그룹 ID
	 */
	private String batchGroupId;
	/**
	 * 중분류 대상 배치 ID 리스트 
	 */
	private List<String> batchIdList;
	/**
	 * 투입 유형 
	 */
	private String inputType;
	/**
	 * 투입 코드
	 */
	private String inputCode;
	/**
	 * 고객사 코드
	 */
	private String comCd;
	/**
	 * 분류 모드 - TOTAL (전체 주문 대상) / NOT-ASSORTED (미분류 주문 대상)
	 */
	private String assortMode;
	
	/**
	 * 중분류 이벤트 생성자 1
	 * 
	 * @param domainId
	 * @param eventStep
	 * @param stageCd
	 * @param batchGroupId
	 * @param jobType
	 * @param inputType
	 * @param inputCode
	 */
	public CategorizeEvent(long domainId, short eventStep, String stageCd, String batchGroupId, String jobType, String inputType, String inputCode) {
		super(domainId, eventStep);
		
		this.setStageCd(stageCd);
		this.setBatchGroupId(batchGroupId);
		this.setJobType(jobType);
		this.setInputType(inputType);
		this.setInputCode(inputCode);
	}
	
	/**
	 * 중분류 이벤트 생성자 2
	 * 
	 * @param domainId
	 * @param eventStep
	 * @param stageCd
	 * @param batchIdList
	 * @param jobType
	 * @param comCd
	 * @param inputType
	 * @param inputCode
	 */
	public CategorizeEvent(long domainId, short eventStep, String stageCd, List<String> batchIdList, String jobType, String comCd, String inputCode) {
		super(domainId, eventStep);
		
		this.setStageCd(stageCd);
		this.setBatchIdList(batchIdList);
		this.setJobType(jobType);
		this.setComCd(comCd);
		this.setInputCode(inputCode);
	}

	@Override
	public String getBatchGroupId() {
		return this.batchGroupId;
	}

	@Override
	public void setBatchGroupId(String batchGroupId) {
		this.batchGroupId = batchGroupId;
	}

	@Override
	public String getInputType() {
		return this.inputType;
	}

	@Override
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	@Override
	public String getInputCode() {
		return this.inputCode;
	}

	@Override
	public void setInputCode(String inputCode) {
		this.inputCode = inputCode;
	}

	@Override
	public List<String> getBatchIdList() {
		return batchIdList;
	}

	@Override
	public void setBatchIdList(List<String> batchIdList) {
		this.batchIdList = batchIdList;
	}

	@Override
	public String getComCd() {
		return comCd;
	}

	@Override
	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	@Override
	public String getAssortMode() {
		return this.assortMode;
	}

	@Override
	public void setAssortMode(String assortMode) {
		this.assortMode = assortMode;
	}

}
