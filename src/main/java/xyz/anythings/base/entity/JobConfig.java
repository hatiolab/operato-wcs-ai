package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "job_configs", idStrategy = GenerationRule.UUID, uniqueFields="jobConfigSetId,name", indexes = {
	@Index(name = "ix_job_configs_0", columnList = "job_config_set_id,name", unique = true)
})
public class JobConfig extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 136198005936806989L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "job_config_set_id", nullable = false, length = 40)
	private String jobConfigSetId;

	@Column (name = "category", length = 100)
	private String category;

	@Column (name = "name", nullable = false, length = 100)
	private String name;

	@Column (name = "description", length = 255)
	private String description;

	@Column (name = "value", length = 100)
	private String value;

	@Column (name = "remark", length = 255)
	private String remark;

	@Column (name = "config", length = 4000)
	private String config;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJobConfigSetId() {
		return jobConfigSetId;
	}

	public void setJobConfigSetId(String jobConfigSetId) {
		this.jobConfigSetId = jobConfigSetId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}	
}
