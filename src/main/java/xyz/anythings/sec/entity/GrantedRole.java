package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Index;

import java.io.Serializable;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "granted_roles", idStrategy = GenerationRule.UUID, uniqueFields="roleId,domainId", indexes = {
	@Index(name = "ix_granted_roles_0", columnList = "role_id,domain_id", unique = true)
})
public class GrantedRole implements Serializable {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 506738301669896792L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "domain_id", nullable = false, length = 40)
	private Long domainId;
	
	@Column (name = "role_id", nullable = false, length = 40)
	private String roleId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
}
