package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "order_preprocesses", idStrategy = GenerationRule.UUID, uniqueFields="batchId,cellAssgnCd,equipType,equipCd", indexes = {
	@Index(name = "ix_order_preprocesses_0", columnList = "batch_id,cell_assgn_cd,equip_type,equip_cd", unique = true)
})
public class OrderPreprocess extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 843616275544182289L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", nullable = false, length = 40)
	private String batchId;

	@Column (name = "job_type", length = 20)
	private String jobType;

	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "cell_assgn_type", nullable = false, length = 20)
	private String cellAssgnType;

	@Column (name = "cell_assgn_cd", nullable = false, length = 30)
	private String cellAssgnCd;

	@Column (name = "cell_assgn_nm", length = 200)
	private String cellAssgnNm;

	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;
	
	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "equip_nm", length = 40)
	private String equipNm;

	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;

	@Column (name = "class_cd", length = 30)
	private String classCd;

	@Column (name = "sku_qty", nullable = false, length = 12)
	private Integer skuQty;

	@Column (name = "total_pcs", nullable = false, length = 12)
	private Integer totalPcs;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getCellAssgnType() {
		return cellAssgnType;
	}

	public void setCellAssgnType(String cellAssgnType) {
		this.cellAssgnType = cellAssgnType;
	}

	public String getCellAssgnCd() {
		return cellAssgnCd;
	}

	public void setCellAssgnCd(String cellAssgnCd) {
		this.cellAssgnCd = cellAssgnCd;
	}

	public String getCellAssgnNm() {
		return cellAssgnNm;
	}

	public void setCellAssgnNm(String cellAssgnNm) {
		this.cellAssgnNm = cellAssgnNm;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getEquipNm() {
		return equipNm;
	}

	public void setEquipNm(String equipNm) {
		this.equipNm = equipNm;
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

	public Integer getSkuQty() {
		return skuQty;
	}

	public void setSkuQty(Integer skuQty) {
		this.skuQty = skuQty;
	}

	public Integer getTotalPcs() {
		return totalPcs;
	}

	public void setTotalPcs(Integer totalPcs) {
		this.totalPcs = totalPcs;
	}
}
