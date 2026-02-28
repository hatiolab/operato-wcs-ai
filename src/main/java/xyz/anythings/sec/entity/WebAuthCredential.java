package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "web_auth_credentials", idStrategy = GenerationRule.UUID, uniqueFields="userId,credentialId", indexes = {
	@Index(name = "ix_web_auth_credentials_0", columnList = "user_id,credential_id", unique = true)
})
public class WebAuthCredential extends xyz.elidom.orm.entity.basic.UserTimeStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 173806345469560410L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "user_id", length = 40)
	private String userId;

	@Column (name = "credential_id", nullable = false, length = 256)
	private String credentialId;

	@Column (name = "public_key", nullable = false, length = 256)
	private String publicKey;

	@Column (name = "counter", nullable = false)
	private Integer counter;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer counter) {
		this.counter = counter;
	}	
}
