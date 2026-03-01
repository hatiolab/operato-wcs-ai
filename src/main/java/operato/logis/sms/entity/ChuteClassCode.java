package operato.logis.sms.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "chute_class_codes", idStrategy = GenerationRule.UUID, uniqueFields="areaCd,stageCd,classTitle,classCd", indexes = {
	@Index(name = "ix_chute_class_codes_0", columnList = "area_cd,stage_cd,class_title,class_cd", unique = true)
})
public class ChuteClassCode extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 270709867729931595L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "area_cd", length = 30)
	private String areaCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "class_title", length = 100)
	private String classTitle;

	@Column (name = "class_cd", length = 30)
	private String classCd;

	@Column (name = "sorter_cd", length = 30)
	private String sorterCd;

	@Column (name = "chute_no", length = 30)
	private String chuteNo;
  
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

	public String getClassTitle() {
		return classTitle;
	}

	public void setClassTitle(String classTitle) {
		this.classTitle = classTitle;
	}

	public String getClassCd() {
		return classCd;
	}

	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}

	public String getSorterCd() {
		return sorterCd;
	}

	public void setSorterCd(String sorterCd) {
		this.sorterCd = sorterCd;
	}

	public String getChuteNo() {
		return chuteNo;
	}

	public void setChuteNo(String chuteNo) {
		this.chuteNo = chuteNo;
	}	
}
