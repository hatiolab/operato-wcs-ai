package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "serial_instances", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,comCd,skuCd,serialNo", indexes = {
	@Index(name = "ix_serial_instances_0", columnList = "domain_id,com_cd,sku_cd,serial_no", unique = true),
	@Index(name = "ix_serial_instances_1", columnList = "domain_id,batch_id"),
	@Index(name = "ix_serial_instances_2", columnList = "domain_id,job_date,job_seq"),
	@Index(name = "ix_serial_instances_3", columnList = "domain_id,cust_order_no"),
	@Index(name = "ix_serial_instances_4", columnList = "domain_id,order_no"),
	@Index(name = "ix_serial_instances_5", columnList = "domain_id,box_id"),
	@Index(name = "ix_serial_instances_6", columnList = "domain_id,invoice_id"),
	@Index(name = "ix_serial_instances_7", columnList = "domain_id,shop_cd")
})
public class SerialInstance extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 527401588164356283L;

	@PrimaryKey
	@Column(name = "id", nullable = false, length = 40)
	private String id;

	@Column(name = "batch_id", length = 40)
	private String batchId;

	@Column(name = "job_date", length = 10)
	private String jobDate;

	@Column(name = "job_seq", length = 10)
	private String jobSeq;

	@Column(name = "wave_no", length = 40)
	private String waveNo;
	
	@Column(name = "shop_cd", length = 30)
	private String shopCd;
	
	@Column(name = "shop_nm", length = 40)
	private String shopNm;

	@Column(name = "cust_order_no", length = 40)
	private String custOrderNo;

	@Column(name = "order_no", length = 40)
	private String orderNo;
	
	@Column(name = "invoice_id", length = 40)
	private String invoiceId;
	
	@Column(name = "box_id", length = 40)
	private String boxId;

	@Column(name = "com_cd", length = 30)
	private String comCd;
	
	@Column(name = "sku_cd", length = 30)
	private String skuCd;
	
	@Column(name = "sku_barcd", length = 30)
	private String skuBarcd;
	
	@Column(name = "sku_nm", length = 200)
	private String skuNm;

	@Column(name = "serial_no", length = 60)
	private String serialNo;
	
	@Column(name = "lot_no", length = 60)
	private String lotNo;

	@Column(name = "status", length = 10)
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(String jobSeq) {
		this.jobSeq = jobSeq;
	}

	public String getWaveNo() {
		return waveNo;
	}

	public void setWaveNo(String waveNo) {
		this.waveNo = waveNo;
	}

	public String getShopCd() {
		return shopCd;
	}

	public void setShopCd(String shopCd) {
		this.shopCd = shopCd;
	}

	public String getShopNm() {
		return shopNm;
	}

	public void setShopNm(String shopNm) {
		this.shopNm = shopNm;
	}

	public String getCustOrderNo() {
		return custOrderNo;
	}

	public void setCustOrderNo(String custOrderNo) {
		this.custOrderNo = custOrderNo;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuBarcd() {
		return skuBarcd;
	}

	public void setSkuBarcd(String skuBarcd) {
		this.skuBarcd = skuBarcd;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getLotNo() {
		return lotNo;
	}

	public void setLotNo(String lotNo) {
		this.lotNo = lotNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
