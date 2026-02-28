package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "work_cells", idStrategy = GenerationRule.UUID, uniqueFields="domainId,cellCd", indexes = {
	@Index(name = "ix_work_cells_0", columnList = "domain_id,cell_cd", unique = true)
})
public class WorkCell extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 166768251495546479L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", nullable = false, length = 40)
	private String batchId;

	@Column (name = "cell_cd", nullable = false, length = 30)
	private String cellCd;
	
	@Column (name = "ind_cd", length = 30)
	private String indCd;

	@Column (name = "job_type", length = 20)
	private String jobType;

	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "class_cd", length = 30)
	private String classCd;

	@Column (name = "box_id", length = 40)
	private String boxId;

	@Column (name = "last_job_cd", length = 30)
	private String lastJobCd;

	@Column (name = "last_picked_qty", length = 12)
	private Integer lastPickedQty;

	@Column (name = "job_instance_id", length = 40)
	private String jobInstanceId;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "active_flag", length = 1)
	private Boolean activeFlag;
  
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

	public String getCellCd() {
		return cellCd;
	}

	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}

	public String getIndCd() {
		return indCd;
	}

	public void setIndCd(String indCd) {
		this.indCd = indCd;
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

	public String getClassCd() {
		return classCd;
	}

	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getLastJobCd() {
		return lastJobCd;
	}

	public void setLastJobCd(String lastJobCd) {
		this.lastJobCd = lastJobCd;
	}

	public Integer getLastPickedQty() {
		return lastPickedQty;
	}

	public void setLastPickedQty(Integer lastPickedQty) {
		this.lastPickedQty = lastPickedQty;
	}

	public String getJobInstanceId() {
		return jobInstanceId;
	}

	public void setJobInstanceId(String jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}	
}
