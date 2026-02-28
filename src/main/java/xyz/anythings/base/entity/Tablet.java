package xyz.anythings.base.entity;

import xyz.anythings.base.model.IDevice;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "tablets", idStrategy = GenerationRule.UUID, uniqueFields="domainId,tabletIp", indexes = {
	@Index(name = "ix_tablets_0", columnList = "domain_id,tablet_ip", unique = true),
	@Index(name = "ix_tablets_1", columnList = "domain_id,stage_cd")
})
public class Tablet extends xyz.elidom.orm.entity.basic.ElidomStampHook implements IDevice {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 552572357238461316L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "tablet_cd", nullable = false, length = 30)
	private String tabletCd;

	@Column (name = "tablet_nm", nullable = false, length = 100)
	private String tabletNm;

	@Column (name = "tablet_ip", nullable = false, length = 16)
	private String tabletIp;

	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_cd", nullable = false, length = 30)
	private String equipCd;

	@Column (name = "station_cd", length = 30)
	private String stationCd;

	@Column (name = "status", length = 10)
	private String status;
  
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

	public String getTabletCd() {
		return tabletCd;
	}

	public void setTabletCd(String tabletCd) {
		this.tabletCd = tabletCd;
	}

	public String getTabletNm() {
		return tabletNm;
	}

	public void setTabletNm(String tabletNm) {
		this.tabletNm = tabletNm;
	}

	public String getTabletIp() {
		return tabletIp;
	}

	public void setTabletIp(String tabletIp) {
		this.tabletIp = tabletIp;
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

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}	
}
