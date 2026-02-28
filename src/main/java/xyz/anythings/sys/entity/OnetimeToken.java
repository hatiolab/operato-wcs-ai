package xyz.anythings.sys.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "onetime_tokens", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_onetime_tokens_0", columnList = "requester_id"),
	@Index(name = "ix_onetime_tokens_1", columnList = "auth_token", unique = true),
	@Index(name = "ix_onetime_tokens_2", columnList = "user_id")
})
public class OnetimeToken extends xyz.elidom.orm.entity.basic.DomainTimeStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 655341186601830135L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "requester_id", length = 32)
	private String requesterId;

	@Column (name = "user_id", length = 32)
	private String userId;
	
	@Column (name = "auth_token", length = 40)
	private String authToken;
	
	@Column (name = "access_ip", length = 30)
	private String accessIp;

	@Column (name = "expired_flag")
	private Boolean expiredFlag;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRequesterId() {
		return requesterId;
	}

	public void setRequesterId(String requesterId) {
		this.requesterId = requesterId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}

	public Boolean getExpiredFlag() {
		return expiredFlag;
	}

	public void setExpiredFlag(Boolean expiredFlag) {
		this.expiredFlag = expiredFlag;
	}	
}
