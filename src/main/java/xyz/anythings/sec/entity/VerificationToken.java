package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "verification_tokens", idStrategy = GenerationRule.NONE, uniqueFields="userId", indexes = {
	@Index(name = "ix_verification_tokens_0", columnList = "user_id", unique = true)
})
public class VerificationToken extends xyz.elidom.orm.entity.basic.TimeStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 173565796863504271L;

	@PrimaryKey
	@Column (name = "user_id", nullable = false)
	private String userId;

	@Column (name = "token", nullable = false, length = 40)
	private String token;

	@Column (name = "type", nullable = false, length = 20)
	private String type;

	@Column (name = "suppliment")
	private String suppliment;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSuppliment() {
		return suppliment;
	}

	public void setSuppliment(String suppliment) {
		this.suppliment = suppliment;
	}
}
