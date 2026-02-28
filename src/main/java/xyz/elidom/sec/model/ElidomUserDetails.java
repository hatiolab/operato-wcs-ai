/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.model;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sec.SecConfigConstants;
import xyz.elidom.sec.SecConstants;
import xyz.elidom.sec.util.SecurityUtil;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.SessionUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;

/**
 * User Information for security
 * 
 * @author Minu.Kim
 */
@SuppressWarnings("serial")
public class ElidomUserDetails implements UserDetails {

	private User userDetail;

	public ElidomUserDetails(final User user) {
		this.userDetail = user;
	}

	@Override
	public String getPassword() {
		if (ValueUtil.isEmpty(this.userDetail.getEncryptedPassword())) {
			User user = BeanUtil.get(IQueryManager.class).select(User.class, this.userDetail.getId());
			return (user == null) ? null : user.getEncryptedPassword();
		} else {
			return this.userDetail.getEncryptedPassword();
		}
	}

	@Override
	public String getUsername() {
		return userDetail.getId();
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return null;
	}

	/**
	 * 계정 만료 상태 확인.
	 */
	@Override
	public boolean isAccountNonExpired() {
		// 현재까지 지원 안 함
		return true;
	}

	/**
	 * 계정 잠금 상태 확인 
	 * 일시 잠금 기능 ex) 비밀번호를 일정 횟수 이상 오입력 시 메시지 - 5분 후에 다시 시도해주세요
	 */
	@Override
	public boolean isAccountNonLocked() {
		String expireDate = this.userDetail.getAccountExpireDate();
		int lockMinute = ValueUtil.toInteger(SettingUtil.getValue(SecConfigConstants.USER_PASS_LOCK_MINUTE), 0);
		
		if (ValueUtil.isEmpty(expireDate) || lockMinute < 1) {
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
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		// 비밀번호 만료 날짜가 비어있는 경우 초기값 설정(오늘 날짜 + 변경 주기)
		String expireDate = this.userDetail.getPasswordExpireDate();

		// 비밀번호 만료 설정이 활성화 되어 있지 않을 경우 return
		boolean isEnable = ValueUtil.toBoolean(SettingUtil.getValue(SysConfigConstants.USER_PASSWORD_EXPIRE_ENABLE), false);
		if (!isEnable) {
			return true;
		}

		// 비밀번호 만료 상태 Check
		String accountType = this.userDetail.getAccountType();
		boolean expirableType = ValueUtil.isEmpty(accountType) || ValueUtil.isEqual(accountType, SysConstants.ACCOUNT_TYPE_USER);

		// 계정 타입이 user가 아니거나, 비어있지 않은 경우
		if (!expirableType) {
			return true;
		}

		// 기본 비밀번호와 동일한지 확인.
		boolean isDefaultPass = false;
		String defaultPass = SettingUtil.getValue(SysConfigConstants.SECURITY_INIT_PASS);
		if (ValueUtil.isNotEmpty(defaultPass)) {
			isDefaultPass = ValueUtil.isEqual(userDetail.getEncryptedPassword(), SecurityUtil.encodePassword(defaultPass));
		}
		
		// 만료 날짜가 현재 날짜보다 작을 경우
		if (ValueUtil.isNotEmpty(expireDate) && !DateUtil.isBiggerThenCurrentDate(expireDate)) {
			String status = isDefaultPass ? SecConstants.ACCOUNT_STATUS_PASSWORD_CHANGE : SecConstants.ACCOUNT_STATUS_PASSWORD_EXPIRED;
			SessionUtil.setAttribute(SecConstants.ACCOUNT_STATUS, status);
			return true;
		}

		boolean isInitPass = ValueUtil.isNotEmpty(this.userDetail.getResetPasswordToken());
		boolean isFirstLogin = ValueUtil.isEmpty(this.userDetail.getLastSignInAt()) && ValueUtil.isNotEmpty(this.userDetail.getCreatorId())
				&& ValueUtil.isEmpty(userDetail.getPasswordExpireDate());

		// 비밀번호 초기화 요청 또는 최초 로그인 시.
		if (isInitPass || isFirstLogin || isDefaultPass) {
			SessionUtil.setAttribute(SecConstants.ACCOUNT_STATUS, SecConstants.ACCOUNT_STATUS_PASSWORD_CHANGE);
		}

		// 최초 로그인이 아니고, 만료 날짜가 비어 있는 경우 비밀번호 만료 날짜 생성.
		if (!isFirstLogin && ValueUtil.isEmpty(expireDate)) {
			// 비밀번호 만료 날짜가 비어있는 경우 초기값 설정(오늘 날짜 + 변경 주기)
			String period = SettingUtil.getValue(SysConfigConstants.USER_PASSWORD_CHANGE_PERIOD_DAY, "90");
			expireDate = DateUtil.addDateToStr(new Date(), ValueUtil.toInteger(period));
			userDetail.setPasswordExpireDate(expireDate);
			BeanUtil.get(IQueryManager.class).update(userDetail, "passwordExpireDate");
		}
		
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.userDetail != null ? this.userDetail.getActiveFlag() : true;
	}

	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof ElidomUserDetails 	&& ValueUtil.isEqual(((ElidomUserDetails) object).getUsername(), getUsername());
	}

	@Override
	public int hashCode() {
		return getUsername() == null ? 0 : getUsername().hashCode();
	}

	public User getUser() {
		return this.userDetail;
	}
}