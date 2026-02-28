package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "printouts", idStrategy = GenerationRule.UUID, uniqueFields="domainId,reportCd", indexes = {
	@Index(name = "ix_printouts_0", columnList = "domain_id,report_cd", unique = true)
})
public class Printout extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 287194187135011273L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "report_cd", nullable = false, length = 30)
	private String reportCd;

	@Column (name = "report_nm", length = 40)
	private String reportNm;

	@Column (name = "template_cd", length = 30)
	private String templateCd;

	@Column (name = "active_flag", length = 1)
	private Boolean activeFlag;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReportCd() {
		return reportCd;
	}

	public void setReportCd(String reportCd) {
		this.reportCd = reportCd;
	}

	public String getReportNm() {
		return reportNm;
	}

	public void setReportNm(String reportNm) {
		this.reportNm = reportNm;
	}

	public String getTemplateCd() {
		return templateCd;
	}

	public void setTemplateCd(String templateCd) {
		this.templateCd = templateCd;
	}

	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}	
}
