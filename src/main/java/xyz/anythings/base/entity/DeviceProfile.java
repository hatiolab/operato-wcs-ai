package xyz.anythings.base.entity;

import java.util.List;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "device_profiles", idStrategy = GenerationRule.UUID, uniqueFields="domainId,stageCd,deviceType,comCd,jobType,equipType,equipCd,profileCd", indexes = {
	@Index(name = "ix_device_profiles_0", columnList = "domain_id,stage_cd,device_type,com_cd,job_type,equip_type,equip_cd,profile_cd", unique = true)
})
public class DeviceProfile extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 903271763310967560L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "device_type", length = 20)
	private String deviceType;

	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "job_type", length = 20)
	private String jobType;

	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "profile_cd", nullable = false, length = 30)
	private String profileCd;

	@Column (name = "profile_nm", nullable = false, length = 100)
	private String profileNm;

	@Column (name = "default_flag", length = 1)
	private Boolean defaultFlag;
	
	@Ignore
	private List<DeviceConf> items;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
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

	public String getProfileCd() {
		return profileCd;
	}

	public void setProfileCd(String profileCd) {
		this.profileCd = profileCd;
	}

	public String getProfileNm() {
		return profileNm;
	}

	public void setProfileNm(String profileNm) {
		this.profileNm = profileNm;
	}

	public Boolean getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public List<DeviceConf> getItems() {
		return items;
	}

	public void setItems(List<DeviceConf> items) {
		this.items = items;
	}

}
