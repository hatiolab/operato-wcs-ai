package xyz.anythings.base.entity;

import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.entity.ifc.IBucket;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "tray_boxes", idStrategy = GenerationRule.UUID, uniqueFields="domainId,trayCd", indexes = {
	@Index(name = "ix_tray_boxes_0", columnList = "domain_id,tray_cd", unique = true)
})
public class TrayBox extends xyz.elidom.orm.entity.basic.ElidomStampHook implements IBucket{
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 675245030092189565L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "tray_cd", nullable = false, length = 30)
	private String trayCd;

	@Column (name = "tray_nm", nullable = false, length = 100)
	private String trayNm;

	@Column (name = "tray_type", length = 20)
	private String trayType;

	@Column (name = "tray_color", length = 10)
	private String trayColor;
	
	@Column (name = "bin_count", length = 12)
	private Integer binCount;

	@Column (name = "status", length = 10)
	private String status;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTrayCd() {
		return trayCd;
	}

	public void setTrayCd(String trayCd) {
		this.trayCd = trayCd;
	}

	public String getTrayNm() {
		return trayNm;
	}

	public void setTrayNm(String trayNm) {
		this.trayNm = trayNm;
	}

	public String getTrayType() {
		return trayType;
	}

	public void setTrayType(String trayType) {
		this.trayType = trayType;
	}

	public String getTrayColor() {
		return trayColor;
	}

	public void setTrayColor(String trayColor) {
		this.trayColor = trayColor;
	}

	public Integer getBinCount() {
		return binCount;
	}

	public void setBinCount(Integer binCount) {
		this.binCount = binCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getBucketCd() {
		return this.getTrayCd();
	}

	@Override
	public String getBucketTypeCd() {
		return this.getTrayType();
	}

	@Override
	public String getBucketColor() {
		return this.getTrayColor();
	}	
	
	@Override
	public String getBucketType() {
		return LogisCodeConstants.BOX_TYPE_TRAY;
	}
}
