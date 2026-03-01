package operato.logis.sms.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "inductions", idStrategy = GenerationRule.UUID)
public class Induction extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 521828644971868231L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "sorter_cd", length = 30)
	private String sorterCd;

	@Column (name = "induction_cd", length = 10)
	private String inductionCd;

	@Column (name = "job_type", length = 30)
	private String jobType;

	@Column (name = "batch_id", length = 40)
	private String batchId;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "active_flag", length = 1)
	private Boolean activeFlag;

	@Column (name = "std_uph", length = 19)
	private Float stdUph;

	@Column (name = "avg_uph", length = 19)
	private Float avgUph;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSorterCd() {
		return sorterCd;
	}

	public void setSorterCd(String sorterCd) {
		this.sorterCd = sorterCd;
	}

	public String getInductionCd() {
		return inductionCd;
	}

	public void setInductionCd(String inductionCd) {
		this.inductionCd = inductionCd;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
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

	public Float getStdUph() {
		return stdUph;
	}

	public void setStdUph(Float stdUph) {
		this.stdUph = stdUph;
	}

	public Float getAvgUph() {
		return avgUph;
	}

	public void setAvgUph(Float avgUph) {
		this.avgUph = avgUph;
	}	
}
