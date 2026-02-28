package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "companies", idStrategy = GenerationRule.UUID, uniqueFields="domainId,comCd", indexes = {
	@Index(name = "ix_companies_0", columnList = "domain_id,com_cd", unique = true)
})
public class Company extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 198201659531459117L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "com_nm", nullable = false, length = 40)
	private String comNm;

	@Column (name = "com_tel_no", length = 40)
	private String comTelNo;

	@Column (name = "com_zip_cd", length = 30)
	private String comZipCd;

	@Column (name = "com_addr", length = 400)
	private String comAddr;

	@Column (name = "biz_lic_no", length = 40)
	private String bizLicNo;

	@Column (name = "rep_per_nm", length = 40)
	private String repPerNm;

	@Column (name = "biz_con_nm", length = 40)
	private String bizConNm;

	@Column (name = "biz_item_nm", length = 40)
	private String bizItemNm;

	@Column (name = "del_flag", length = 1)
	private Boolean delFlag;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getComNm() {
		return comNm;
	}

	public void setComNm(String comNm) {
		this.comNm = comNm;
	}

	public String getComTelNo() {
		return comTelNo;
	}

	public void setComTelNo(String comTelNo) {
		this.comTelNo = comTelNo;
	}

	public String getComZipCd() {
		return comZipCd;
	}

	public void setComZipCd(String comZipCd) {
		this.comZipCd = comZipCd;
	}

	public String getComAddr() {
		return comAddr;
	}

	public void setComAddr(String comAddr) {
		this.comAddr = comAddr;
	}

	public String getBizLicNo() {
		return bizLicNo;
	}

	public void setBizLicNo(String bizLicNo) {
		this.bizLicNo = bizLicNo;
	}

	public String getRepPerNm() {
		return repPerNm;
	}

	public void setRepPerNm(String repPerNm) {
		this.repPerNm = repPerNm;
	}

	public String getBizConNm() {
		return bizConNm;
	}

	public void setBizConNm(String bizConNm) {
		this.bizConNm = bizConNm;
	}

	public String getBizItemNm() {
		return bizItemNm;
	}

	public void setBizItemNm(String bizItemNm) {
		this.bizItemNm = bizItemNm;
	}

	public Boolean getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(Boolean delFlag) {
		this.delFlag = delFlag;
	}	
}
