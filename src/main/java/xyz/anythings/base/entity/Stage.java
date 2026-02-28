package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "stages", idStrategy = GenerationRule.UUID, uniqueFields="domainId,areaCd,stageCd", indexes = {
	@Index(name = "ix_stages_0", columnList = "domain_id,area_cd,stage_cd", unique = true),
	@Index(name = "ix_stages_1", columnList = "domain_id,stage_cd", unique = true)
})
public class Stage extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 804249332562971432L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "area_cd", nullable = false, length = 30)
	private String areaCd;

	@Column (name = "stage_cd", nullable = false, length = 30)
	private String stageCd;

	@Column (name = "stage_nm", length = 40)
	private String stageNm;

	@Column (name = "stage_type", length = 20)
	private String stageType;
  
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

	public String getStageNm() {
		return stageNm;
	}

	public void setStageNm(String stageNm) {
		this.stageNm = stageNm;
	}

	public String getStageType() {
		return stageType;
	}

	public void setStageType(String stageType) {
		this.stageType = stageType;
	}	
}
