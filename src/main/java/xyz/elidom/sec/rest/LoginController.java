/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.rest;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sec.SecConfigConstants;
import xyz.elidom.sec.SecConstants;
import xyz.elidom.sec.util.SecurityUtil;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.entity.relation.DomainRef;
import xyz.elidom.sys.system.auth.AuthProviderFactory;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SessionUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.DateUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@ServiceDesc(description = "Login Service API")
@RequestMapping("/rest")
public class LoginController {

	/**
	 * logger
	 */
	protected Logger logger = LoggerFactory.getLogger(LoginController.class);
	/**
	 * securityContextRepository
	 */
	private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	/**
	 * 쿼리 매니저
	 */
	@Autowired
	private IQueryManager queryManager;
	/**
	 * 로그인 이력 컨트롤러
	 */
	@Autowired
	private LoginHistoryController loginHistoryController;
	/**
	 * 인증 정보 프로바이더
	 */
	@Autowired
	private AuthProviderFactory authProviderFactory;
	/**
	 * 사용자 
	 */
	@Autowired
	private UserDetailsService userDetailsService;
	/**
	 * Authentication Manager
	 */
	private AuthenticationManager authManager;
	
	/**
	 * 사용자 ID/Password 정보로 사용자 체크 후 토큰 발급
	 * 
	 * @param req
	 * @param res
	 * @param login
	 * @param password
	 * @param requesterId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/check_user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check User")
	public Map<String, Object> checkUser(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestParam(name = "email") String login, 
			@RequestParam(name = "password") String password,
			@RequestParam(name = "requester_id") String requesterId) {

		Domain domain = this.findDomainByRequest(req);;
		
		String clientType = (String)SessionUtil.getAttribute("CLIENT_TYPE");
		if(clientType != null && ValueUtil.isEqual(clientType, "MOBILE")) {
			domain = Domain.systemDomain();
		} else {
			domain = this.findDomainByRequest(req);
		}
		
		// 1. 로그인 한 도메인이 사이트 도메인이면 /rest/login URL로 요청하라고 상태를 알려줌
		if(domain != null && !domain.getSystemFlag()) {
			return ValueUtil.newMap("status", "site_domain");
		}
		
		// 3. 로그인 정보로 부터 사용자 ID를 조회
		String ipAddr = AnyValueUtil.getRemoteIp(req);
		User user = this.getUserForAuth(domain, login, ipAddr);
		
		// 4. 계정 상태 체크, null이면 OK, 나머지는 추가 프로세스 필요
		Map<String, Object> userInfo = this.checkValidAccount(domain, user, password, ipAddr);
		String accountStatus = (String)userInfo.get("status");
		
		// 5. 로그인 가능 상태
		if(ValueUtil.isEqual(SysConstants.OK_STRING, accountStatus)) {
			// 5.1 사이트 권한
			List<Map> siteList = this.searchSiteList(user);
			
			// 5.2 사이트 접근 권한이 없는 경우 에러 발생
			if(ValueUtil.isEmpty(siteList)) {
				throw new ElidomRuntimeException(MessageUtil.getMessage("NO_SITE_PERMISSION"));
			} else {
				userInfo.put("site_list", siteList);
			}
		} 
		
		return userInfo;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/site_list_by_user/{requester_id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Site List by User")
	public List<?> siteList(HttpServletRequest req, @PathVariable("requester_id") String requesterId) {
		// 1. 현재 도메인 & 사용자 추출
		Domain currentDomain = Domain.currentDomain();
		User currentUser = User.currentUser();
		
		// 2. 사용자가 방문 권한이 있는 사이트 리스트 조회
		List<Map> siteList = this.searchSiteList(currentUser);
		
		for(Map site : siteList) {
			String domainId = AnyValueUtil.getMapData(site, "id").toString();
			site.put("current_domain", ValueUtil.isEqualIgnoreCase(domainId, currentDomain.getId().toString()));
		}
		
		// 5. 결과 리턴
		return siteList;
	}
	
	/**
	 * siteList 조회
	 * 
	 * @param serverName
	 * @param currentUserId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List<Map> searchSiteList(User currentUser) {
		String sql = "select id, name, brand_name, description, subdomain from domains where id in (select domain_id from domain_users where user_id = :userId)";
		
		if(currentUser.getSuperUser()) {
			sql = "select id, name, brand_name, description, subdomain from domains"; 
		}
		
		Map<String, Object> params = ValueUtil.newMap("userId,systemFlag", currentUser.getId(), true);
		return this.queryManager.selectListBySql(sql, params, Map.class, 0, 0);
	}
	
	/**
	 * Request로 부터 브라우저에서 접근한 도메인으로 부터 매핑된 도메인(사이트)를 찾아냄
	 * 
	 * @param req
	 * @return
	 */
	public Domain findDomainByRequest(HttpServletRequest req) {
		Domain domain = (Domain)SessionUtil.getAttribute(SysConstants.CURRENT_DOMAIN);
		// 없다면 에러
		if(domain == null) {
			throw ThrowUtil.newDomainNotExist(AnyValueUtil.getClientRequestSubDomain(req));
		}
	
		return domain;
	}
	
	/**
	 * 1. login이 base64로 인코딩 되어 있다고 추정하여 decoding한 뒤 사용자 조회 시도
	 * 2. 조회 중 에러 발생하면 decoding 하지 않은 상태로 사용자 조회 시도
	 * 3. 조회 중 에러 발생하면 로그인 정보 불일치로 판단하여 에러 발생
	 * 
	 * @param domain
	 * @param login
	 * @param ipAddr
	 * @return
	 */
	private User getUserForAuth(Domain domain, String login, String ipAddr) {
		try {
			return this.findUserForAuth(domain, this.decodeLogin(login), ipAddr);
		} catch (ElidomRuntimeException e) {
			return this.findUserForAuth(domain, login, ipAddr);
		}
	}
	
	/**
	 * base64 decoding login parameter 
	 * @param login
	 * @return
	 */
	private String decodeLogin(String login) {
		return new String(Base64.decodeBase64(login));
	}
			
	/**
	 * 인증을 위해 login 정보로 사용자 조회
	 * 
	 * @param domain
	 * @param login
	 * @param ipAddr
	 * @return
	 */
	private User findUserForAuth(Domain domain, String login, String ipAddr) {
		String userId = this.authProviderFactory.getAuthProvider().loginToUserId(login);
		User user = this.queryManager.select(User.class, userId);
		
		if(user == null) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("USER_INVALID_ID_OR_PASS"));
		}
		
		return user;
	}
	
	/**
	 * 사용자 정보가 유효한 지 체크
	 * 
	 * @param domain
	 * @param user
	 * @param password
	 * @param ipAddress
	 * @return 계정 상태
	 */
	private Map<String, Object> checkValidAccount(Domain domain, User user, String password, String ipAddress) {
		// 1. 사용자 활성화 상태 체크
		if(!user.getActiveFlag()) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("ACCOUNT_LOCKED"));
		}
		
		// 2. 비밀번호 체크
		boolean validUser = false;
		if(user != null) {
			String encPasswd = user.getEncryptedPassword();
			validUser = ValueUtil.isEqual(encPasswd, password);
			
			if(!validUser) {
				String encodePass = SecurityUtil.encodePassword(password);
				validUser = ValueUtil.isEqual(encPasswd, encodePass);
			}
		}
		
		// 3. 비밀번호가 맞지 않으면 로그인 실패 이력 기록
		if(!validUser) {
			this.loginHistoryController.saveLoginFailHistory(domain.getId(), user.getId(), ipAddress);
			this.loginHistoryController.doLoginFailLock(user);			
			throw new ElidomRuntimeException(MessageUtil.getMessage("USER_INVALID_ID_OR_PASS"));
		}
		
		// 4. 계정 일시 잠금 체크
		if(!this.isAccountNonLocked(user)) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("ACCOUNT_TEMPORARY_LOCKED"));
		}
		
		// 5. 계정 상태 체크, null이면 OK, 나머지는 추가 프로세스 필요
		String accountStatus = this.isCredentialsNonExpired(user);
		
		// 6. 계정 정보 및 상태 리턴
		return ValueUtil.newMap(
				"id,name,locale,timezone,account_type,super_user,admin_flag,status", 
				user.getId(), 
				user.getName(), 
				user.getLocale(), 
				user.getTimezone(), 
				user.getAccountType(), 
				user.getSuperUser(), 
				user.getAdminFlag(),
				(accountStatus == null ? SysConstants.OK_STRING : accountStatus));
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Login")
	public Map<String, Object> login(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestParam(name = "email") String login, 
			@RequestParam(name = "password") String password,
			@RequestParam(name = "domainId", required = false) Long domainId) {
		
		Domain domain = null;
		
		// operator 로그인은 domainId 필수 
		if(ValueUtil.isNotEmpty(domainId)) {
			domain = Domain.find(domainId);
			SecurityContextHolder.clearContext();
		} else {
			// 클라이언트 접근 URL로 부터 도메인을 조회
			domain = this.findDomainByRequest(req);
		}

		// 로그인 정보로 사용자 정보 체크
		String ipAddr = AnyValueUtil.getRemoteIp(req);
		User user = this.getUserForAuth(domain, login, ipAddr);
		
		// 사용자 정보가 유효한 지 체크
		Map<String, Object> userInfo = this.checkValidAccount(domain, user, password, ipAddr);
		String accountStatus = (String)userInfo.get("status");
		
		// 계정 상태가 OK가 아니면 사용자 정보 리턴
		if(ValueUtil.isNotEqual(accountStatus, SysConstants.OK_STRING)) {
			return userInfo;
		// 계정 상태가 OK이면 인증 실행
		} else {
			// 세션에 도메인, 접속 IP 설정
			SessionUtil.setAttribute(SecConstants.CURRENT_DOMAIN, domain);
			SessionUtil.setAttribute("ACCESS_IP", ipAddr);
			
			// operator 로그인은 domainId 필수 
			if(ValueUtil.isNotEmpty(domainId)) {
				SessionUtil.setAttribute("CLIENT_TYPE", "MOBILE");
			} else {
				SessionUtil.setAttribute("CLIENT_TYPE", "MANAGER");
			}
			
			// 인증 실행
			this.doAuthenticate(user.getId(), password, req, res);

			// 세션 정보 리턴
			Map<String, Object> sessionInfo = this.currentSession(user, domain);
			sessionInfo.put(SecConstants.ACCOUNT_STATUS, accountStatus);
			return sessionInfo;
		}
	}
	
	@RequestMapping(value = "/session_info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find Session Information")
	public Map<String, Object> sessionInfo() {
		Domain currentDomain = Domain.currentDomain();
		User currentUser = SecurityUtil.getAuthenticatedUser();		
		return this.currentSession(currentUser, currentDomain);
	}
	

	@RequestMapping(value = "/check_auth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check User Auth Domain List")
	public Map<String, Object> checkAuth(HttpServletRequest req) {
		Domain currentDomain = Domain.currentDomain();
		User currentUser = SecurityUtil.getAuthenticatedUser();		
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		// 1. subDomain 이 없을때  ( system domain 으로 route 용 url 일때 ) 
		if(ValueUtil.isEqual(AnyValueUtil.getClientRequestSubDomain(req), "_ROOT_")) {
			String qry = "SELECT * FROM DOMAINS WHERE ID in (SELECT DOMAIN_ID FROM DOMAIN_USERS WHERE USER_ID = :userId) AND SYSTEM_FLAG = :systemFlag";

			List<Domain> domainList = this.queryManager.selectListBySql(qry, ValueUtil.newMap("userId,systemFlag", currentUser.getId(), false), Domain.class, 0, 0);
			// 1.1. 사용자가 super user 이면 system domain 접근 권한 있음 
			if(currentUser.getSuperUser()) domainList.add(currentDomain);
			
			retMap.put("result", "SITE_LIST");
			retMap.put("site_list", domainList);
			
		} else {
			// 2. subDomain 이 있을때   
			String qry = "SELECT count(1) FROM DOMAIN_USERS WHERE USER_ID = :userId AND DOMAIN_ID = :domainId ";
			
			int userDomainCheck = this.queryManager.selectBySql(qry, ValueUtil.newMap("userId,domainId", currentUser.getId(), currentDomain.getId()), Integer.class);
			// 도메인 정보가 없으면 ... 사용할 수 없는 도메인 
			if(userDomainCheck == 0) {
				// 2.1. system domain + super user 는 pass 
				if(currentUser.getSuperUser() && currentDomain.getSystemFlag()) retMap.put("result", "OK");
				else {
					SessionUtil.removeAttribute(SysConstants.CURRENT_DOMAIN);
					retMap.put("result", "USER_DOMAIN_NOT_AUTH");
				}
			} else {
				retMap.put("result", "OK");
			}
		}
		
		return retMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> currentSession(User currentUser, Domain domain) {
		return (Map<String, Object>) this.authProviderFactory.getAuthProvider().sessionUserInfo(domain, currentUser);
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	@ApiDesc(description = "Logout")
	public boolean logout(HttpServletRequest req, HttpServletResponse res) {
		this.loginHistoryController.updateLogOutInfo();
		SecurityContextHolder.clearContext();

		if (req != null) {
			HttpSession session = req.getSession();
			if (session != null) {
				session.invalidate();
			}
		}

		return true;
	}

	/**
	 * Login 사용자에 대한 인증 실행
	 * 
	 * @param id
	 * @param password
	 * @param request
	 * @param response
	 * @return
	 */
	public Authentication doAuthenticate(String id, String password, HttpServletRequest request, HttpServletResponse response) {
		// 세션에서 현재 도메인 추출
		Authentication authResult = null;
		Domain domain = (Domain)SessionUtil.getAttribute(SecConstants.CURRENT_DOMAIN);
		String accessIp = (String)SessionUtil.getAttribute("ACCESS_IP");
		this.authManager = this.getAuthenticationProvider();
		
		try {
			// 비밀번호 암호화를 하지 않고 인증 실행.
			authResult = this.authManager.authenticate(new UsernamePasswordAuthenticationToken(id, password));
			
		} catch (AuthenticationException ae) {
			try {
				// 비밀번호를 암호화 처리하여 인증 실행.
				String encodePass = SecurityUtil.encodePassword(password);
				authResult = this.authManager.authenticate(new UsernamePasswordAuthenticationToken(id, encodePass));
				
			} catch (BadCredentialsException bce) {
				User user = this.queryManager.select(User.class, id);
				
				// 사용자가 존재 할 경우 로그인 실행 이력 생성.
				if (user != null) {
					this.loginHistoryController.saveLoginFailHistory(domain.getId(), id, accessIp);
					this.loginHistoryController.doLoginFailLock(user);
				}
				
				throw bce;
			}
		}

		// 인증 정보를 Context에 저장
		SecurityContextHolder.getContext().setAuthentication(authResult);
		User user = SecurityUtil.getUser();
		DomainRef domainRef = user.getDomain();
		domainRef.setId(domain.getId());
		domainRef.setBrandName(domain.getBrandName());
		domainRef.setName(domain.getName());
		this.securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
		
		// 로그인 정보 업데이트
		Date currentDate = new Date();
		user.setLastSignInAt(ValueUtil.checkValue(user.getCurrentSignInAt(), currentDate));
		user.setCurrentSignInAt(currentDate);
		user.setAccountExpireDate(null);
		user.setDomainId(domain.getId());
		user.setUpdaterId(user.getId());
		this.queryManager.update(user, "lastSignInAt", "currentSignInAt", "accountExpireDate", "domainId", "updaterId", "updatedAt");
	
		// 로그인 이력 생성
		this.loginHistoryController.saveLoginSuccessHistory(domain.getId(), user.getId(), accessIp);
		
		// 인증 정보 리턴
		return authResult;
	}
	
	/**
	 * 계정이 잠겨 있지 않은지 체크
	 * 
	 * @param user
	 * @return
	 */
	private boolean isAccountNonLocked(User user) {
		String expireDate = user.getAccountExpireDate();
		if (ValueUtil.isEmpty(expireDate)) {
			return true;
		}

		int lockMinute = ValueUtil.toInteger(SettingUtil.getValue(SecConfigConstants.USER_PASS_LOCK_MINUTE), 0);
		if (lockMinute < 1) {
			return true;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(ValueUtil.toDate(expireDate));
		calendar.add(Calendar.MINUTE, lockMinute);

		Date lockPeriod = calendar.getTime();
		return new Date().compareTo(lockPeriod) > 0;
	}
	
	/**
	 * 비밀번호 만료 상태 확인.
	 * 
	 * @param user
	 * @return
	 */
	private String isCredentialsNonExpired(User user) {
		// 비밀번호 만료 날짜가 비어있는 경우 초기값 설정 (오늘 날짜 + 변경 주기)
		String expireDate = user.getPasswordExpireDate();

		// 비밀번호 만료 설정이 활성화 되어 있지 않을 경우 OK
		if (!ValueUtil.toBoolean(SettingUtil.getValue(SysConfigConstants.USER_PASSWORD_EXPIRE_ENABLE), false)) {
			return null;
		}

		// 비밀번호 만료 상태 Check
		String accountType = user.getAccountType();

		// 계정 타입이 User가 아니거나, 비어있지 않은 경우 OK
		if (!(ValueUtil.isEmpty(accountType) || ValueUtil.isEqual(accountType, SysConstants.ACCOUNT_TYPE_USER))) {
			return null;
		}

		// 기본 비밀번호와 동일한지 확인.
		boolean isDefaultPass = false;
		String defaultPass = SettingUtil.getValue(SysConfigConstants.SECURITY_INIT_PASS);
		if (ValueUtil.isNotEmpty(defaultPass)) {
			isDefaultPass = ValueUtil.isEqual(user.getEncryptedPassword(), SecurityUtil.encodePassword(defaultPass));
		}
		
		// 계정 상태
		String accountStatus = null;
		
		// 만료 날짜가 현재 날짜보다 작을 경우
		if (ValueUtil.isNotEmpty(expireDate) && !DateUtil.isBiggerThenCurrentDate(expireDate)) {
			accountStatus = isDefaultPass ? SecConstants.ACCOUNT_STATUS_PASSWORD_CHANGE : SecConstants.ACCOUNT_STATUS_PASSWORD_EXPIRED;
		}

		boolean isInitPass = ValueUtil.isNotEmpty(user.getResetPasswordToken());
		boolean isFirstLogin = 	ValueUtil.isEmpty(user.getLastSignInAt()) && 
								ValueUtil.isNotEmpty(user.getCreatorId()) && 
								ValueUtil.isEmpty(user.getPasswordExpireDate());

		// 비밀번호 초기화 요청 또는 최초 로그인 시.
		if (isInitPass || isFirstLogin || isDefaultPass) {
			return SecConstants.ACCOUNT_STATUS_PASSWORD_CHANGE;
		}

		// 최초 로그인이 아니고, 만료 날짜가 비어 있는 경우 비밀번호 만료 날짜 생성.
		if (!isFirstLogin && ValueUtil.isEmpty(expireDate)) {
			// 비밀번호 만료 날짜가 비어있는 경우 초기값 설정(오늘 날짜 + 변경 주기)
			String period = SettingUtil.getValue(SysConfigConstants.USER_PASSWORD_CHANGE_PERIOD_DAY, "90");
			expireDate = DateUtil.addDateToStr(new Date(), ValueUtil.toInteger(period));
			user.setPasswordExpireDate(expireDate);
			this.queryManager.update(user, "passwordExpireDate");
		}
		
		return accountStatus;
	}
	
	/**
	 * 스프링 인증 관리자 
	 * 
	 * @return
	 */
	private AuthenticationManager getAuthenticationProvider() {
		if(this.authManager == null) {
			DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
			daoAuthenticationProvider.setPasswordEncoder(this.authProviderFactory.getAuthProvider().getPasswordEncoder());
			daoAuthenticationProvider.setUserDetailsService(this.userDetailsService);
			List<AuthenticationProvider> list = ValueUtil.toList(daoAuthenticationProvider);
			this.authManager = new ProviderManager(list);
		}
		
		return this.authManager;
	}
}