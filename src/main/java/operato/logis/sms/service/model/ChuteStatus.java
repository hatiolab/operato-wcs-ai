package operato.logis.sms.service.model;

public class ChuteStatus {
	/**
	 * 슈트 번호
	 */
	private String chuteNo;
	/**
	 * 거래처 코드
	 */
	private String shopCd;
	/**
	 * 거래처 명
	 */
	private String shopNm;
	/**
	 * 할당된 개수 PCS
	 */
	private Integer assignedPcs;
	
	/**
	 * 기본 생성자 
	 */
	public ChuteStatus() {
		
	}
	
	/**
	 * 생성자
	 * 
	 * @param chuteNo
	 * @param shopCd
	 * @param shopNm
	 * @param assignedPcs
	 * @return
	 */
	public ChuteStatus(String chuteNo, String shopCd, String shopNm, Integer assignedPcs) {
		this.chuteNo = chuteNo;
		this.shopCd = shopCd;
		this.shopNm = shopNm;
		this.assignedPcs = assignedPcs;
	}
	
	public String getChuteNo() {
		return chuteNo;
	}
	public void setChuteNo(String chuteNo) {
		this.chuteNo = chuteNo;
	}
	public String getShopCd() {
		return shopCd;
	}
	public void setShopCd(String shopCd) {
		this.shopCd = shopCd;
	}
	public String getShopNm() {
		return shopNm;
	}
	public void setShopNm(String shopNm) {
		this.shopNm = shopNm;
	}
	public Integer getAssignedPcs() {
		return assignedPcs;
	}
	public void setAssignedPcs(Integer assignedPcs) {
		this.assignedPcs = assignedPcs;
	}
}
