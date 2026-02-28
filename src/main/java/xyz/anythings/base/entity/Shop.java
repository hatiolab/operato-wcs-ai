package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "shops", idStrategy = GenerationRule.UUID, uniqueFields="domainId,comCd,shopCd", indexes = {
	@Index(name = "ix_shops_0", columnList = "domain_id,com_cd,shop_cd", unique = true)
})
public class Shop extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 437295193065389036L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "shop_cd", nullable = false, length = 30)
	private String shopCd;

	@Column (name = "shop_nm", length = 100)
	private String shopNm;
	
	@Column (name = "shop_desc", length = 100)
	private String shopDesc;

	@Column (name = "shop_type", length = 20)
	private String shopType;

	@Column (name = "shop_tel_no", length = 40)
	private String shopTelNo;
	
	@Column (name = "shop_fax_no", length = 40)
	private String shopFaxNo;

	@Column (name = "shop_zip_cd", length = 30)
	private String shopZipCd;

	@Column (name = "shop_addr", length = 400)
	private String shopAddr;

	@Column (name = "biz_lic_no", length = 40)
	private String bizLicNo;

	@Column (name = "rep_per_nm", length = 40)
	private String repPerNm;

	@Column (name = "biz_con_nm", length = 40)
	private String bizConNm;

	@Column (name = "biz_item_nm", length = 40)
	private String bizItemNm;

	@Column (name = "region_cd", length = 30)
	private String regionCd;

	@Column (name = "region_nm", length = 40)
	private String regionNm;

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

	public String getShopDesc() {
		return shopDesc;
	}

	public void setShopDesc(String shopDesc) {
		this.shopDesc = shopDesc;
	}

	public String getShopType() {
		return shopType;
	}

	public void setShopType(String shopType) {
		this.shopType = shopType;
	}

	public String getShopTelNo() {
		return shopTelNo;
	}

	public void setShopTelNo(String shopTelNo) {
		this.shopTelNo = shopTelNo;
	}

	public String getShopFaxNo() {
		return shopFaxNo;
	}

	public void setShopFaxNo(String shopFaxNo) {
		this.shopFaxNo = shopFaxNo;
	}

	public String getShopZipCd() {
		return shopZipCd;
	}

	public void setShopZipCd(String shopZipCd) {
		this.shopZipCd = shopZipCd;
	}

	public String getShopAddr() {
		return shopAddr;
	}

	public void setShopAddr(String shopAddr) {
		this.shopAddr = shopAddr;
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

	public String getRegionCd() {
		return regionCd;
	}

	public void setRegionCd(String regionCd) {
		this.regionCd = regionCd;
	}

	public String getRegionNm() {
		return regionNm;
	}

	public void setRegionNm(String regionNm) {
		this.regionNm = regionNm;
	}

	public Boolean getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(Boolean delFlag) {
		this.delFlag = delFlag;
	}	
}
