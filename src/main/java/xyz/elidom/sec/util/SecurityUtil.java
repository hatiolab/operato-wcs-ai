/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import xyz.elidom.exception.client.ElidomUnauthorizedException;
import xyz.elidom.sec.model.ElidomUserDetails;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.auth.AuthProviderFactory;
import xyz.elidom.sys.util.SessionUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 보안 및 인증 관련 유틸리티 클래스 
 * 
 * @author shortstop
 */
public class SecurityUtil {
	
	public static final String PARAMNAME_USERNAME = "j_username";
	public static final String PARAMNAME_PASSWORD = "j_password";
	public static final String USERID_ANONYMOUS = "anonymous";
	
	/**
	 * annonymous 여부 리턴 
	 * 
	 * @return
	 */
	public static boolean isAnonymous() {
		Authentication auth = getAuthentication();
		return auth == null || auth instanceof AnonymousAuthenticationToken;
	}

	/**
	 * 인증정보 리턴 
	 * 
	 * @return
	 */
	public static Authentication getAuthentication() {
		SecurityContext sc = SecurityContextHolder.getContext();
		if(sc == null) {
			sc = (SecurityContext) SessionUtil.getAttribute("SPRING_SECURITY_CONTEXT");
		}
		
		return sc == null ? null : sc.getAuthentication();
	}

	/**
	 * 인증된 사용자의 정보 리턴 
	 * 
	 * @return
	 */
	public static User getUser() {
		Authentication auth = getAuthentication();
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return null;
		}

		ElidomUserDetails elidomUserDetails = (ElidomUserDetails) auth.getPrincipal();
		return elidomUserDetails.getUser();
	}
	
	/**
	 * 인증된 사용자의 정보 리턴
	 * 
	 * @return
	 * @throws ElidomUnauthorizedException
	 */
	public static User getAuthenticatedUser() {
		User user = SecurityUtil.getUser();
		if(user != null) {
			return user;
		} else {
			ElidomUnauthorizedException eue = new ElidomUnauthorizedException();
			eue.setWritable(false);
			throw eue;
		}
	}
	
	/**
	 * 비밀번호를 암호화하여 리턴 
	 * 
	 * @param value
	 * @return
	 */
	public static String encodePassword(String value) {
		return BeanUtil.get(AuthProviderFactory.class).getAuthProvider().encodePassword(value);
	}
	
	/**
	 * 비밀번호 일치 여부 확인
	 * 
	 * @param encPass : 암호화된 Pass
	 * @param rawPass : 평문의 Pass
	 * @return
	 */
	public static boolean isPasswordValid(String encPass, String rawPass) {
		return BeanUtil.get(AuthProviderFactory.class).getAuthProvider().isPasswordValid(encPass, rawPass);
	}
	
	/**
	 * 초기호 비밀번호 리턴 
	 * 
	 * @return
	 */
	public static String newPass() {
		return BeanUtil.get(AuthProviderFactory.class).getAuthProvider().newPass();
	}
	
	/**
	 * Random Password 생성
	 * 
	 * @return
	 */
	public static String randomPassword() {
		char[] initRandomChar = {
			'a', 'b', 'c', 'd', 'e', 'f', 
			'g', 'h', 'i', 'j', 'k', 'l', 
			'm', 'n', 'o', 'p', 'q', 'r', 
			's', 't', 'u', 'v', 'w', 'x', 
			'y', 'z', '0', '1', '2', '3', 
			'4', '5', '6', '7', '8', '9'
		};

		char[] randomChar = new char[6];
		
		for (int i = 0; i < 6; i++) {
			randomChar[i] += initRandomChar[(int) (Math.random() * initRandomChar.length)];
		}

		StringBuffer buf = new StringBuffer();
		for (char randChar : randomChar) {
			buf.append(randChar);
		}
		
		return buf.toString();
	}
}