package xyz.anythings.sec.entity;

import java.io.Serializable;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "users_domains")
public class UsersDomain implements Serializable {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 549988541244073275L;

	@PrimaryKey
	@Column (name = "users_id", nullable = false, length = 40)
	private String usersId;

	@PrimaryKey
	@Column (name = "domains_id", nullable = false)
	private Integer domainsId;

	public String getUsersId() {
		return usersId;
	}

	public void setUsersId(String usersId) {
		this.usersId = usersId;
	}

	public Integer getDomainsId() {
		return domainsId;
	}

	public void setDomainsId(Integer domainsId) {
		this.domainsId = domainsId;
	}
}
