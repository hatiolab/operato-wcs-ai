/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.rest;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.util.StringJoiner;
import xyz.elidom.exception.client.ElidomRecordNotFoundException;
import xyz.elidom.exception.server.ElidomAlreadyExistException;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sec.SecConfigConstants;
import xyz.elidom.sec.entity.RequestAuth;
import xyz.elidom.sec.entity.Role;
import xyz.elidom.sec.entity.UsersRole;
import xyz.elidom.sec.util.SecurityUtil;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.auth.model.CheckPassword;
import xyz.elidom.sys.system.engine.ITemplateEngine;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.system.transport.sender.MailSender;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.FileUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;

@RestController
@Transactional
@RequestMapping("/rest/request_auths")
@ServiceDesc(description = "RequestAuth Service API")
public class RequestAuthController extends AbstractRestService {
	
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(RequestAuthController.class);
	
	@Autowired
	@Qualifier("basic")
	private ITemplateEngine templateEngine;
	
	@Autowired
	private MailSender mailSender;

	@Override
	protected Class<?> entityClass() {
		return RequestAuth.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiDesc(description = "Find one by ID")
	public RequestAuth findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id:.+}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public RequestAuth create(@RequestBody RequestAuth input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id:.+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiDesc(description = "Update")
	public RequestAuth update(@PathVariable("id") String id, @RequestBody RequestAuth input) {
		return this.updateOne(input);
	}
	
	@RequestMapping(value="/{id:.+}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiDesc(description = "Create, Update or Delete multiple data at one time")
	public Boolean multipleUpdate(@RequestBody List<RequestAuth> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	/****************************************************************************************
	 * 									계정 신청 및 처리 API
	 ****************************************************************************************/
	@RequestMapping(value = "/account/signup", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Account SignUp")
	public Boolean accountSignUp(@RequestBody User account) {
		// 1. 사용자가 존재하지 않는지 체크 
		this.checkNotExistUser(account.getLogin());
		
		// 2. 사용자 로케일 설정 
		if(ValueUtil.isEmpty(account.getLocale())) {
			String defaultLocale = SettingUtil.getValue(SysConstants.DEFAULT_LANGUAGE_KEY, SysConstants.LANG_EN);
			account.setLocale(defaultLocale);
		}
		
		// 3. 데이터 생성 
		queryManager.insert(account);
		
		return true;
	}
	
	@RequestMapping(value = "/account/register/request", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Request register account")
	public String requestAccountRegister(@RequestBody User account) {
		// 1. validation
		AssertUtil.assertNotEmpty("terms.label.domain", account.getDomainId());
		AssertUtil.assertNotEmpty("terms.label.login", account.getLogin());
		AssertUtil.assertNotEmpty("terms.label.name", account.getName());
		// AssertUtil.assertNotEmpty("terms.label.email", account.getEmail());
		// AssertUtil.assertNotEmpty("terms.label.password", account.getEncryptedPassword());

		// 2. 사용자 생성
		if(ValueUtil.isEmpty(account.getLocale())) {
			String defaultLocale = SettingUtil.getValue(SysConstants.DEFAULT_LANGUAGE_KEY, SysConstants.LANG_EN);
			account.setLocale(defaultLocale);
		}
		
		// 3. 사용자가 존재하지 않는지 체크 
		this.checkNotExistUser(account.getLogin());
		account.setEncryptedPassword(account.getEncryptedPassword());
		queryManager.insert(account);
		
		// 4. 권한 요청 생성
		RequestAuth request = new RequestAuth(account.getDomainId(), account.getLogin(), account.getEmail(), account.getName(), RequestAuth.TYPE_ACCOUNT);
		request = this.checkAndCreate(request);
		
		// 5. 관리자에게 계정 신청 메일 전송
		String title = MessageUtil.getMessage(SysMessageConstants.USER_REQUEST, "System account registering request");
		this.sendCommonRequestEmail(title, request, account);
		
		// 6. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.REQUEST_SENT_TO_ADMIN, "Your request was sent to administrator.");
	}

	@RequestMapping(value = "/account/register/approve/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Approve register account")
	public String approveAccountRegister(@PathVariable("id") String id, @RequestBody RequestAuth content) {
		// 1. Validation
		RequestAuth request = this.checkRequestAuth(id, RequestAuth.TYPE_ACCOUNT, MessageUtil.getMessage(SysMessageConstants.NOT_A_ACCOUNT_APPR_REQUEST, "This request is not for a account approval."));
		
		// 2. 계정 승인 처리 
		User account = User.getUserById(request.getRequesterId());
		if(account.getActiveFlag()) {
			// Already activated account.
			throw ThrowUtil.newAlreadyActivatedAccount();
		}
		
		account.setActiveFlag(true);
		queryManager.update(account);

		// 3. 요청 처리 
		request.setStatus(RequestAuth.STATUS_COMPLETED);
		request.setOpinion(content.getOpinion());
		this.queryManager.update(request, "status", "opinion", "updaterId");
		
		// 4. 신청자에게 승인 처리 메일 전송 
		String title = MessageUtil.getMessage(SysMessageConstants.USER_APPROVAL, "Account approval request has been accepted");
		this.sendCommonApprovedEmail(title, request, account);
		
		// 5. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.REQUEST_PROCESSED_APPROVED, "Your request has been processed 'approved'.");
	}

	@RequestMapping(value = "/account/register/reject/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Reject register account")
	public String rejectAccountRegister(@PathVariable("id") String id, @RequestBody RequestAuth content) {
		// 1. Validation
		RequestAuth request = this.checkRequestAuth(id, RequestAuth.TYPE_ACCOUNT, MessageUtil.getMessage(SysMessageConstants.NOT_A_ACCOUNT_APPR_REQUEST, "This request is not for a account approval."));
		
		// 2. 계정 삭제 처리
		User account = User.getUserById(request.getRequesterId());
		
		if(account != null) {
			if(account.getActiveFlag()) {
				// Already activated account.
				throw ThrowUtil.newAlreadyActivatedAccount();
			}
			
			queryManager.delete(account);
		}
		
		// 3. 요청 처리 
		request.setStatus(RequestAuth.STATUS_REJECTED);
		request.setOpinion(content.getOpinion());
		this.queryManager.update(request, "status", "opinion", "updaterId");
		
		// 4. 신청자에게 반려 처리 메일 전송 
		String title = MessageUtil.getMessage(SysMessageConstants.USER_REJECT, "Request request was rejected!");
		this.sendCommonRejectedEmail(title, request, account);
		
		// 5. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.REQUEST_PROCESSED_REJECTED, "Your request has been processed 'rejected'.");
	}

	/****************************************************************************************
	 * 									패스워드 초기화 신청 및 처리 API
	 ****************************************************************************************/

	@RequestMapping(value = "/password/reset/request", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiDesc(description = "Request reset password")
	public String requestPasswordReset(@RequestBody CheckPassword checkPass) {
		// 1. Validation
		AssertUtil.assertNotEmpty("terms.label.login", checkPass.getLogin());
		User account = this.checkExistUser(checkPass.getLogin());
		
		if(!account.getActiveFlag()) {
			// Already inactive account.
			throw ThrowUtil.newAlreadyDeactivatedAccount();
		}

		// 2. 권한 요청 생성
		RequestAuth request = new RequestAuth(account.getDomainId(),account.getLogin(), account.getEmail(), account.getName(), RequestAuth.TYPE_PASSWORD);
		request = this.checkAndCreate(request);
		
		// 3. 관리자에게 메일 전송
		String title = MessageUtil.getMessage(SysMessageConstants.USER_REQUEST_INIT_PASS, "Request reset password");
		this.sendCommonRequestEmail(title, request, account);

		// 4. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.REQUEST_SENT_TO_ADMIN, "Your request was sent to administrator.");
	}
	
	@RequestMapping(value = "password/reset/approve/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Approve reset password")
	public String approvePasswordReset(@PathVariable("id") String id, @RequestBody RequestAuth content) {
		// 1. Validation
		RequestAuth request = this.checkRequestAuth(id, RequestAuth.TYPE_PASSWORD, MessageUtil.getMessage(SysMessageConstants.NOT_A_PASSWORD_INIT_REQUEST, "This request is not for a password initialization."));
		
		// 2. 패스워드 초기화 처리  
		User account = User.getUserById(request.getRequesterId());
		String password = SecurityUtil.newPass();
		account.setEncryptedPassword(SecurityUtil.encodePassword(password));
		account.setResetPasswordSentAt(new Date());
		account.setResetPasswordToken(SecurityUtil.encodePassword(password));
		this.queryManager.update(account, "encryptedPassword", "resetPasswordSentAt", "resetPasswordToken");

		// 3. 요청 처리 
		request.setStatus(RequestAuth.STATUS_COMPLETED);
		request.setOpinion(content.getOpinion());
		this.queryManager.update(request, "status", "opinion", "updaterId");
		
		// 4. 신청자에게 처리 메일 전송 
		String title = MessageUtil.getMessage(SysMessageConstants.USER_COMPLETE_INIT_PASS, "Completed reset password");
		String link = SettingUtil.getValue(SysConfigConstants.CLIENT_CONTEXT_PATH, "http://factory.hatiolab.com");
		Map<String, Object> templateParams = ValueUtil.newMap("requestAuth,account,approver,title,link,password", request, account, User.currentUser(), title, link, password);
		RequestAuthController ctrl = BeanUtil.get(RequestAuthController.class);
		ctrl.sendMailToRequester(SysConfigConstants.MAIL_TEMPLATE_ACCOUNT_INIT_PASSWORD_RESULT, templateParams);
		
		// 5. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.RESULT_SENT_TO_REQUESTER, "Results of processing your request has been sent.");
	}
	
	@RequestMapping(value = "password/reset/reject/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Reject reset password")
	public String rejectPasswordReset(@PathVariable("id") String id, @RequestBody RequestAuth content) {
		// 1. Validation
		RequestAuth request = this.checkRequestAuth(id, RequestAuth.TYPE_PASSWORD, MessageUtil.getMessage(SysMessageConstants.NOT_A_PASSWORD_INIT_REQUEST, "This request is not for a password initialization."));
		
		// 2. 요청 처리 
		User account = User.getUserById(request.getRequesterId());
		request.setStatus(RequestAuth.STATUS_REJECTED);
		request.setOpinion(content.getOpinion());
		this.queryManager.update(request, "status", "opinion", "updaterId");
		
		// 3. 신청자에게 반려 메일 전송 
		String title = MessageUtil.getMessage(SysMessageConstants.USER_REJECT_INIT_PASS, "Reject reset password request");
		this.sendCommonRejectedEmail(title, request, account);
		
		// 4. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.RESULT_SENT_TO_REQUESTER, "Results of processing your request has been sent.");
	}
	
	/****************************************************************************************
	 * 									계정 활성화 신청 및 처리 API
	 ****************************************************************************************/	
	
	@RequestMapping(value = "/account/activate/request", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Request account activation")
	public String requestAccountActivation(@RequestBody CheckPassword checkPass) {
		// 1. Validation
		AssertUtil.assertNotEmpty("terms.label.login", checkPass.getLogin());
		User account = this.checkExistUser(checkPass.getLogin());
		
		// Already activated account.
		if (account.getActiveFlag())
			throw ThrowUtil.newAlreadyActivatedAccount();
		
		// Not Equal Password.
		if (ValueUtil.isNotEqual(account.getEncryptedPassword(), checkPass.getCurrentPass()))
			throw ThrowUtil.invalidIdOrPass();
		
		// 2. 권한 요청 생성
		RequestAuth request = new RequestAuth(account.getDomainId(), account.getLogin(),  account.getEmail(), account.getName(), RequestAuth.TYPE_ACTIVATION);
		request = this.checkAndCreate(request);
		
		// 3. 관리자에게 메일 전송 
		String title = MessageUtil.getMessage(SysMessageConstants.USER_REQUEST_ACTIVE_ACCOUNT, "Request account activation");
		this.sendCommonRequestEmail(title, request, account);
		
		// 4. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.REQUEST_SENT_TO_ADMIN, "Your request was sent to administrator.");
	}
	
	@RequestMapping(value = "/account/activate/approve/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Approve account activation")
	public String approveAccountActivation(@PathVariable("id") String id, @RequestBody RequestAuth content) {
		// 1. Validation
		RequestAuth request = this.checkRequestAuth(id, RequestAuth.TYPE_ACTIVATION, MessageUtil.getMessage(SysMessageConstants.NOT_A_ACCOUNT_ACTIVATION_REQUEST, "This request is not for a account activation."));
		
		// 2. 계정 활성화 처리
		User account = User.getUserById(request.getRequesterId());
		
		if (account.getActiveFlag()) {
			// Already activated account.
			throw ThrowUtil.newAlreadyActivatedAccount();
		}
		
		account.setActiveFlag(true);
		this.queryManager.update(account, "activeFlag");

		// 3. 요청 처리 
		request.setStatus(RequestAuth.STATUS_COMPLETED);
		request.setOpinion(content.getOpinion());
		this.queryManager.update(request, "status", "opinion", "updaterId");

		// 4. 신청자에게 처리 내용 메일 전송 
		String title = MessageUtil.getMessage(SysMessageConstants.USER_COMPLETE_ACTIVE_ACCOUNT, "Complete account active");
		this.sendCommonApprovedEmail(title, request, account);
		
		// 5. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.RESULT_SENT_TO_REQUESTER, "Results of processing your request has been sent.");
	}
	
	@RequestMapping(value = "/account/activate/reject/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Reject account activation")
	public Object rejectAccountActivation(@PathVariable("id") String id, @RequestBody RequestAuth content) {
		// 1. Validation
		RequestAuth request = this.checkRequestAuth(id, RequestAuth.TYPE_ACTIVATION, MessageUtil.getMessage(SysMessageConstants.NOT_A_ACCOUNT_ACTIVATION_REQUEST, "This request is not for a account activation."));
		User account = User.getUserById(request.getRequesterId());
		
		if (account.getActiveFlag()) {
			// Already activated account.
			throw ThrowUtil.newAlreadyActivatedAccount();
		}

		// 2. 요청 처리 
		request.setStatus(RequestAuth.STATUS_REJECTED);
		request.setOpinion(content.getOpinion());
		this.queryManager.update(request, "status", "opinion", "updaterId");
		
		// 3. 신청자에게 반려 메일 전송
		String title = MessageUtil.getMessage(SysMessageConstants.USER_INACTIVE_ACCOUNT, "User not activated");
		this.sendCommonRejectedEmail(title, request, account);
		
		// 4. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.RESULT_SENT_TO_REQUESTER, "Results of processing your request has been sent.");
	}
	
	/****************************************************************************************
	 * 									권한 (역할) 신청 및 처리 API
	 ****************************************************************************************/
	
	@RequestMapping(value = "/account/auth/request", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Request account authorization")
	public String requestAccountAuthorization(@RequestBody RequestAuth requestAuth) {
		// 1. Validation 
		AssertUtil.assertNotEmpty("terms.label.role", requestAuth.getRoleId());
		Role role = this.getOne(true, Role.class, requestAuth.getRoleId());
		User account = User.currentUser();
		String sql = "select id from users_roles where user_id = :userId and role_id = :roleId";
		int count = this.queryManager.selectSizeBySql(sql, ValueUtil.newMap("userId,roleId", account.getId(), role.getId()));
		
		if(count > 0) {
			// You already have the authorization.
			throw ThrowUtil.newAlreadyHaveAuthroization(); 
		}
		
		// 2. 요청 처리 
		Long domainId = Domain.currentDomain().getId();
		requestAuth.setDomainId(domainId);
		requestAuth.setRequesterId(account.getId());
		requestAuth.setRequesterEmail(account.getEmail());
		requestAuth.setRequesterName(account.getName());
		requestAuth.setStatus(RequestAuth.STATUS_WAIT);
		requestAuth.setRequestType(RequestAuth.TYPE_ROLE);
		requestAuth = this.checkAndCreate(requestAuth);
		
		// 3. 관리자에게 신청 메일 전송 
		String title = MessageUtil.getMessage(SysMessageConstants.USER_REQUEST_ADD_AUTH, "System access authorization request");
		this.sendCommonRequestEmail(title, requestAuth, account);
		
		// 4. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.REQUEST_SENT_TO_ADMIN, "Your request has been sent to administrator.");
	}	
	
	@RequestMapping(value = "/account/auth/approve/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Approve account authorization")
	public String approveAccountAuthorization(@PathVariable("id") String id, @RequestBody RequestAuth content) {
		// 1. Validation
		RequestAuth request = this.checkRequestAuth(id, RequestAuth.TYPE_ROLE, MessageUtil.getMessage(SysMessageConstants.NOT_A_AUTHORIZATION_REQUEST, "This request is not for a authorization."));
		
		// 2. 역할 조회 
		Role role = this.getOne(true, Role.class, request.getRoleId());
		User account = User.getUserById(request.getRequesterId());
		String sql = "select id from users_roles where user_id = :userId and role_id = :roleId";
		int count = this.queryManager.selectSizeBySql(sql, ValueUtil.newMap("userId,roleId", account.getId(), role.getId()));
		
		if(count > 0) {
			// You already have the authorization.
			throw ThrowUtil.newAlreadyHaveAuthroization();
		}
		
		// 3. 권한 추가 처리 
		UsersRole ur = new UsersRole(account.getId(), role.getId());
		this.queryManager.insert(ur);
		
		// 4. 요청 처리 
		request.setStatus(RequestAuth.STATUS_COMPLETED);
		request.setOpinion(content.getOpinion());
		this.queryManager.update(request, "status", "opinion", "updaterId");
		
		// 5. 신청자에게 완료 메일 전송 
		String title = MessageUtil.getMessage(SysMessageConstants.USER_COMPLETE_ADD_AUTH, "Approved system access authorization request");
		this.sendCommonApprovedEmail(title, request, account);
		
		// 6. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.RESULT_SENT_TO_REQUESTER, "Results of processing your request has been sent.");
	}
	
	@RequestMapping(value = "/account/auth/reject/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Reject account authorization")
	public String rejectAccountAuthorization(@PathVariable("id") String id, @RequestBody RequestAuth content) {
		// 1. Validation
		RequestAuth request = this.checkRequestAuth(id, RequestAuth.TYPE_ROLE, MessageUtil.getMessage(SysMessageConstants.NOT_A_AUTHORIZATION_REQUEST, "This request is not for a authorization."));
		
		// 2. 권한 조회 
		Role role = this.getOne(true, Role.class, request.getRoleId());
		User account = User.getUserById(request.getRequesterId());
		String sql = "select id from users_roles where user_id = :userId and role_id = :roleId";
		int count = this.queryManager.selectSizeBySql(sql, ValueUtil.newMap("userId,roleId", account.getId(), role.getId()));
		
		if(count > 0) {
			// You already have the authorization.
			throw ThrowUtil.newAlreadyHaveAuthroization();
		}
		
		// 3. 요청 처리 
		request.setStatus(RequestAuth.STATUS_REJECTED);
		request.setOpinion(content.getOpinion());
		this.queryManager.update(request, "status", "opinion", "updaterId");
		
		// 4. 신청자에게 반려 메일 전송  
		String title = MessageUtil.getMessage(SysMessageConstants.USER_REJECT_ADD_AUTH, "Rejected system access authorization request");
		this.sendCommonRejectedEmail(title, request, account);
		
		// 5. 결과 메시지 리턴 
		return MessageUtil.getMessage(SysMessageConstants.RESULT_SENT_TO_REQUESTER, "Results of processing your request has been sent.");		
	}
	
	/****************************************************************************************
	 * 									Private Methods
	 ****************************************************************************************/	
	
	/**
	 * 사용자 프로세스 공통 신청 이메일 전송
	 * 
	 * @param title
	 * @param request
	 * @param requester
	 */
	public void sendCommonRequestEmail(String title, RequestAuth request, User requester) {
		String link = SettingUtil.getValue(SysConfigConstants.CLIENT_CONTEXT_PATH, "http://factory.hatiolab.com") + "/#!/request_auths";
		Map<String, Object> templateParams = ValueUtil.newMap("requestAuth,account,title,link", request, requester, title, link);
		RequestAuthController ctrl = BeanUtil.get(RequestAuthController.class);
		ctrl.sendMailToAdmin(SysConfigConstants.MAIL_TEMPLATE_ACCOUNT_COMMON_REQUEST, templateParams);
	}
	
	/**
	 * 사용자 프로세스 공통 승인 이메일 전송 
	 * 
	 * @param title
	 * @param request
	 * @param requester
	 */
	public void sendCommonApprovedEmail(String title, RequestAuth request, User requester) {
		String link = SettingUtil.getValue(SysConfigConstants.CLIENT_CONTEXT_PATH, "http://factory.hatiolab.com");
		Map<String, Object> templateParams = ValueUtil.newMap("requestAuth,account,approver,title,link", request, requester, User.currentUser(), title, link);
		RequestAuthController ctrl = BeanUtil.get(RequestAuthController.class);
		ctrl.sendMailToRequester(SysConfigConstants.MAIL_TEMPLATE_ACCOUNT_COMMON_APPROVED, templateParams);		
	}
	
	/**
	 * 사용자 프로세스 공통 반려 이메일 전송
	 * 
	 * @param title
	 * @param request
	 * @param requester
	 */
	public void sendCommonRejectedEmail(String title, RequestAuth request, User requester) {
		String link = SettingUtil.getValue(SysConfigConstants.CLIENT_CONTEXT_PATH, "http://factory.hatiolab.com");
		Map<String, Object> templateParams = ValueUtil.newMap("requestAuth,account,title,link,rejecter", request, requester, title, link, User.currentUser());
		RequestAuthController ctrl = BeanUtil.get(RequestAuthController.class);
		ctrl.sendMailToRequester(SysConfigConstants.MAIL_TEMPLATE_ACCOUNT_COMMON_REJECTED, templateParams);		
	}
	
	/**
	 * Send mail to admin
	 * 
	 * @param templatePath
	 * @param templateParams
	 */
	@Async
	public void sendMailToAdmin(String templatePath, Map<String, Object> templateParams) {
		boolean isEnable = ValueUtil.toBoolean(SettingUtil.getValue(SecConfigConstants.USER_MAIL_REQUEST_AUTH_ENABLE), true);
		if (!isEnable)
			return;
		
		templateParams.put("processedAt", DateUtil.currentTimeStr());
		String title = (String)templateParams.get("title");
		User account = (User)templateParams.get("account");
		Domain domain = this.queryManager.select(Domain.class, account.getDomainId());
		templateParams.put("domain", domain);
		String content = this.convertTemplate(templatePath, templateParams);
		this.logger.info(content);
		String to = this.getAdminEmail(domain);
		
		if(ValueUtil.isNotEmpty(to)) {
			this.mailSender.send(title, null, to, content, templateParams, ValueUtil.newMap("mimeType", "text/html"));
		}
	}
	
	/**
	 * domain의 관리자 이메일을 모두 조회하여 리턴 
	 * 
	 * @param domain
	 * @return
	 */
	private String getAdminEmail(Domain domain) {
		String sql = "select email from users where domain_id = :domainId and admin_flag = :adminFlag";
		List<String> emailList = this.queryManager.selectListBySql(sql, ValueUtil.newMap("domainId,adminFlag", domain.getId(), true), String.class, 0, 0);
		StringJoiner joiner = new StringJoiner(OrmConstants.COMMA);
		for(String email : emailList) {
			joiner.add(email);
		}
		
		return joiner.length() > 0 ? joiner.toString() : null;
	}
	
	/**
	 * Send mail to requester
	 * 
	 * @param templatePath
	 * @param templateParams
	 */
	@Async
	public void sendMailToRequester(String templatePath, Map<String, Object> templateParams) {
		templateParams.put("processedAt", DateUtil.currentTimeStr());
		String title = (String)templateParams.get("title");
		User account = (User)templateParams.get("account");
		Domain domain = this.queryManager.select(Domain.class, account.getDomainId());
		templateParams.put("domain", domain);
		String content = this.convertTemplate(templatePath, templateParams);
		this.logger.info(content);
		
		if(ValueUtil.isNotEmpty(account.getEmail())) {
			this.mailSender.send(title, null, account.getEmail(), content, templateParams, ValueUtil.newMap("mimeType", "text/html"));
		}
	}
	
	/**
	 * translate template
	 * 
	 * @param templatePath
	 * @param templateParams
	 * @return
	 */
	private String convertTemplate(String templatePath, Map<String, Object> templateParams) {
		templatePath = this.makeTemplatePath(templatePath);
		String template = FileUtil.readClassPathResource(templatePath);
		StringWriter writer = new StringWriter();
		this.templateEngine.processTemplate(template, writer, templateParams, null);
		return writer.toString();
	}
	
	/**
	 * 메일 templatePath를 완성하여 리턴 
	 * 
	 * @param templatePath
	 * @return
	 */
	private String makeTemplatePath(String templatePath) {
		templatePath = SysConstants.MAIL_TEMPLATE_PATH_PREFIX + templatePath; 
		templatePath = templatePath.replace(OrmConstants.DOT, OrmConstants.SLASH);
		templatePath += SysConstants.MAIL_TEMPLATE_PATH_SUFFIX;
		return templatePath;
	}
	
	/**
	 * Check Request Authorization
	 * 
	 * @param id
	 * @param requestType
	 * @param message
	 * @return
	 */
	private RequestAuth checkRequestAuth(String id, String requestType, String message) {
		RequestAuth request = this.findOne(id);
		
		if(ValueUtil.isNotEqual(request.getRequestType(), requestType)) {
			throw new ElidomServiceException(message);
		}
		
		if(ValueUtil.isNotEqual(request.getStatus(), RequestAuth.STATUS_WAIT)) {
			throw ThrowUtil.newAlreadyProcessedRequest(message);
		}		
		
		return request;
	}
	
	/**
	 * 사용자가 존재하지 않는지 체크 
	 * 
	 * @param login
	 * @return
	 */
	private User checkExistUser(String login) {
		User account = User.getUserByLoginId(login);
		
		if(account == null) {
			throw new ElidomRecordNotFoundException(SysMessageConstants.USER_NOT_EXIST, " User does not exist");
		}

		return account;
	}
	
	/**
	 * 사용자가 존재하지 않는지 체크 
	 * 
	 * @param login
	 * @return
	 */
	private void checkNotExistUser(String login) {
		if(User.getUserByLoginId(login) != null) {
			throw new ElidomAlreadyExistException(SysMessageConstants.USER_ALREADY_EXIST, "User already exist");
		}
	}
	
	/**
	 * 이미 접수된 요청인지 체크한 후 요청 접수 
	 * 
	 * @param request
	 * @return
	 */
	private RequestAuth checkAndCreate(RequestAuth request) {
		Map<String, Object> paramsMap = ValueUtil.newMap("domainId,requesterId,requestType,status", request.getDomainId(), request.getRequesterId(), request.getRequestType(), RequestAuth.STATUS_WAIT);
		String sql = "select id from request_auths where domain_id = :domainId and requester_id = :requesterId and request_type = :requestType and status = :status";
		
		if(ValueUtil.isEqual(RequestAuth.TYPE_ROLE, request.getRequestType())) {
			sql += " and role_id = :roleId";
			paramsMap.put("roleId", request.getRoleId());
		}
		
		int count = this.queryManager.selectSizeBySql(sql, paramsMap);
		if(count > 0) {
			// This request already has been received.
			throw ThrowUtil.newAlreadyReceivedRequest();
		}
		
		return this.create(request);
	}

}