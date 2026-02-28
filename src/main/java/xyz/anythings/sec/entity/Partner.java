package xyz.anythings.sec.entity;

import java.io.Serializable;
import java.util.Date; 
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "partners", idStrategy = GenerationRule.UUID, uniqueFields="domainId,partnerDomainId", indexes = {
	@Index(name = "ix_partners_0", columnList = "domain_id,partner_domain_id", unique = true)
})
public class Partner implements Serializable {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 498148625073795208L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "domain_id")
	private Integer domainId;
	
	@Column (name = "partner_domain_id")
	private Integer partnerDomainId;

	@Column (name = "requested_at", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date requestedAt;

	@Column (name = "approved_at", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date approvedAt;

	@Column (name = "requester_id")
	private String requesterId;

	@Column (name = "approver_id")
	private String approverId;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getDomainId() {
		return domainId;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
	}

	public Integer getPartnerDomainId() {
		return partnerDomainId;
	}

	public void setPartnerDomainId(Integer partnerDomainId) {
		this.partnerDomainId = partnerDomainId;
	}

	public Date getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(Date requestedAt) {
		this.requestedAt = requestedAt;
	}

	public Date getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(Date approvedAt) {
		this.approvedAt = approvedAt;
	}

	public String getRequesterId() {
		return requesterId;
	}

	public void setRequesterId(String requesterId) {
		this.requesterId = requesterId;
	}

	public String getApproverId() {
		return approverId;
	}

	public void setApproverId(String approverId) {
		this.approverId = approverId;
	}
}
