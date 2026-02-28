package xyz.anythings.base.model;

import java.util.Date;

import xyz.elidom.util.ValueUtil;

/**
 * Database 기준 현재 시간 정보
 * 
 * @author shortstop
 */
public class CurrentDbTime {
	/**
	 * 현재 시간
	 */
	private Date currentTime;
	/**
	 * 현재 날짜 (YYYY-MM-DD)
	 */
	private String dateStr;
	/**
	 * 현재 시간 (HH24)
	 */
	private Integer hour;
	/**
	 * 현재 시간 문자열 - 오전 3시이면 03
	 */
	private String hourStr;
	/**
	 * 현재 분 (MI)
	 */	
	private Integer minute;
	/**
	 * 현재 시간 문자열 - 오전 3시이면 03
	 */
	private String minuteStr;
	/**
	 * 현재 초 (SS)
	 */	
	private Integer second;
	/**
	 * 현재 초 문자열 -  3초이면 03
	 */
	private String secondStr;
	
	public Date getCurrentTime() {
		return currentTime;
	}
	
	public void setCurrentTime(Date currentTime) {
		this.currentTime = currentTime;
	}
	
	public String getDateStr() {
		return dateStr;
	}
	
	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}
	
	public Integer getHour() {
		if(this.hour == null && this.hourStr != null) {
			return ValueUtil.toInteger(this.hourStr);
		} else {
			return hour;
		}
	}
	
	public void setHour(Integer hour) {
		this.hour = hour;
	}
	
	public String getHourStr() {
		return hourStr;
	}
	
	public void setHourStr(String hourStr) {
		this.hourStr = hourStr;
	}
	
	public Integer getMinute() {
		if(this.minute == null && this.minuteStr != null) {
			return ValueUtil.toInteger(this.minuteStr);
		} else {
			return minute;
		}
	}
	
	public void setMinute(Integer minute) {
		this.minute = minute;
	}
	
	public String getMinuteStr() {
		return minuteStr;
	}
	
	public void setMinuteStr(String minuteStr) {
		this.minuteStr = minuteStr;
	}
	
	public Integer getSecond() {
		if(this.second == null && this.secondStr != null) {
			return ValueUtil.toInteger(this.secondStr);
		} else {
			return second;
		}
	}
	
	public void setSecond(Integer second) {
		this.second = second;
	}
	
	public String getSecondStr() {
		return secondStr;
	}
	
	public void setSecondStr(String secondStr) {
		this.secondStr = secondStr;
	}
}
