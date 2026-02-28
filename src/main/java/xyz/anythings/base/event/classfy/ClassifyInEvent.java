package xyz.anythings.base.event.classfy;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.IClassifyInEvent;

/**
 * 소분류 투입 (In) 이벤트 구현
 * 
 * @author shortstop
 */
public class ClassifyInEvent extends ClassifyEvent implements IClassifyInEvent {

	/**
	 * 검수 여부
	 */
	protected boolean isForInspection;
	/**
	 * 투입 유형
	 */
	protected String inputType;
	/**
	 * 투입 코드
	 */
	protected String inputCode;
	/**
	 * 투입 수량
	 */
	protected int inputQty;
	/**
	 * 고객사 코드
	 */
	protected String comCd;
	
	/**
	 * 생성자 1
	 * 
	 * @param batch
	 * @param eventStep
	 * @param isForInspection
	 * @param inputType
	 * @param inputCode
	 * @param inputQty
	 */
	public ClassifyInEvent(JobBatch batch, short eventStep, boolean isForInspection, String inputType, String inputCode, int inputQty) {
		super(batch, eventStep);
		
		this.setForInspection(isForInspection);
		this.setInputType(inputType);
		this.setInputCode(inputCode);
		this.setInputQty(inputQty);
	}

	@Override
	public boolean isForInspection() {
		return this.isForInspection;
	}

	@Override
	public void setForInspection(boolean isForInspection) {
		this.isForInspection = isForInspection;
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

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	@Override
	public int getInputQty() {
		return this.inputQty;
	}

	@Override
	public void setInputQty(int inputQty) {
		this.inputQty = inputQty;
	}

}
