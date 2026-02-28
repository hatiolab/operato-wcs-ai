package xyz.anythings.base.model;

/**
 * 작업 배치 진행율
 * 
 * @author shortstop
 */
public class BatchProgressRate {
	/**
	 * 주문 예정량
	 */
	private Integer planOrder;
	/**
	 * 주문 처리량
	 */
	private Integer actualOrder;
	/**
	 * 주문 진행율
	 */
	private Float rateOrder;
	/**
	 * SKU 예정량
	 */
	private Integer planSku;
	/**
	 * SKU 처리량
	 */
	private Integer actualSku;
	/**
	 * SKU 진행율
	 */
	private Float rateSku;
	/**
	 * PCS 예정량
	 */
	private Integer planPcs;
	/**
	 * PCS 처리량
	 */
	private Integer actualPcs;
	/**
	 * PCS 진행율
	 */
	private Float ratePcs;
	/**
	 * 시간당 분류 개수 
	 */
	private Float uph;
	
	public Integer getPlanOrder() {
		return planOrder;
	}
	
	public void setPlanOrder(Integer planOrder) {
		this.planOrder = planOrder;
	}
	
	public Integer getActualOrder() {
		return actualOrder;
	}
	
	public void setActualOrder(Integer actualOrder) {
		this.actualOrder = actualOrder;
	}
	
	public Float getRateOrder() {
		return rateOrder;
	}
	
	public void setRateOrder(Float rateOrder) {
		this.rateOrder = rateOrder;
	}
	
	public Integer getPlanSku() {
		return planSku;
	}
	
	public void setPlanSku(Integer planSku) {
		this.planSku = planSku;
	}
	
	public Integer getActualSku() {
		return actualSku;
	}
	
	public void setActualSku(Integer actualSku) {
		this.actualSku = actualSku;
	}
	
	public Float getRateSku() {
		return rateSku;
	}
	
	public void setRateSku(Float rateSku) {
		this.rateSku = rateSku;
	}
	
	public Integer getPlanPcs() {
		return planPcs;
	}
	
	public void setPlanPcs(Integer planPcs) {
		this.planPcs = planPcs;
	}
	
	public Integer getActualPcs() {
		return actualPcs;
	}
	
	public void setActualPcs(Integer actualPcs) {
		this.actualPcs = actualPcs;
	}
	
	public Float getRatePcs() {
		return ratePcs;
	}
	
	public void setRatePcs(Float ratePcs) {
		this.ratePcs = ratePcs;
	}

	public Float getUph() {
		return uph;
	}

	public void setUph(Float uph) {
		this.uph = uph;
	}

}
