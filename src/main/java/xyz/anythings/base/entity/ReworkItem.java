package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "rework_items", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_rework_items_0", columnList = "rework_id")
})
public class ReworkItem extends xyz.elidom.orm.entity.basic.DomainStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 786931601943930175L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "rework_id", length = 40)
	private String reworkId;

	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;
	
	@Column (name = "class_cd", length = 30)
	private String classCd;

	@Column (name = "sku_cd", length = 30)
	private String skuCd;

	@Column (name = "sku_nm", length = 200)
	private String skuNm;

	@Column (name = "rework_qty", length = 12)
	private Integer reworkQty;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReworkId() {
		return reworkId;
	}

	public void setReworkId(String reworkId) {
		this.reworkId = reworkId;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getSubEquipCd() {
		return subEquipCd;
	}

	public void setSubEquipCd(String subEquipCd) {
		this.subEquipCd = subEquipCd;
	}

	public String getClassCd() {
		return classCd;
	}

	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public Integer getReworkQty() {
		return reworkQty;
	}

	public void setReworkQty(Integer reworkQty) {
		this.reworkQty = reworkQty;
	}
}
