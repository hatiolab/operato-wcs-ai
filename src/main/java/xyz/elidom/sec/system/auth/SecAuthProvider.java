/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.system.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;

import xyz.elidom.sec.SecConstants;
import xyz.elidom.sec.entity.LoginHistory;
import xyz.elidom.sec.entity.Role;
import xyz.elidom.sec.rest.LoginHistoryController;
import xyz.elidom.sec.util.SecurityUtil;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.auth.IAuthProvider;
import xyz.elidom.sys.util.SessionUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 인증을 사용하는 인증 기능 프로바이더
 * 
 * @author shortstop
 */
public class SecAuthProvider implements IAuthProvider {
	
	/**
	 * 패스워드 인코더
	 */
	private PasswordEncoder passwordEncoder;
	
	public SecAuthProvider(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
		
	@Override
	public int getProviderPriority() {
		return -1;
	}
	
	@Override
	public String loginToUserId(String login) {
		return login;
	}
	
	@Override
	public User currentUser() {
		User user = SecurityUtil.getUser();

		// 인증되지 않은 사용자는 annonymous 사용자로 설정
		if (user == null) {
			user = new User();
			user.setName("annonymous");
		}

		user.setLocale((String) SessionUtil.getAttribute(SysConstants.LOCALE));
		return user;
	}
	
	@Override
	public Object sessionUserInfo(Domain currentDomain, User user) {
		user.setDomainId(currentDomain.getId());
		Map<String, Object> sessionInfo = this.userToMap(user);
		Map<String, Object> domainInfo = this.domainToMap(currentDomain);
		sessionInfo.put("domain", domainInfo);

		List<Role> roles = Role.getRoles(user.getId());
		List<Map<String, Object>> roleList = new ArrayList<Map<String, Object>>(3);
		for (Role role : roles) {
			roleList.add(ValueUtil.newMap("id,name,description", role.getId(), role.getName(), role.getDescription()));
		}
		sessionInfo.put("roles", roleList);

		try {
			// 최종 로그인 정보 추출.
			LoginHistory lastLoginInfo = BeanUtil.get(LoginHistoryController.class).getLastLoginInfo(user.getId());
			sessionInfo.put(SecConstants.ACCOUNT_LOGIN_INFO, lastLoginInfo);
		} catch (Exception e) {
		}

		return sessionInfo;
	}

	@Override
	public String encodePassword(String defaultPass) {
		return this.passwordEncoder.encode(defaultPass);
	}

	@Override
	public boolean isPasswordValid(String encPass, String rawPass) {
		boolean result = ValueUtil.isEqual(encPass, rawPass);
		if (!result) {
			result = this.passwordEncoder.matches(rawPass, rawPass);
		}
		
		return result;
	}

	@Override
	public String newPass() {
		boolean useRandomPass = ValueUtil.toBoolean(SettingUtil.getValue(SysConfigConstants.SECURITY_USE_RANDOM_INIT_PASS, SysConstants.TRUE_STRING));
		return useRandomPass ? SecurityUtil.randomPassword() : SettingUtil.getValue(SysConfigConstants.SECURITY_INIT_PASS);
	}
	
	@Override
	public PasswordEncoder getPasswordEncoder() {
		return this.passwordEncoder;
	}

	/**
	 * 사용자 객체를 Map으로 변환
	 * 
	 * @param user 사용자 정보
	 * @return
	 */
	private Map<String, Object> userToMap(User user) {
		Map<String, Object> sessionInfo = ValueUtil.newMap(
				"id,domain_id,login,email,name,dept,division,locale,super_user,admin_flag,operator_flag,exclusive_role", 
				user.getId(),
				user.getDomainId(),
				user.getLogin(), 
				user.getEmail(), 
				user.getName(), 
				user.getDept(), 
				user.getDivision(), 
				user.getLocale(), 
				user.getSuperUser(), 
				user.getAdminFlag(), 
				user.getOperatorFlag(),
				user.getExclusiveRole());		
		return sessionInfo;
	}
	
	/**
	 * 도메인 객체를 Domain으로 변환 
	 * 
	 * @param domain
	 * @return
	 */
	private Map<String, Object> domainToMap(Domain domain) {
		return ValueUtil.newMap("id,name,brandName,theme", domain.getId(), domain.getName(), domain.getBrandName(), domain.getTheme());
	}
}