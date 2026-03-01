package operato.logis.sms.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "chutes", idStrategy = GenerationRule.UUID)
public class Chute extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 133729899867756756L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "sorter_cd", length = 30)
	private String sorterCd;

	@Column (name = "chute_no", length = 10)
	private String chuteNo;

	@Column (name = "chute_type", length = 20)
	private String chuteType;

	@Column (name = "job_type", length = 30)
	private String jobType;

	@Column (name = "batch_id", length = 40)
	private String batchId;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "active_flag", length = 1)
	private Boolean activeFlag;

	@Column (name = "ext_flag", length = 1)
	private Boolean extFlag;

	@Column (name = "ext_cell_cnt", length = 12)
	private Integer extCellCnt;

	@Column (name = "station_cd", length = 30)
	private String stationCd;
  
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

	public String getChuteNo() {
		return chuteNo;
	}

	public void setChuteNo(String chuteNo) {
		this.chuteNo = chuteNo;
	}

	public String getChuteType() {
		return chuteType;
	}

	public void setChuteType(String chuteType) {
		this.chuteType = chuteType;
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

	public Boolean getExtFlag() {
		return extFlag;
	}

	public void setExtFlag(Boolean extFlag) {
		this.extFlag = extFlag;
	}

	public Integer getExtCellCnt() {
		return extCellCnt;
	}

	public void setExtCellCnt(Integer extCellCnt) {
		this.extCellCnt = extCellCnt;
	}

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}
}
