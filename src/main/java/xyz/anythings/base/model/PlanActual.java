package xyz.anythings.base.model;

/**
 * 예정량 / 처리량 / 진행율
 * 
 * @author shortstop
 */
public class PlanActual {

	/**
	 * 예정량
	 */
	private Integer plan;
	/**
	 * 처리량
	 */
	private Integer actual;
	/**
	 * 진행율
	 */
	private Integer rate;
	
	public PlanActual() {
	}
	
	public PlanActual(Integer plan, Integer actual) {
		this.plan = plan;
		this.actual = actual;
	}
	
	public PlanActual(Integer plan, Integer actual, Integer rate) {
		this.plan = plan;
		this.actual = actual;
		this.rate = rate;
	}
	
	public Integer getPlan() {
		return plan;
	}
	
	public void setPlan(Integer plan) {
		this.plan = plan;
	}
	
	public Integer getActual() {
		return actual;
	}
	
	public void setActual(Integer actual) {
		this.actual = actual;
	}
	
	public Integer getRate() {
		return rate;
	}
	
	public void setRate(Integer rate) {
		this.rate = rate;
	}
	
}
