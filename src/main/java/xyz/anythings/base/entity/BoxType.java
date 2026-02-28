package xyz.anythings.base.entity;

import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.entity.ifc.IBucket;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "box_types", idStrategy = GenerationRule.UUID, uniqueFields="domainId,boxTypeCd", indexes = {
	@Index(name = "ix_box_types_0", columnList = "domain_id,box_type_cd", unique = true)
})
public class BoxType extends xyz.elidom.orm.entity.basic.ElidomStampHook implements IBucket {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 171698191754410361L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;
	
	@Column (name = "stage_cd", length = 30)
	private String stageCd;
	
	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "box_type_cd", nullable = false, length = 30)
	private String boxTypeCd;

	@Column (name = "box_type_nm", nullable = false, length = 40)
	private String boxTypeNm;

	@Column (name = "box_color", length = 10)
	private String boxColor;

	@Column (name = "box_wt", length = 19)
	private Float boxWt;

	@Column (name = "box_wt_min", length = 19)
	private Float boxWtMin;

	@Column (name = "box_wt_max", length = 19)
	private Float boxWtMax;

	@Column (name = "box_len", length = 19)
	private Float boxLen;

	@Column (name = "box_wd", length = 19)
	private Float boxWd;

	@Column (name = "box_ht", length = 19)
	private Float boxHt;

	@Column (name = "box_vol", length = 19)
	private Float boxVol;

	@Column (name = "len_unit", length = 6)
	private String lenUnit;

	@Column (name = "vol_unit", length = 6)
	private String volUnit;
	
	@Ignore
	private String boxId;
  
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

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getBoxTypeCd() {
		return boxTypeCd;
	}

	public void setBoxTypeCd(String boxTypeCd) {
		this.boxTypeCd = boxTypeCd;
	}

	public String getBoxTypeNm() {
		return boxTypeNm;
	}

	public void setBoxTypeNm(String boxTypeNm) {
		this.boxTypeNm = boxTypeNm;
	}

	public String getBoxColor() {
		return boxColor;
	}

	public void setBoxColor(String boxColor) {
		this.boxColor = boxColor;
	}

	public Float getBoxWt() {
		return boxWt;
	}

	public void setBoxWt(Float boxWt) {
		this.boxWt = boxWt;
	}

	public Float getBoxWtMin() {
		return boxWtMin;
	}

	public void setBoxWtMin(Float boxWtMin) {
		this.boxWtMin = boxWtMin;
	}

	public Float getBoxWtMax() {
		return boxWtMax;
	}

	public void setBoxWtMax(Float boxWtMax) {
		this.boxWtMax = boxWtMax;
	}

	public Float getBoxLen() {
		return boxLen;
	}

	public void setBoxLen(Float boxLen) {
		this.boxLen = boxLen;
	}

	public Float getBoxWd() {
		return boxWd;
	}

	public void setBoxWd(Float boxWd) {
		this.boxWd = boxWd;
	}

	public Float getBoxHt() {
		return boxHt;
	}

	public void setBoxHt(Float boxHt) {
		this.boxHt = boxHt;
	}

	public Float getBoxVol() {
		return boxVol;
	}

	public void setBoxVol(Float boxVol) {
		this.boxVol = boxVol;
	}

	public String getLenUnit() {
		return lenUnit;
	}

	public void setLenUnit(String lenUnit) {
		this.lenUnit = lenUnit;
	}

	public String getVolUnit() {
		return volUnit;
	}

	public void setVolUnit(String volUnit) {
		this.volUnit = volUnit;
	}
	
	public String getBoxId() {
		return this.boxId;
	}
	
	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	@Override
	public String getBucketCd() {
		return this.getBoxId();
	}

	@Override
	public String getBucketTypeCd() {
		return this.getBoxTypeCd();
	}

	@Override
	public String getBucketColor() {
		return this.getBoxColor();
	}
	
	@Override
	public String getStatus() {
		// 미구현 
		return null;
	}
	
	@Override
	public String getBucketType() {
		return LogisCodeConstants.BOX_TYPE_BOX;
	}

	@Override
	public void beforeUpdate() {
		super.beforeUpdate();
		
		if(this.boxHt != null && this.boxLen != null && this.boxWd != null) {
			this.boxVol = this.boxHt *this.boxLen * this.boxWd;
		}
	}
}
