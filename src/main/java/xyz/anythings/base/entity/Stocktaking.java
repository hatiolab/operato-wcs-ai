package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 재고 실사
 * 
 * @author shortstop
 */
@Table(name = "stocktakings", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,jobDate,jobSeq,equipType,equipCd", indexes = {
	@Index(name = "ix_stocktakings_0", columnList = "domain_id,job_date,job_seq,equip_type,equip_cd", unique = true) 
})
public class Stocktaking extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 796043866203911286L;
	
	/**
	 * 상태 - 진행 중
	 */
	public static final String STATUS_RUNNING = "R";
	/**
	 * 상태 - 완료
	 */
	public static final String STATUS_FINISHED = "F";

	@PrimaryKey
	@Column(name = "id", nullable = false, length = 40)
	private String id;

	@Column(name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column(name = "job_seq", nullable = false, length = 12)
	private Integer jobSeq;
	
	@Column (name = "stage_cd", nullable = false, length = 30)
	private String stageCd;

	@Column(name = "equip_type", nullable = false, length = 20)
	private String equipType;

	@Column(name = "equip_cd", nullable = false, length = 30)
	private String equipCd;

	@Column(name = "status", length = 10)
	private String status;

	@Column(name = "remark", length = 1000)
	private String remark;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
