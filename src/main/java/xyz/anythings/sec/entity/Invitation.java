package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "invitations", idStrategy = GenerationRule.UUID)
public class Invitation extends xyz.elidom.orm.entity.basic.UserTimeStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 976513512082699308L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "email", nullable = false, length = 32)
	private String email;

	@Column (name = "reference", nullable = false, length = 32)
	private String reference;

	@Column (name = "type", nullable = false, length = 20)
	private String type;

	@Column (name = "token", nullable = false, length = 50)
	private String token;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
