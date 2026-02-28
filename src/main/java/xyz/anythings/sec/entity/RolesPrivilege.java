package xyz.anythings.sec.entity;

import java.io.Serializable;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "roles_privileges")
public class RolesPrivilege implements Serializable {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 145311136205135759L;

	@PrimaryKey
	@Column (name = "privileges_id", nullable = false, length = 40)
	private String privilegesId;

	@PrimaryKey
	@Column (name = "roles_id", nullable = false, length = 40)
	private String rolesId;

	public String getPrivilegesId() {
		return privilegesId;
	}

	public void setPrivilegesId(String privilegesId) {
		this.privilegesId = privilegesId;
	}

	public String getRolesId() {
		return rolesId;
	}

	public void setRolesId(String rolesId) {
		this.rolesId = rolesId;
	}
}
