package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "invoices", idStrategy = GenerationRule.UUID, uniqueFields="id", indexes = {
	@Index(name = "ix_invoices_0", columnList = "id", unique = true),
	@Index(name = "ix_invoices_1", columnList = "com_cd")
})
public class Invoice extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 976170137378999303L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "com_cd", length = 30)
	private String comCd;
	
	@Column (name = "customer_cd", length = 30)
	private String customerCd;

	@Column (name = "used_flag", length = 1)
	private Boolean usedFlag;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getCustomerCd() {
		return customerCd;
	}

	public void setCustomerCd(String customerCd) {
		this.customerCd = customerCd;
	}

	public Boolean getUsedFlag() {
		return usedFlag;
	}

	public void setUsedFlag(Boolean usedFlag) {
		this.usedFlag = usedFlag;
	}
}
