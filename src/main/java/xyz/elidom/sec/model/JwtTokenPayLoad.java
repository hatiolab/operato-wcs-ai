/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.model;

/**
 * JWT access_token payload 모델 
 * 
 * @author yang
 */

public class JwtTokenPayLoad{
	
	/**
	 * 사용자 ID 
	 */
	private String id;
	
	/**
	 * 사용자 유형 
	 */
	private String userType;
	
	/**
	 * 상태 
	 */
	private String status;
	
	/**
	 * 토큰 발행 시간 timestamp
	 */
	private long iat;
	
	/**
	 * 토큰 만료 시간 timestamp
	 */
	private long exp;
	
	/**
	 * JWT 토큰 발급자 (issuer)
	 */
	private String iss;
	
	/**
	 * JWT 토큰 제목 (subject)
	 */
	private String sub;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getIat() {
		return iat;
	}

	public void setIat(long iat) {
		this.iat = iat;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}
	
	
}