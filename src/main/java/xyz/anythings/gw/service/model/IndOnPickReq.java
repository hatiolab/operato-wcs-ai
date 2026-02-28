package xyz.anythings.gw.service.model;

/**
 * 표시기 점등 요청 모델
 * 
 * @author shortstop
 */
public class IndOnPickReq {

	/**
	 * jobInstanceId
	 */
	private String jobInstanceId;
	/**
	 * 고객사 코드
	 */
	private String comCd;
	/**
	 * 실행 순서
	 */
	private Integer inputSeq;
	/**
	 * 표시기 코드
	 */
	private String indCd;
	/**
	 * 표시기 색상 
	 */
	private String colorCd;
	/**
	 * 피킹 수량  
	 */
	private Integer pickQty;
	/**
	 * 박스 입수 수량 
	 */
	private Integer boxInQty;
	/**
	 * gateway path
	 */
	private String gwPath;
	
	public IndOnPickReq() {
	}
	
	public String getJobInstanceId() {
		return jobInstanceId;
	}
	
	public void setJobInstanceId(String jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}
	
	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public Integer getInputSeq() {
		return inputSeq;
	}

	public void setInputSeq(Integer inputSeq) {
		this.inputSeq = inputSeq;
	}

	public String getIndCd() {
		return indCd;
	}
	
	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}
	
	public String getColorCd() {
		return colorCd;
	}
	
	public void setColorCd(String colorCd) {
		this.colorCd = colorCd;
	}
	
	public Integer getPickQty() {
		return pickQty;
	}
	
	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
	}
	
	public Integer getBoxInQty() {
		return boxInQty;
	}
	
	public void setBoxInQty(Integer boxInQty) {
		this.boxInQty = boxInQty;
	}
	
	public String getGwPath() {
		return gwPath;
	}
	
	public void setGwPath(String gwPath) {
		this.gwPath = gwPath;
	}
	
}
