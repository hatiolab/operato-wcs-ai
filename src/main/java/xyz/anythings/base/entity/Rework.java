package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "reworks", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_reworks_0", columnList = "domain_id,batch_id"),
	@Index(name = "ix_reworks_1", columnList = "domain_id,job_date,job_seq,job_type,rework_type"),
	@Index(name = "ix_reworks_2", columnList = "domain_id,job_date,job_seq,area_cd,stage_cd,equip_type,equip_cd,rework_type"),
	@Index(name = "ix_reworks_3", columnList = "domain_id,created_at")
})
public class Rework extends xyz.elidom.orm.entity.basic.DomainCreateStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 214090306701458508L;
	
	/**
	 * 재작업 - 릴레이
	 */
	public static final String REWORK_TYPE_RELAY = "RELAY";
	/**
	 * 재작업 - 재점등
	 */
	public static final String REWORK_TYPE_RELIGHT = "RELIGHT";
	/**
	 * 재작업 - 검수
	 */
	public static final String REWORK_TYPE_INSPECTION = "INSPECTION";

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", nullable = false, length = 40)
	private String batchId;

	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column (name = "job_seq", length = 10)
	private String jobSeq;

	@Column (name = "job_type", length = 20)
	private String jobType;

	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "area_cd", length = 30)
	private String areaCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;

	@Column (name = "equip_type", length = 30)
	private String equipType;

	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "rework_seq")
	private Integer reworkSeq;

	@Column (name = "rework_cd", length = 30)
	private String reworkCd;

	@Column (name = "input_cd", length = 30)
	private String inputCd;

	@Column (name = "input_nm", length = 200)
	private String inputNm;

	@Column (name = "rework_qty", length = 12)
	private Integer reworkQty;

	@Column (name = "rework_type", length = 10)
	private String reworkType;
  
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

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(String jobSeq) {
		this.jobSeq = jobSeq;
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

	public String getAreaCd() {
		return areaCd;
	}

	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public Integer getReworkSeq() {
		return reworkSeq;
	}

	public void setReworkSeq(Integer reworkSeq) {
		this.reworkSeq = reworkSeq;
	}

	public String getReworkCd() {
		return reworkCd;
	}

	public void setReworkCd(String reworkCd) {
		this.reworkCd = reworkCd;
	}

	public String getInputCd() {
		return inputCd;
	}

	public void setInputCd(String inputCd) {
		this.inputCd = inputCd;
	}

	public String getInputNm() {
		return inputNm;
	}

	public void setInputNm(String inputNm) {
		this.inputNm = inputNm;
	}

	public Integer getReworkQty() {
		return reworkQty;
	}

	public void setReworkQty(Integer reworkQty) {
		this.reworkQty = reworkQty;
	}

	public String getReworkType() {
		return reworkType;
	}

	public void setReworkType(String reworkType) {
		this.reworkType = reworkType;
	}
}
