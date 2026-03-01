package operato.logis.dps.model;

/**
 * B2C 배치 투입 가능 박스 타입 리스트 모델
 * 
 * @author yang
 */
public class DpsBatchInputableBox {
	
	/**
	 * 랙 호기
	 */
	private String rackCd;
	
	/**
	 * 박스 타입 코드
	 */
	private String boxTypeCd;
	
	/**
	 * 박스 타입 명
	 */
	private String boxTypeNm;
	
	/**
	 * 박스 투입 예정 수량
	 */
	private Integer inputPlanQty;
	
	/**
	 * 투입 박스 수량
	 */
	private Integer inputBoxQty;
	
	/**
	 * 투입 박스 수량
	 */
	private Integer inputableBoxQty;
	
	/**
	 * 투입해야 할 남은 수량
	 */
	private Integer remainQty;
	
	public String getRackCd() {
		return this.rackCd;
	}
	
	public void setRackCd(String rackCd) {
		this.rackCd = rackCd;
	}

	public String getBoxTypeCd() {
		return boxTypeCd;
	}

	public void setBoxTypeCd(String boxTypeCd) {
		this.boxTypeCd = boxTypeCd;
	}

	public String getBoxTypeNm() {
		return boxTypeNm;
	}

	public void setBoxTypeNm(String boxTypeNm) {
		this.boxTypeNm = boxTypeNm;
	}

	public Integer getInputPlanQty() {
		return inputPlanQty;
	}

	public void setInputPlanQty(Integer inputPlanQty) {
		this.inputPlanQty = inputPlanQty;
	}

	public Integer getInputBoxQty() {
		return inputBoxQty;
	}

	public void setInputBoxQty(Integer inputBoxQty) {
		this.inputBoxQty = inputBoxQty;
	}

	public Integer getInputableBoxQty() {
		return inputableBoxQty;
	}

	public void setInputableBoxQty(Integer inputableBoxQty) {
		this.inputableBoxQty = inputableBoxQty;
	}

	public Integer getRemainQty() {
		return remainQty;
	}

	public void setRemainQty(Integer remainQty) {
		this.remainQty = remainQty;
	}

}