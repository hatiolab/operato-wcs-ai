package xyz.elidom.sys.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 日期处理
 * 时区处理 
 */
public class OperatoDateUtil {

	// UTC
	private static String DEF_LOCALE_UTC = "UTC";
	
	// APP Server Local TimeZone
	private static String ENV_SERVER_TIME_ZONE = "server.timezone";
	
	/**
	 * Default Date Format
	 */
	private static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	/**
	 * Default Time Format
	 */
	private static String DEFAULT_TIME_FORMAT = "HH:mm:ss";
	/**
	 * Default Date Time Format
	 */
	private static String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	/**
	 * Default Date Time Format
	 */
	private static String DEFAULT_DETAIL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	
	/**
	 * Default Max Date
	 */
	private static String DEFAULT_MAX_DATE = "9999-12-31";
	
	/**
	 * Default Max Date
	 * @return
	 */
	public static String getDefaultMaxDate() {
		return DEFAULT_MAX_DATE;
	}

	/**
	 * Date Format
	 * 
	 * @return
	 */
	public static String getDateFormat() {
		return DEFAULT_DATE_FORMAT;
	}
	
	/**
	 * Time Format
	 *  
	 * @return
	 */
	public static String getTimeFormat() {
		return DEFAULT_TIME_FORMAT;
	}
	
	/**
	 * Date Time Format
	 * 
	 * @return
	 */
	public static String getDateTimeFormat() {
		return DEFAULT_DATE_TIME_FORMAT;
	}
	
	/**
	 * Date Time Format
	 * 
	 * @return
	 */
	public static String getDetailDateTimeFormat() {
		return DEFAULT_DETAIL_DATE_TIME_FORMAT;
	}
	
	/**
	 * 根据是否启用UTC，运行UTC Time或当前OS的时间抽取
	 * 
	 * @return
	 */
	public static Date getDate() {
		// 服务器设置的本地时区
		String serverTimeZone = ValueUtil.toString(EnvUtil.getValue(ENV_SERVER_TIME_ZONE, DEF_LOCALE_UTC));
		Domain domain = null;
		
		try {
			domain = Domain.currentDomain();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		// Domain设置的时区
		String domainTimezone = ValueUtil.isNotEmpty(domain) && ValueUtil.isNotEmpty(domain.getTimezone())? domain.getTimezone() : DEF_LOCALE_UTC;
		
		// 返回当前操作的时区: 如果配置文件设置了服务器时区，则使用服务器配置的时区，同时转换为Domain设置的时区
		return ValueUtil.isNotEmpty(serverTimeZone) ? convertTimeZone(parseLocaleDate(serverTimeZone), serverTimeZone, domainTimezone)  : new Date();
	}
	
	/**
	 * 根据是否启用UTC，运行UTC Time或当前OS的时间抽取
	 * 
	 * @param domainId 域ID
	 * @return
	 */
	public static Date getDate(Long domainId) {
		// 服务器设置的本地时区
		String serverTimeZone = ValueUtil.toString(EnvUtil.getValue(ENV_SERVER_TIME_ZONE, DEF_LOCALE_UTC));
		// 查询域
		Domain domain = BeanUtil.get(DomainController.class).findOne(domainId, null);
		// Domain设置的时区
		String domainTimezone = ValueUtil.isNotEmpty(domain) && ValueUtil.isNotEmpty(domain.getTimezone())? domain.getTimezone() : DEF_LOCALE_UTC;
		// 返回当前操作的时区: 如果配置文件设置了服务器时区，则使用服务器配置的时区，同时转换为Domain设置的时区
		return ValueUtil.isNotEmpty(serverTimeZone) ? convertTimeZone(parseLocaleDate(serverTimeZone), serverTimeZone, domainTimezone)  : new Date();
	}
	
	/**
	 * 将今天的日期转换为默认日期格式
	 * 
	 * @return
	 */
	public static String todayStr() {
		return dateStr(OperatoDateUtil.getDate(), getDateFormat());
	}
	
	/**
	 * 获取今天日期（返回字符串）
	 * 
	 * @return
	 */	
	public static String todayStr(String format) {
		return dateStr(getDate(), format);
	}
	
	/**
	 * 返回当前时间（字符串）
	 * 
	 * @return
	 */
	public static String currentTimeStr() {
		return dateStr(getDate(), getDateTimeFormat());
	}
	
	/**
	 * 格式化日期
	 * 
	 * @return
	 */	
	public static String dateTimeStr(Date date, String format) {
		return dateStr(date, format);
	}
	
	/**
	 * 格式化时间
	 * 
	 * @return
	 */	
	public static String dateTimeStr(Date date) {
		return dateTimeStr(date, getDateTimeFormat());
	}	
	
	/**
	 * 获取当前日期
	 * 
	 * @return
	 */
	public static String currentDate() {
		return dateStr(getDate(), getDateFormat());
	}
	
	/**
	 * Date类型格式化默认字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String defaultDateStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(getDateFormat());
		return sdf.format(date);		
	}
	
	/**
	 * Date类型格式化指定字符串格式
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateStr(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);		
	}
	
	/**
	 * Date String转化成 Date类型
	 * 
	 * @param dateTimeStr
	 * @return
	 */
	public static Date parse(String dateTimeStr) {
		return parse(dateTimeStr, getDateTimeFormat());
	}
	
	/**
	 * Date String转化指定格式的Date
	 * 
	 * @param dateStr
	 * @param format
	 * @return
	 */
	public static Date parse(String dateStr, String format) {
		try {
			return new SimpleDateFormat(format).parse(dateStr);
		} catch(ParseException ex) {
			throw new IllegalArgumentException("Parse date error [" + dateStr + "]", ex);
		}
	}
	
	/**
	 * UTC 世界标准时间
	 * 
	 * @return
	 */
	public static Date getCurrentUtcDate() {
		return parseLocaleDate(DEF_LOCALE_UTC);
	}

	/**
	 * 本地时间指定
	 * 
	 * @param locale
	 * @return
	 */
	public static Date parseLocaleDate(String locale) {
		return parseLocaleDate(new Date(), locale);
	}

	public static Date parseLocaleDate(Date utcDate, String locale) {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DETAIL_DATE_TIME_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone(locale));
		return parse(dateFormat.format(utcDate), DEFAULT_DETAIL_DATE_TIME_FORMAT);
	}
	
	/**
	 * 日期相加指定天数
	 * 
	 * @param date    yyyy-MM-dd
	 * @param addDate
	 * @return
	 */
	public static Date addDate(String date, int addDate) {
		Calendar c = Calendar.getInstance(); 
		c.setTime(OperatoDateUtil.parse(date, getDateFormat()));
		c.add(Calendar.DATE, addDate);
		return c.getTime();
	}
	
	/**
	 * 日期相加指定天数
	 * 
	 * @param date
	 * @param addDate
	 * @return
	 */
	public static Date addDate(Date date, int addDate) {
		Calendar c = Calendar.getInstance(); 
		c.setTime(date);
		c.add(Calendar.DATE, addDate);
		return c.getTime();
	}
	
	/**
	 * 日期相加指定天数（返回字符串格式）
	 * 
	 * @param date
	 * @param addDate
	 * @return
	 */
	public static String addDateToStr(String date, int addDate) {
		Date value = addDate(date, addDate);
		return new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(value);
	}
	
	/**
	 * 日期相加指定天数（返回字符串格式）
	 * 
	 * @param date
	 * @param addDate
	 * @return
	 */
	public static String addDateToStr(Date date, int addDate) {
		Date value = addDate(date, addDate);
		return new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(value);
	}
		
	/**
	 * 获取当前年
	 * 
	 * @return
	 */
	public static String getYear() {
		return dateInfoMap().get(Calendar.YEAR);
	}
	
	/**
	 * 获取当前月
	 * 
	 * @return
	 */
	public static String getMonth() {
		return dateInfoMap().get(Calendar.MONTH);
	}
	
	/**
	 * 获取当前日
	 * 
	 * @return
	 */
	public static String getDay() {
		return dateInfoMap().get(Calendar.DATE);
	}
	
	/**
	 * 获取当前时
	 * 
	 * @return
	 */
	public static String getHour() {
		return dateInfoMap().get(Calendar.HOUR_OF_DAY);
	}
	
	/**
	 * 获取当前分钟
	 * 
	 * @return
	 */
	public static String getMinute() {
		return dateInfoMap().get(Calendar.MINUTE);
	}
	
	/**
	 * 获取当前秒
	 * 
	 * @return
	 */
	public static String getSecond() {
		return dateInfoMap().get(Calendar.SECOND);
	}
	
	/**
	 * 系统当前月
	 * 
	 * @return
	 */
	public static String getCurrentMonth() {
		StringBuilder sb = new StringBuilder();
		Map<Integer, String> map = dateInfoMap();
		sb.append(map.get(Calendar.YEAR));
		sb.append(map.get(Calendar.MONTH));
		return sb.toString();
	}
	
	/**
	 * 系统当前日
	 * 
	 * @return
	 */
	public static String getCurrentDay() {
		StringBuilder sb = new StringBuilder();
		Map<Integer, String> map = dateInfoMap();
		sb.append(getCurrentMonth());
		sb.append(map.get(Calendar.DATE));
		return sb.toString();
	}
	
	/**
	 * 系统当前小时
	 * 
	 * @return
	 */
	public static String getCurrentHour() {
		StringBuilder sb = new StringBuilder();
		Map<Integer, String> map = dateInfoMap();
		sb.append(getCurrentDay());
		sb.append(map.get(Calendar.HOUR_OF_DAY));
		return sb.toString();
	}
	
	/**
	 * 系统当前分钟
	 * 
	 * @return
	 */
	public static String getCurrentMinute() {
		StringBuilder sb = new StringBuilder();
		Map<Integer, String> map = dateInfoMap();
		sb.append(getCurrentHour());
		sb.append(map.get(Calendar.MINUTE));
		return sb.toString();
	}
	
	/**
	 * 系统当前秒
	 * 
	 * @return
	 */
	public static String getCurrentSecond() {
		StringBuilder sb = new StringBuilder();
		Map<Integer, String> map = dateInfoMap();
		sb.append(getCurrentMinute());
		sb.append(map.get(Calendar.SECOND));
		return sb.toString();
	}
	
	/**
	 * 将当前日期的年、月、日信息返回到Map
	 * 
	 * @return
	 */
	public static Map<Integer, String> dateInfoMap() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getDate());

		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(Calendar.YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
		map.put(Calendar.MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
		map.put(Calendar.DATE, Integer.toString(calendar.get(Calendar.DATE)));
		map.put(Calendar.HOUR_OF_DAY, Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
		map.put(Calendar.MINUTE, Integer.toString(calendar.get(Calendar.MINUTE)));
		map.put(Calendar.SECOND, Integer.toString(calendar.get(Calendar.SECOND)));

		for (int key : map.keySet()) {
			String value = map.get(key);
			if (value.length() == 1) {
				map.put(key, "0" + value);
			}
		}
		return map;
	}
	
	/**
	 * 计算From - To经过的（秒）
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public static int elapsedTime(Date fromDate, Date toDate) {
		long fromTime = fromDate.getTime();
		long toTime = toDate.getTime();
		
		if(fromTime >= toTime) {
			return 0;
		}
		
		long elapsedTime = (toTime - fromTime) / 1000;
		return ValueUtil.toInteger(elapsedTime);
	}
	
	/**
	 * 计算From - To经过的（分）
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public static float elapsedMinute(Date fromDate, Date toDate) {
		int timeGap = elapsedTime(fromDate, toDate);
		if(timeGap == 0) {
			return 0f;
		}
		
		return secondsToMinutes(timeGap);
	}
	
	/**
	 * 计算From - To经过的（日）
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public static float elapsedDay(Date fromDate, Date toDate) {
		int timeGap = elapsedTime(fromDate, toDate);
		if(timeGap == 0) {
			return 0f;
		}
		
		return secondsToMinutes(timeGap) / 60 /24;
	}
	
	/**
	 * 计算From - To经过的（日）
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public static int elapsedDay(String fromDate, String toDate) {
		
		int timeGap = elapsedTime(OperatoDateUtil.parse(fromDate, getDateFormat()), OperatoDateUtil.parse(toDate, getDateFormat()));
		if(timeGap == 0) {
			return 0;
		}
		
		return (int) (secondsToMinutes(timeGap) / 60 /24);
	}
	
	/**
	 * 计算From - To经过的（时）
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public static float elapsedHour(Date fromDate, Date toDate) {
		int timeGap = elapsedTime(fromDate, toDate);
		if(timeGap == 0) {
			return 0f;
		}
		
		return secondsToMinutes(timeGap) / 60;
	}
	
	/**
	 * 秒转成分钟
	 * 
	 * @param seconds
	 * @return
	 */
	public static Float secondsToMinutes(int seconds) {
		return ValueUtil.toFloat((seconds < 60) ? 1.0 : (seconds / 60.0));
	}
	
	/**
	 * Compares base date to current Date for ordering.
	 * 
	 * @param baseTime 기준 날짜
	 * @return
	 */
	public static boolean isBiggerThenCurrentDate(String baseTime) {
		return isBiggerThenTargetDate(baseTime, todayStr(DEFAULT_DATE_FORMAT));
	}

	/**
	 * Compares two Dates for ordering.
	 * (baseDate greater than targetDate : true, baseDate less than equals targetDate : false)
	 * 两个日期比较大小，大于等于 true
	 * 
	 * @param baseDate 基准日期
	 * @param targetDate 比较目标日期
	 * @return
	 */
	public static boolean isBiggerThenTargetDate(String baseDate, String targetDate) {
		try {
			Date baseTimeDate = new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(baseDate);
			Date targetTimeDate = new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(targetDate);
			return !(baseTimeDate.compareTo(targetTimeDate) < 1);
		} catch (ParseException e) {
			return false;
		}
	}
	
	/**
	 * Compares two Dates for ordering.
	 * (baseDate greater targetDate : true, baseDate less equals targetDate : false)
	 * 两个日期比较大小， 大于 true， 等于小于 false
	 * 
	 * @param baseDate 基准日期
	 * @param targetDate 比较目标日期
	 * @return
	 */
	public static boolean isBiggerTargetDate(String baseDate, String targetDate) {
		try {
			Date baseTimeDate = new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(baseDate);
			Date targetTimeDate = new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(targetDate);
			return !(baseTimeDate.compareTo(targetTimeDate) <= 1);
		} catch (ParseException e) {
			return false;
		}
	}
	
	/**
	 * 比较两个日期段是否有重合
	 * 
	 * @param date1Start
	 * @param date1End
	 * @param date2Start
	 * @param date2End
	 * @return
	 */
	public static boolean isDateOverlapping(String date1Start, String date1End, String date2Start, String date2End) {
		
		// 如果区间1的开始日期在区间2之后，或者区间1的结束日期在区间2之前，则不重叠
		if(isBiggerThenTargetDate(date1Start, date2End) || isBiggerThenTargetDate(date2Start, date1End)) {
			return false;
		}
        // 否则重叠
        return true;
	}
	
	/**
	 * 时区转化
	 * @param argDate
	 * @param fromTimeZone
	 * @param toTimeZone
	 * @return
	 */
	public static Date convertTimeZone(Date argDate, String fromTimeZone, String toTimeZone) {
        // from 解析为ZonedDateTime
        ZonedDateTime fromDateTime = argDate.toInstant().atZone(ZoneId.of(fromTimeZone));
        // 转换TO时区
        ZonedDateTime toDateTime = fromDateTime.withZoneSameInstant(ZoneId.of(toTimeZone));
        // 返回TO时区 时间
        return Date.from(toDateTime.toLocalDateTime().toInstant(fromDateTime.getOffset()));
	}
	
	/**
	 * 获取字符日期的年（yyyy-MM-dd)
	 * @param date
	 */
	public static String getYear(String date) {
		return ValueUtil.isEmpty(date) || date.length() != 10? null : date.substring(0, 4);
	}
	
	/**
	 * 获取字符日期的月（yyyy-MM-dd)
	 * @param date
	 */
	public static String getMonth(String date) {
		return ValueUtil.isEmpty(date) || date.length() != 10? null : date.substring(5, 7);
	}
	
	/**
	 * 获取字符日期的日（yyyy-MM-dd)
	 * @param date
	 */
	public static String getDay(String date) {
		return ValueUtil.isEmpty(date) || date.length() != 10? null : date.substring(8, 10);
	}
	
	/**
	 * 指定日期对应月份第一天
	 * @param date (yyyy-MM-dd)
	 * @return yyyy-MM-dd
	 */
	public static String getMonthFirstDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDate firstDay = localDate.withDayOfMonth(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        return firstDay.format(formatter);
	}
	
	/**
	 * 指定日期对应月份最后一天
	 * @param date (yyyy-MM-dd)
	 * @return yyyy-MM-dd
	 */
	public static String getMonthLastDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDate lastDay = localDate.withDayOfMonth(localDate.lengthOfMonth());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        return lastDay.format(formatter);
	}
	
	/**
	 * 月份相加（返回字符串格式）
	 * 
	 * @param date     (yyyy-MM-dd)
	 * @param addMonth
	 * @return yyyy-MM-dd
	 */
	public static String addMonthToStr(String date, int addMonth) {
		LocalDate localDate = LocalDate.parse(date);
		LocalDate localMonth = localDate.plusMonths(addMonth);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        return localMonth.format(formatter);
	}

	public static void main(String[] args) {
		String date = "2025-02-15";
		
		System.out.println(OperatoDateUtil.addMonthToStr(date, -3));
		
		date = "2025-02-01";
		
		System.out.println(OperatoDateUtil.getMonthLastDate(date));
		

		date = "2025-03-11";
		
		System.out.println(OperatoDateUtil.getMonthFirstDate(date));
		
		System.out.println(OperatoDateUtil.elapsedDay(date,  "2025-03-12"));
	}
}
