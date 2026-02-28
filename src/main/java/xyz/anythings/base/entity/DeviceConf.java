package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "device_confs", idStrategy = GenerationRule.UUID, uniqueFields="deviceProfileId,name", indexes = {
	@Index(name = "ix_device_confs_0", columnList = "device_profile_id,name", unique = true)
})
public class DeviceConf extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 425546153703991636L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "device_profile_id", length = 40)
	private String deviceProfileId;

	@Column (name = "category", length = 100)
	private String category;

	@Column (name = "name", nullable = false, length = 100)
	private String name;

	@Column (name = "description")
	private String description;

	@Column (name = "value", length = 100)
	private String value;

	@Column (name = "remark")
	private String remark;

	@Column (name = "config", length = 4000)
	private String config;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceProfileId() {
		return deviceProfileId;
	}

	public void setDeviceProfileId(String deviceProfileId) {
		this.deviceProfileId = deviceProfileId;
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
