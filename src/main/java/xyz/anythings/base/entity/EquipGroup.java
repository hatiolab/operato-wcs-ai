package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "equip_groups", idStrategy = GenerationRule.UUID, uniqueFields="domainId,equipGroupCd", indexes = {
	@Index(name = "ix_equip_groups_0", columnList = "domain_id,equip_group_cd", unique = true)
})
public class EquipGroup extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 304444492646549193L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;

	@Column (name = "equip_group_nm", length = 100)
	private String equipGroupNm;

	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "area_cd", length = 30)
	private String areaCd;
	
	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	/**
	 * 표준 투입 작업자 수
	 */
	@Column (name = "std_workers", length = 12)
	private Float stdWorkers;
	/**
	 * 평균 투입 작업자 수 - 실 데이터 기반으로 매일 업데이트
	 */
	@Column (name = "avg_workers", length = 12)
	private Float avgWorkers;
	/**
	 * 표준 인당 UPH
	 */
	@Column (name = "std_uph", length = 12)
	private Float stdUph;
	/**
	 * 평균 인당 UPH - 실 데이터 기반으로 매일 업데이트
	 */
	@Column (name = "avg_uph", length = 12)
	private Float avgUph;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
	}

	public String getEquipGroupNm() {
		return equipGroupNm;
	}

	public void setEquipGroupNm(String equipGroupNm) {
		this.equipGroupNm = equipGroupNm;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getAreaCd() {
		return areaCd;
	}

	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public Float getStdWorkers() {
		return stdWorkers;
	}

	public void setStdWorkers(Float stdWorkers) {
		this.stdWorkers = stdWorkers;
	}

	public Float getAvgWorkers() {
		return avgWorkers;
	}

	public void setAvgWorkers(Float avgWorkers) {
		this.avgWorkers = avgWorkers;
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
