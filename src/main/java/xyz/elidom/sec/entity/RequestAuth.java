/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;
import xyz.elidom.sec.entity.relation.RoleRef;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.entity.relation.UserRef;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ValueUtil;

@Table(name = "request_auths", idStrategy = GenerationRule.UUID, indexes = { 
	@Index(name = "ix_req_auth_0", columnList = "domain_id"),
	@Index(name = "ix_req_auth_1", columnList = "domain_id,role_id,request_type,status") 
})
public class RequestAuth extends ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 7921491034707007923L;
	
	/**
	 * 계정 신청 
	 */
	public static final String TYPE_ACCOUNT = "ACCOUNT";
	/**
	 * 권한 신청
	 */
	public static final String TYPE_ROLE = "ROLE";
	/**
	 * 패스워드 초기화 신청 
	 */
	public static final String TYPE_PASSWORD = "PASSWORD";
	/**
	 * 계정 활성화 신청
	 */
	public static final String TYPE_ACTIVATION = "ACTIVATION";
	
	/**
	 * 처리 상태 - 대기 
	 */
	public static final String STATUS_WAIT = "WAIT";
	/**
	 * 처리 상태 - 완료 
	 */
	public static final String STATUS_COMPLETED = "COMPLETED";
	/**
	 * 처리 상태 - 반려  
	 */
	public static final String STATUS_REJECTED = "RETURNED";
	/**
	 * 처리 상태 - 취소 
	 */
	public static final String STATUS_CANCELED = "CANCELED";	
	
	@PrimaryKey
	@Column (name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column (name = "title", length = OrmConstants.FIELD_SIZE_LONG_NAME, nullable = false)
	private String title;

	@Column (name = "requester_id", length = OrmConstants.FIELD_SIZE_USER_ID, nullable = false)
	private String requesterId;
	
	@Relation(field = "requesterId")
	private UserRef requester;	
	
	@Column (name = "requester_email", length = OrmConstants.FIELD_SIZE_EMAIL)
	private String requesterEmail;
	
	@Column (name = "requester_name", length = OrmConstants.FIELD_SIZE_NAME, nullable = false)
	private String requesterName;
	
	@Column (name = "request_type", length = 10, nullable = false)
	private String requestType;

	@Column (name = "role_id", length = OrmConstants.FIELD_SIZE_UUID)
	private String roleId;

	@Relation(field = "roleId")
	private RoleRef role;

	@Column (name = "status", length = 15, nullable = false)
	private String status;

	@Column (name = "opinion", length = OrmConstants.FIELD_SIZE_VALUE_255)
	private String opinion;
	
	public RequestAuth() {
	}
	
	public RequestAuth(String id) {
		this.id = id;
	}
	
	public RequestAuth(Long domainId, String requesterId, String requesterEmail, String requesterName, String requestType) {
		this.domainId = domainId;
		this.requesterId = requesterId;	
		this.requesterEmail = requesterEmail;
		this.requesterName = requesterName;
		this.requestType = requestType;
	}
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRequesterId() {
		return requesterId;
	}
	
	public void setRequesterId(String requesterId) {
		this.requesterId = requesterId;
	}
	
	public UserRef getRequester() {
		return requester;
	}

	public void setRequester(UserRef requester) {
		this.requester = requester;

		if(this.requester != null) {
			String refId = this.requester.getId();
			if (refId != null) {
				this.requesterId = refId;
			}
		}
	}	
	
	public String getRequesterEmail() {
		return requesterEmail;
	}

	public void setRequesterEmail(String requesterEmail) {
		this.requesterEmail = requesterEmail;
	}

	public String getRequesterName() {
		return requesterName;
	}

	public void setRequesterName(String requesterName) {
		this.requesterName = requesterName;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public RoleRef getRole() {
		return role;
	}

	public void setRole(RoleRef role) {
		this.role = role;

		if(this.role != null) {
			String refId = this.role.getId();
			if (refId != null) {
				this.roleId = refId;
			}
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

	@Override
	public void beforeCreate() {
		super.beforeCreate();
		
		this.status = RequestAuth.STATUS_WAIT;
		User account = User.getUserByLoginId(this.requesterId);
		
		if(ValueUtil.isEqual(RequestAuth.TYPE_ACCOUNT, this.requestType)) {
			this.title = MessageUtil.getLocaleMessage(account.getLocale(), SysMessageConstants.USER_REQUEST, SysMessageConstants.USER_REQUEST);
			
		} else if(ValueUtil.isEqual(RequestAuth.TYPE_ROLE, this.requestType)) {
			this.title = MessageUtil.getLocaleMessage(account.getLocale(), SysMessageConstants.USER_REQUEST_ADD_AUTH, SysMessageConstants.USER_REQUEST_ADD_AUTH);
			
		} else if(ValueUtil.isEqual(RequestAuth.TYPE_PASSWORD, this.requestType)) {
			this.title = MessageUtil.getLocaleMessage(account.getLocale(), SysMessageConstants.USER_REQUEST_INIT_PASS, SysMessageConstants.USER_REQUEST_INIT_PASS);
			
		} else if(ValueUtil.isEqual(RequestAuth.TYPE_ACTIVATION, this.requestType)) {
			this.title = MessageUtil.getLocaleMessage(account.getLocale(), SysMessageConstants.USER_REQUEST_ACTIVE_ACCOUNT, SysMessageConstants.USER_REQUEST_ACTIVE_ACCOUNT);
		}
		
		if(ValueUtil.isEmpty(this.creatorId)) {
			this.creatorId = account.getId();
		}
	}
	
}