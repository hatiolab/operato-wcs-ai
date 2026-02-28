package xyz.anythings.base.entity;

import xyz.anythings.base.model.IDevice;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "pdas", idStrategy = GenerationRule.UUID, uniqueFields="domainId,pdaIp", indexes = {
	@Index(name = "ix_pdas_0", columnList = "domain_id,pda_ip", unique = true),
	@Index(name = "ix_pdas_1", columnList = "domain_id,stage_cd")
})
public class PDA extends xyz.elidom.orm.entity.basic.ElidomStampHook implements IDevice {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 914153930963895807L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "pda_cd", nullable = false, length = 30)
	private String pdaCd;

	@Column (name = "pda_nm", nullable = false, length = 100)
	private String pdaNm;

	@Column (name = "pda_ip", nullable = false, length = 16)
	private String pdaIp;

	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "station_cd", length = 30)
	private String stationCd;

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

	public String getPdaCd() {
		return pdaCd;
	}

	public void setPdaCd(String pdaCd) {
		this.pdaCd = pdaCd;
	}

	public String getPdaNm() {
		return pdaNm;
	}

	public void setPdaNm(String pdaNm) {
		this.pdaNm = pdaNm;
	}

	public String getPdaIp() {
		return pdaIp;
	}

	public void setPdaIp(String pdaIp) {
		this.pdaIp = pdaIp;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}	
}
