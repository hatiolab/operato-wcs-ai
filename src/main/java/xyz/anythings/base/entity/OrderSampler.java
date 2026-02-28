package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 테스트를 위한 샘플 주문 생성기
 * 
 * @author shortstop
 */
@Table(name = "order_samplers", idStrategy = GenerationRule.UUID, uniqueFields="jobDate,jobType,jobSeq,comCd", indexes = {
	@Index(name = "ix_order_samplers_0", columnList = "job_date,job_type,job_seq,com_cd", unique = true)
})
public class OrderSampler extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 108156489076040776L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column (name = "order_date", nullable = false, length = 10)
	private String orderDate;

	@Column (name = "job_type", nullable = false, length = 10)
	private String jobType;

	@Column (name = "job_seq", nullable = false, length = 5)
	private Integer jobSeq;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "cust_order_prefix", length = 3)
	private String custOrderPrefix;

	@Column (name = "order_id_prefix", length = 3)
	private String orderIdPrefix;

	@Column (name = "order_type", length = 10)
	private String orderType;

	@Column (name = "total_order_qty", length = 10)
	private Integer totalOrderQty;

	@Column (name = "total_sku_qty", length = 10)
	private Integer totalSkuQty;

	@Column (name = "max_order_qty", length = 10)
	private Integer maxOrderQty;

	@Column (name = "need_invoice_flag", length = 1)
	private Boolean needInvoiceFlag;

	@Column (name = "need_addr_flag", length = 1)
	private Boolean needAddrFlag;

	@Column (name = "need_box_type_flag", length = 1)
	private Boolean needBoxTypeFlag;

	@Column (name = "status", length = 10)
	private String status;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public Integer getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(Integer jobSeq) {
		this.jobSeq = jobSeq;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getCustOrderPrefix() {
		return custOrderPrefix;
	}

	public void setCustOrderPrefix(String custOrderPrefix) {
		this.custOrderPrefix = custOrderPrefix;
	}

	public String getOrderIdPrefix() {
		return orderIdPrefix;
	}

	public void setOrderIdPrefix(String orderIdPrefix) {
		this.orderIdPrefix = orderIdPrefix;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public Integer getTotalOrderQty() {
		return totalOrderQty;
	}

	public void setTotalOrderQty(Integer totalOrderQty) {
		this.totalOrderQty = totalOrderQty;
	}

	public Integer getTotalSkuQty() {
		return totalSkuQty;
	}

	public void setTotalSkuQty(Integer totalSkuQty) {
		this.totalSkuQty = totalSkuQty;
	}

	public Integer getMaxOrderQty() {
		return maxOrderQty;
	}

	public void setMaxOrderQty(Integer maxOrderQty) {
		this.maxOrderQty = maxOrderQty;
	}

	public Boolean getNeedInvoiceFlag() {
		return needInvoiceFlag;
	}

	public void setNeedInvoiceFlag(Boolean needInvoiceFlag) {
		this.needInvoiceFlag = needInvoiceFlag;
	}

	public Boolean getNeedAddrFlag() {
		return needAddrFlag;
	}

	public void setNeedAddrFlag(Boolean needAddrFlag) {
		this.needAddrFlag = needAddrFlag;
	}

	public Boolean getNeedBoxTypeFlag() {
		return needBoxTypeFlag;
	}

	public void setNeedBoxTypeFlag(Boolean needBoxTypeFlag) {
		this.needBoxTypeFlag = needBoxTypeFlag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
