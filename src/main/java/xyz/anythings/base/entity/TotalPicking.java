package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "total_pickings", idStrategy = GenerationRule.UUID)
public class TotalPicking extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 525898266760963862L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "picking_id", nullable = false, length = 40)
	private String pickingId;

	@Column (name = "batch_id", nullable = false, length = 40)
	private String batchId;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "job_type", nullable = false, length = 20)
	private String jobType;

	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column (name = "job_seq", nullable = false, length = 12)
	private Integer jobSeq;

	@Column (name = "sku_cd", nullable = false, length = 30)
	private String skuCd;

	@Column (name = "sku_nm", length = 200)
	private String skuNm;

	@Column (name = "pick_qty", nullable = false, length = 12)
	private Integer pickQty;

	@Column (name = "picking_qty", length = 12)
	private Integer pickingQty;

	@Column (name = "picked_qty", length = 12)
	private Integer pickedQty;

	@Column (name = "from_zone_cd", length = 30)
	private String fromZoneCd;

	@Column (name = "from_cell_cd", length = 30)
	private String fromCellCd;

	@Column (name = "to_zone_cd", length = 30)
	private String toZoneCd;

	@Column (name = "to_cell_cd", length = 30)
	private String toCellCd;

	@Column (name = "status", length = 10)
	private String status;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPickingId() {
		return pickingId;
	}

	public void setPickingId(String pickingId) {
		this.pickingId = pickingId;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public Integer getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(Integer jobSeq) {
		this.jobSeq = jobSeq;
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

	public Integer getPickQty() {
		return pickQty;
	}

	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
	}

	public Integer getPickingQty() {
		return pickingQty;
	}

	public void setPickingQty(Integer pickingQty) {
		this.pickingQty = pickingQty;
	}

	public Integer getPickedQty() {
		return pickedQty;
	}

	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

	public String getFromZoneCd() {
		return fromZoneCd;
	}

	public void setFromZoneCd(String fromZoneCd) {
		this.fromZoneCd = fromZoneCd;
	}

	public String getFromCellCd() {
		return fromCellCd;
	}

	public void setFromCellCd(String fromCellCd) {
		this.fromCellCd = fromCellCd;
	}

	public String getToZoneCd() {
		return toZoneCd;
	}

	public void setToZoneCd(String toZoneCd) {
		this.toZoneCd = toZoneCd;
	}

	public String getToCellCd() {
		return toCellCd;
	}

	public void setToCellCd(String toCellCd) {
		this.toCellCd = toCellCd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}	
}
