package xyz.anythings.gw.service.model;

/**
 * 표시기 공통 모델 
 * 
 * @author shortstop
 */
public class IndCommonReq {

	/**
	 * 도메인 ID
	 */
	private Long domainId;
	/**
	 * 스테이지 코드
	 */
	private String stageCd;
	/**
	 * 표시기 코드
	 */
	private String indCd;
	/**
	 * 셀 코드
	 */
	private String cellCd;
	/**
	 * 게이트웨이 Path
	 */
	private String gwPath;
	
	public IndCommonReq() {
	}
	
	public IndCommonReq(Long domainId, String stageCd, String indCd, String cellCd, String gwPath) {
		this.domainId = domainId;
		this.stageCd = stageCd;
		this.indCd = indCd;
		this.cellCd = cellCd;
		this.gwPath = gwPath;
	}
	
	public IndCommonReq(Long domainId, String indCd, String cellCd, String gwPath) {
		this.domainId = domainId;
		this.indCd = indCd;
		this.cellCd = cellCd;
		this.gwPath = gwPath;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getIndCd() {
		return indCd;
	}

	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}

	public String getCellCd() {
		return cellCd;
	}

	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}

	public String getGwPath() {
		return gwPath;
	}

	public void setGwPath(String gwPath) {
		this.gwPath = gwPath;
	}

}
