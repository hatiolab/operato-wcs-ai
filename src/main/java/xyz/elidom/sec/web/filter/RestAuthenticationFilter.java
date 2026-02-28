/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.web.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import xyz.anythings.sys.AnyConstants;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sec.SecConfigConstants;
import xyz.elidom.sec.SecConstants;
import xyz.elidom.sec.model.JwtTokenPayLoad;
import xyz.elidom.sec.rest.LoginController;
import xyz.elidom.sec.rest.PermitUrlController;
import xyz.elidom.sec.util.SecurityUtil;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.service.params.ErrorOutput;
import xyz.elidom.sys.util.EnvUtil;
import xyz.elidom.sys.util.SessionUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;

@Order(3)
@Component
// @WebFilter(urlPatterns = { "/rest/**" })
public class RestAuthenticationFilter extends AbstractSecurityWebApplicationInitializer implements Filter {
	/**
	 * logger
	 */
	protected Logger logger = LoggerFactory.getLogger(RestAuthenticationFilter.class);
	/**
	 * restful 기본 URL - '/rest'
	 */
	private static final String BASE_URL = "/rest";
	/**
	 * 헤더 키 x-locale
	 */
	private static final String HEADER_LOCALE = "x-locale";
	/**
	 * Content Type - 'application/json; charset=UTF-8'
	 */
	private static final String CONTENT_TYPE_JSON_UTF_8 = "application/json; charset=UTF-8";
	/**
	 * 인증 없이 들어올 수 있는 URL 리스트 - key : 도메인 ID, value : permitAllUrls
	 */
	private Map<Long, Set<String>> permitAllUrls = new ConcurrentHashMap<Long, Set<String>>();
	/**
	 * GET 방식만 인증 없이 들어올 수 있는 URL 리스트 - key : 도메인 ID, value : permitReadOnlyUrls
	 */
	private Map<Long, Set<String>> permitReadOnlyUrls = new ConcurrentHashMap<Long, Set<String>>();
	
	/**
	 * Things Factory JWT Secret Key
	 */                          
	@Value("${factory.secret.key:0xD58F835B69D207A76CC5F84a70a1D0d4C79dAC95}") 
	private String jwtSecretKey;
	/**
	 * Things Factory Access Token Key
	 */
	@Value("${factory.secret.accessTokenCookieKey:access_token}") 
	private String jwtTokenName;
	
	
	/**
	 * 인증하지 않고 접근할 수 있는 URL 리스트
	 * 
	 * @param domainId
	 * @return
	 */
	private Set<String> getAllPermitUrls(Long domainId) {
		if(this.permitAllUrls.isEmpty() || !this.permitAllUrls.containsKey(domainId) || this.permitAllUrls.get(domainId).isEmpty()) {
			Set<String> allPermitUrls = this.createPermitUrls(true, domainId);
			this.permitAllUrls.put(domainId, allPermitUrls);
		}

		return this.permitAllUrls.get(domainId);
	}

	/**
	 * 인증하지 않고 Read할 수 있는 URL 리스트
	 * 
	 * @param domainId
	 * @return
	 */
	private Set<String> getReadPermitUrls(Long domainId) {
		if(this.permitReadOnlyUrls.isEmpty() || !this.permitReadOnlyUrls.containsKey(domainId) || this.permitReadOnlyUrls.get(domainId).isEmpty()) {
			Set<String> permitReadOnlyUrls = this.createPermitUrls(false, domainId);
			this.permitReadOnlyUrls.put(domainId, permitReadOnlyUrls);
		}
		
		return permitReadOnlyUrls.get(domainId);
	}
	
	/**
	 * 도메인 별 Permit URL을 구성한다.
	 * 
	 * @param isPermitAll
	 * @param domainId
	 * @return
	 */
	private Set<String> createPermitUrls(boolean isPermitAll, Long domainId) {
		// 1. 기본 Permit URLs
		Set<String> permitUrls = isPermitAll ? this.getDefaultAllPermitURL() : this.getDefaultReadOnlyPermitURL();
		
		// 2. PermitUrl 관리 테이블에서 조회
		try {
			PermitUrlController permitCtrl = BeanUtil.get(PermitUrlController.class);
			List<String> dbPermitUrls = isPermitAll ? permitCtrl.listAllPermitURL(domainId) : permitCtrl.listReadOnlyURL(domainId);
			if(dbPermitUrls != null) permitUrls.addAll(dbPermitUrls);

		} catch (Exception e) {
			this.logger.error("Failed to set permit URLs!", e);
		}
		
		// 3. application.properties에서 PermitURL 조회
		String settingPermitUrls = 
				EnvUtil.getValue((isPermitAll ? SecConfigConstants.SECURITY_ALL_PERMIT_URI : SecConfigConstants.SECURITY_READ_ONLY_URI), OrmConstants.EMPTY_STRING);
			
		if(ValueUtil.isNotEmpty(settingPermitUrls)) {
			String[] settingPermitUrlArr = StringUtils.tokenizeToStringArray(settingPermitUrls, OrmConstants.COMMA);
			if (settingPermitUrlArr != null && settingPermitUrlArr.length > 0) {
				Collections.addAll(permitUrls, settingPermitUrlArr);
			}
		}

		permitUrls.removeAll(Collections.singleton(null));
		return permitUrls;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
				
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String uri = request.getRequestURI();
		String method = request.getMethod();
		
		// rest 로 시작되지 않는 요청은 skip
		if(uri.startsWith(BASE_URL) == false) {
			chain.doFilter(req, res);
			return;
		}
		
		// 클라이언트 접근 도메인으로 현재 도메인 정보 설정
		Domain currentDomain = null;
		
		// 미들웨어 서비스간 http 리퀘스트를 이용해 각각의 server 에 명령을 내려야 하는 경우 domain_id를 찾지 못해 문제 발생함. rabbitmq 서비스는 시스템 도메인으로 통일 
		if(uri.contains("rest/rabbitmq")) {
			currentDomain = Domain.systemDomain();
		} else {
			currentDomain = SessionUtil.setCurrentDomain();	
		}
		
		if (currentDomain != null && uri.startsWith(BASE_URL) && !RequestMethod.OPTIONS.name().equalsIgnoreCase(method)) {
			// 인증되지 않은 Session에 대한 처리
			if (SecurityUtil.isAnonymous() && !this.isPermitUri(uri, this.getAllPermitUrls(currentDomain.getId()))) {
				// Get방식이 아니거나, Read Permit URL에 포함되어 있지 않을 경우 인증 확인.
				if (!(RequestMethod.GET.name().equalsIgnoreCase(method) && this.isPermitUri(uri, this.getReadPermitUrls(currentDomain.getId())))) {
					// HttpSender를 통한 JSON 호출 시, 인증 실행.
					String authType = request.getHeader(SecConstants.AUTH_TYPE);
					String authKey = request.getHeader(SecConstants.AUTH_KEY);

					/**
					 * Type에 따른 인증 실행
					 */
					if (ValueUtil.isNotEmpty(authType) && ValueUtil.isNotEmpty(authKey)) {
						switch (authType) {
						case SecConstants.AUTH_TYPE_JSON :
							this.doJsonAuth(authKey, request, response);
							break;

						case SecConstants.AUTH_TYPE_TOKEN :
							this.doTokenAuth(authKey, request, response);
							break;
						}
					} else {
						this.doJwtTokenAuth(request, response);
					}
					
					// 인증되지 않았을 경우 Message 처리
					if (SecurityUtil.isAnonymous() || SecurityUtil.getAuthentication() == null) {
						this.processUnauthorized(request, response);
						return;
					}
				}
			}

			Object locale = ValueUtil.checkValue(request.getHeader(HEADER_LOCALE), SettingUtil.getValue(currentDomain.getId(), SysConstants.DEFAULT_LANGUAGE_KEY, SysConstants.LANG_EN));
			SessionUtil.setAttribute(SysConstants.LOCALE, locale);
			
		// PDF 다운로드 임시 코드 TODO 추후 삭제
		} else if(uri.contains("/stream/")) {
			SessionUtil.setAttribute(SysConstants.CURRENT_DOMAIN, Domain.findByName("first"));
			String locale = request.getHeader("x-locale");
			SessionUtil.setAttribute(SysConstants.LOCALE, locale);
			
			String authType = request.getHeader(SecConstants.AUTH_TYPE);
			String authKey = request.getHeader(SecConstants.AUTH_KEY);

			/**
			 * Type에 따른 인증 실행
			 */
			if (ValueUtil.isNotEmpty(authType) && ValueUtil.isNotEmpty(authKey)) {
				switch (authType) {
				case SecConstants.AUTH_TYPE_JSON :
					this.doJsonAuth(authKey, request, response);
					break;

				case SecConstants.AUTH_TYPE_TOKEN :
					this.doTokenAuth(authKey, request, response);
					break;
				}
			}
		}
		
		chain.doFilter(req, res);
	}

	/**
	 * 인증이 안 된 경우 처리
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void processUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ErrorOutput output = new ErrorOutput();
		output.setCode(SysMessageConstants.NOT_AUTHORIZED_USER);
		output.setMsg("Unauthorized user");
		output.setSuccess(false);
		output.setStatus(HttpStatus.UNAUTHORIZED.value());

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setCharacterEncoding(SysConstants.CHAR_SET_UTF8);
		response.setContentType(CONTENT_TYPE_JSON_UTF_8);

		PrintWriter writer = response.getWriter();
		writer.println(FormatUtil.toJsonString(output));
	}
	
	/**
	 * Check All permit URI.
	 * 
	 * @param uri
	 * @param permitUrls
	 * @return
	 */
	private boolean isPermitUri(String uri, Set<String> permitUrls) {
		StringBuilder sb = new StringBuilder();
		if (!uri.startsWith(OrmConstants.SLASH)) {
			sb.append(OrmConstants.SLASH);
		}

		sb.append(uri.endsWith(OrmConstants.SLASH) ? uri.substring(0, uri.lastIndexOf(OrmConstants.SLASH)) : uri);
		String apiUrl = sb.toString();

		for (String str : permitUrls) {
			if (apiUrl.startsWith(str)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 인증을 실행하지 않고 호출 할 수 있는, 기본 URL 설정.
	 */
	private Set<String> getDefaultAllPermitURL() {
		Set<String> permitUrls = new HashSet<String>();
		permitUrls.add("/rest/login");
		permitUrls.add("/rest/terminologies/resource");
		permitUrls.add("/rest/users/exist");
		permitUrls.add("/rest/users/register");
		permitUrls.add("/rest/users/request_init_pass");
		permitUrls.add("/rest/users/init_pass");
		permitUrls.add("/rest/users/approval");
		permitUrls.add("/rest/users/reject");
		permitUrls.add("/rest/users/request_active");
		permitUrls.add("/rest/users/active");
		permitUrls.add("/rest/domains/list");
		permitUrls.add("/rest/check_auth");
		return permitUrls;
	}

	/**
	 * 인증을 실행하지 않고 호출 할 수 있는, 기본 URL 설정.
	 */
	private Set<String> getDefaultReadOnlyPermitURL() {
		Set<String> permitUrls = new HashSet<String>();
		permitUrls.add("/rest/fonts");
		permitUrls.add("/rest/download/public");
		permitUrls.add("/rest/publishers");
		return permitUrls;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.logger.info("RestAuthenticationFilter initialized!");
	}

	@Override
	public void destroy() {
	}
	
	/**
	 * JSON 호출에 대한 인증
	 * 
	 * @param authKey
	 * @param request
	 * @param response
	 */
	private void doJsonAuth(String authKey, HttpServletRequest request, HttpServletResponse response) {
		String authJsonValue = new String(Base64.decodeBase64(authKey.getBytes()));
		User user = FormatUtil.underScoreJsonToObject(authJsonValue, User.class);

		String userId = ValueUtil.checkValue(user.getLogin(), user.getEmail());
		String password = user.getEncryptedPassword();

		Authentication authResult = BeanUtil.get(LoginController.class).doAuthenticate(userId, password, request, response);
		SecurityContextHolder.getContext().setAuthentication(authResult);
		SessionUtil.setAttribute(SecConstants.AUTH_TYPE, SecConstants.AUTH_TYPE_JSON);
	}

	/**
	 * Elidom 토큰 인증 처리
	 * 
	 * @param authKey
	 */
	private void doTokenAuth(String authKey, HttpServletRequest request, HttpServletResponse response) {
		User user = BeanUtil.get(IQueryManager.class).select(User.class, authKey);
		if (ValueUtil.isNotEmpty(user)) {
			Authentication authResult = BeanUtil.get(LoginController.class).doAuthenticate(user.getId(), user.getEncryptedPassword(), request, response);
			// 인증정보 Context에 저장.
			SecurityContextHolder.getContext().setAuthentication(authResult);
			SessionUtil.setAttribute(SecConstants.AUTH_TYPE, SecConstants.AUTH_TYPE_TOKEN);
		}
	}
	
	/**
	 * JWT 토큰 인증 처리
	 * 
	 * @param request
	 * @param response
	 */
	private void doJwtTokenAuth(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		String jwtToken = AnyConstants.EMPTY_STRING;
		
		// 쿠키에서 토큰키 추출
		for(Cookie cookie : cookies) {
			if(ValueUtil.isEqual(cookie.getName(), this.jwtTokenName)) {
				jwtToken = cookie.getValue();
				break;
			}
		}
		
		// 토큰 키가 존재하지 않으면 인증 실패 
		if(ValueUtil.isEmpty(jwtToken)) {
			return;
		}
		
		String[] chunks = jwtToken.split("\\.");
		String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
		JwtTokenPayLoad payloadModel = FormatUtil.jsonToObject(payload, JwtTokenPayLoad.class);
		String loginId = payloadModel.getId();
		
		User user = BeanUtil.get(IQueryManager.class).select(User.class, loginId);
		if (ValueUtil.isNotEmpty(user)) {
			Authentication authResult = BeanUtil.get(LoginController.class).doAuthenticate(user.getId(), user.getEncryptedPassword(), request, response);
			// 인증 정보 Context에 저장.
			SecurityContextHolder.getContext().setAuthentication(authResult);
			SessionUtil.setAttribute(SecConstants.AUTH_TYPE, SecConstants.AUTH_TYPE_JWT);
		}
	}
}