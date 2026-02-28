/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.service;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sec.model.ElidomUserDetails;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * User Information provider service for security
 * 
 * @author shortstop
 */
@Service
public class ElidomUserDetailsService implements UserDetailsService {

	/**
	 * 기본 메시지 EMPTY_NOT_ALLOWED - Empty [{0}] is not allowed
	 */
	private static final String MSG_EMPTY_NOT_ALLOWED = "Empty [{0}] is not allowed!";
	/**
	 * 사용자 아이디 다국어 코드 
	 */
	private static final String USER_ID = "terms.label.user_id";
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		if (ValueUtil.isEmpty(username)) {
			String msg = MessageUtil.getMessage(SysMessageConstants.EMPTY_PARAM, MSG_EMPTY_NOT_ALLOWED, MessageUtil.params(USER_ID));
			throw new UsernameNotFoundException(msg);
		}

		User user = BeanUtil.get(IQueryManager.class).select(User.class, username);
		
		if (ValueUtil.isEmpty(user)) {
			throw new UsernameNotFoundException("User [" + username + "] is not exist.");
		}
		
		return new ElidomUserDetails(user);
	}
}