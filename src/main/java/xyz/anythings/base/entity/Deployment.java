package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "deployments", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_deployments_0", columnList = "target_type,target_id,version,domain_id")
})
public class Deployment extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 139716252076979474L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "target_type", nullable = false, length = 20)
	private String targetType;

	@Column (name = "target_id", nullable = false, length = 40)
	private String targetId;

	@Column (name = "version", nullable = false, length = 15)
	private String version;

	@Column (name = "scheduled_at", nullable = false, length = 22)
	private String scheduledAt;

	@Column (name = "started_at", length = 22)
	private String startedAt;

	@Column (name = "finished_at", length = 22)
	private String finishedAt;

	@Column (name = "remark", length = 1000)
	private String remark;

	@Column (name = "file_name", length = 100)
	private String fileName;

	@Column (name = "file_size", length = 22)
	private Integer fileSize;

	@Column (name = "force_flag", length = 1)
	private Boolean forceFlag;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "file_data")
	private String fileData;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getScheduledAt() {
		return scheduledAt;
	}

	public void setScheduledAt(String scheduledAt) {
		this.scheduledAt = scheduledAt;
	}

	public String getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(String startedAt) {
		this.startedAt = startedAt;
	}

	public String getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(String finishedAt) {
		this.finishedAt = finishedAt;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getFileSize() {
		return fileSize;
	}

	public void setFileSize(Integer fileSize) {
		this.fileSize = fileSize;
	}

	public Boolean getForceFlag() {
		return forceFlag;
	}

	public void setForceFlag(Boolean forceFlag) {
		this.forceFlag = forceFlag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFileData() {
		return fileData;
	}

	public void setFileData(String fileData) {
		this.fileData = fileData;
	}	
}
