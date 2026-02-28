package xyz.anythings.gw.entity;

import xyz.anythings.gw.GwConstants;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "gateways", idStrategy = GenerationRule.UUID, uniqueFields="domainId,gwCd", indexes = {
	@Index(name = "ix_gateways_0", columnList = "domain_id,gw_cd", unique = true),
	@Index(name = "ix_gateways_1", columnList = "domain_id,stage_cd")
})
public class Gateway extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 511723720713339618L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "stage_cd", nullable = false, length = 30)
	private String stageCd;
	
	@Column (name = "gw_cd", nullable = false, length = 30)
	private String gwCd;

	@Column (name = "gw_nm", length = 100)
	private String gwNm;

	@Column (name = "gw_ip", length = 16)
	private String gwIp;

	@Column (name = "channel_no", length = 40)
	private String channelNo;

	@Column (name = "pan_no", length = 40)
	private String panNo;

	@Column (name = "version", length = 15)
	private String version;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "remark", length = 1000)
	private String remark;
	
	/**
	 * 작업 배치 ID
	 */
	@Ignore
	private String batchId;

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

	public String getGwCd() {
		return gwCd;
	}

	public void setGwCd(String gwCd) {
		this.gwCd = gwCd;
	}

	public String getGwNm() {
		return gwNm;
	}

	public void setGwNm(String gwNm) {
		this.gwNm = gwNm;
	}

	public String getGwIp() {
		return gwIp;
	}

	public void setGwIp(String gwIp) {
		this.gwIp = gwIp;
	}

	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public String getPanNo() {
		return panNo;
	}

	public void setPanNo(String panNo) {
		this.panNo = panNo;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
	
	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	
	public static String buildGatewayPath(String siteCd, String areaCd, String stageCd, String gwCd) {
		return siteCd + GwConstants.SLASH + areaCd + GwConstants.SLASH + stageCd + GwConstants.SLASH + gwCd;
	}

}
