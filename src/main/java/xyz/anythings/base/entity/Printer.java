package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "printers", idStrategy = GenerationRule.UUID, uniqueFields="domainId,printerCd", indexes = {
	@Index(name = "ix_printers_0", columnList = "domain_id,printer_cd", unique = true),
	@Index(name = "ix_printers_1", columnList = "domain_id,stage_cd")
})
public class Printer extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 315339403280044039L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;
	
	@Column (name = "printer_cd", nullable = false, length = 30)
	private String printerCd;

	@Column (name = "printer_nm", nullable = false, length = 100)
	private String printerNm;

	@Column (name = "printer_type", length = 20)
	private String printerType;

	@Column (name = "printer_ip", nullable = false, length = 16)
	private String printerIp;

	@Column (name = "printer_port", length = 12)
	private Integer printerPort;

	@Column (name = "printer_driver", length = 40)
	private String printerDriver;

	@Column (name = "printer_agent_url")
	private String printerAgentUrl;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "remark", length = 1000)
	private String remark;

	@Column (name = "default_flag", length = 1)
	private Boolean defaultFlag;

	@Column (name = "dpi", length = 4)
	private Integer dpi;

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

	public String getPrinterCd() {
		return printerCd;
	}

	public void setPrinterCd(String printerCd) {
		this.printerCd = printerCd;
	}

	public String getPrinterNm() {
		return printerNm;
	}

	public void setPrinterNm(String printerNm) {
		this.printerNm = printerNm;
	}

	public String getPrinterType() {
		return printerType;
	}

	public void setPrinterType(String printerType) {
		this.printerType = printerType;
	}

	public String getPrinterIp() {
		return printerIp;
	}

	public void setPrinterIp(String printerIp) {
		this.printerIp = printerIp;
	}

	public Integer getPrinterPort() {
		return printerPort;
	}

	public void setPrinterPort(Integer printerPort) {
		this.printerPort = printerPort;
	}

	public String getPrinterDriver() {
		return printerDriver;
	}

	public void setPrinterDriver(String printerDriver) {
		this.printerDriver = printerDriver;
	}

	public String getPrinterAgentUrl() {
		return printerAgentUrl;
	}

	public void setPrinterAgentUrl(String printerAgentUrl) {
		this.printerAgentUrl = printerAgentUrl;
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

	public Boolean getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public Integer getDpi() {
		return dpi;
	}

	public void setDpi(Integer dpi) {
		this.dpi = dpi;
	}	
}
