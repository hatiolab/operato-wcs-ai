package xyz.anythings.base.entity;

import xyz.anythings.base.model.IDevice;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "kiosks", idStrategy = GenerationRule.UUID, uniqueFields="domainId,kioskIp", indexes = {
	@Index(name = "ix_kiosks_0", columnList = "domain_id,kiosk_ip", unique = true),
	@Index(name = "ix_kiosks_1", columnList = "domain_id,stage_cd")
})
public class Kiosk extends xyz.elidom.orm.entity.basic.ElidomStampHook implements IDevice {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 413131753330717177L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "kiosk_cd", nullable = false, length = 30)
	private String kioskCd;

	@Column (name = "kiosk_nm", nullable = false, length = 100)
	private String kioskNm;

	@Column (name = "kiosk_ip", nullable = false, length = 16)
	private String kioskIp;

	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_cd", nullable = false, length = 30)
	private String equipCd;
	
	@Column(name = "side_cd", length = 30)
	private String sideCd;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "remark", length = 1000)
	private String remark;
  
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

	public String getKioskCd() {
		return kioskCd;
	}

	public void setKioskCd(String kioskCd) {
		this.kioskCd = kioskCd;
	}

	public String getKioskNm() {
		return kioskNm;
	}

	public void setKioskNm(String kioskNm) {
		this.kioskNm = kioskNm;
	}

	public String getKioskIp() {
		return kioskIp;
	}

	public void setKioskIp(String kioskIp) {
		this.kioskIp = kioskIp;
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

	public String getSideCd() {
		return sideCd;
	}

	public void setSideCd(String sideCd) {
		this.sideCd = sideCd;
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
