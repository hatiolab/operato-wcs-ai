package operato.logis.sms.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "sorters", idStrategy = GenerationRule.UUID, uniqueFields="domainId,sorterCd", indexes = {
	@Index(name = "ix_sorters_0", columnList = "domain_id,sorter_cd", unique = true)
})
public class Sorter extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 262655830755420674L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "area_cd", length = 30)
	private String areaCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;
	
	@Column (name = "sorter_cd", nullable = false, length = 30)
	private String sorterCd;

	@Column (name = "sorter_nm", nullable = false, length = 40)
	private String sorterNm;

	@Column (name = "chute_cnt", length = 12)
	private Integer chuteCnt;

	@Column (name = "assign_strategy", length = 30)
	private String assignStrategy;

	@Column (name = "assort_strategy", length = 30)
	private String assortStrategy;

	@Column (name = "std_uph", length = 19)
	private Float stdUph;

	@Column (name = "avg_uph", length = 19)
	private Float avgUph;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "active_flag", length = 1)
	private Boolean activeFlag;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
	}

	public String getSorterCd() {
		return sorterCd;
	}

	public void setSorterCd(String sorterCd) {
		this.sorterCd = sorterCd;
	}

	public String getSorterNm() {
		return sorterNm;
	}

	public void setSorterNm(String sorterNm) {
		this.sorterNm = sorterNm;
	}

	public Integer getChuteCnt() {
		return chuteCnt;
	}

	public void setChuteCnt(Integer chuteCnt) {
		this.chuteCnt = chuteCnt;
	}

	public String getAssignStrategy() {
		return assignStrategy;
	}

	public void setAssignStrategy(String assignStrategy) {
		this.assignStrategy = assignStrategy;
	}

	public String getAssortStrategy() {
		return assortStrategy;
	}

	public void setAssortStrategy(String assortStrategy) {
		this.assortStrategy = assortStrategy;
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
}
