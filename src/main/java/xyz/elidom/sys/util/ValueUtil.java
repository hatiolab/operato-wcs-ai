/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sys.util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.FormatUtil;

/**
 * Value 빈값 체크, 검증, 변환 등의  데이터 처리를 위한 유틸리티 클래스 
 *  
 * @author shortstop
 */
public class ValueUtil extends xyz.elidom.util.ValueUtil {
	
	/**
	 * From Date
	 */
	private static final String FROM_DATE = "from_date";
	/**
	 * To Date
	 */
	private static final String TO_DATE = "to_date";

	/**
	 * Module의 Base Path 리턴.
	 * 
	 * @param module
	 * @return
	 */
	public static String getBasePath(String module) {
		return getModuleProperties(module).getBasePackage();
	}

	/**
	 * Module Property 리턴
	 * 
	 * @param module
	 * @return
	 */
	public static IModuleProperties getModuleProperties(String module) {
		ModuleConfigSet configSet = BeanUtil.get(ModuleConfigSet.class);
		IModuleProperties moduleConfig = configSet.getConfig(module);
		if (ValueUtil.isEmpty(moduleConfig)) {
			throw ThrowUtil.newNotAllowedEmptyInfo("terms.label.module");
		}
		
		return moduleConfig;
	}

	/**
	 * 검색 날짜 가져오기 실행.
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public static Map<String, String> getSearchDate(String fromDate, String toDate) {
		Map<String, String> map = new HashMap<String, String>();
		int defaultPeriod = ValueUtil.toInteger(SettingUtil.getValue(SysConfigConstants.SEARCH_DEFAULT_PERIOD, "7"));
		
		// From과 To 날짜가 모두 비어있을 경우, 현재 날짜를 기준으로 7일 전 데이터까지 조회
		if (ValueUtil.isEmpty(fromDate) && ValueUtil.isEmpty(toDate)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_MONTH, defaultPeriod);

			fromDate = DateUtil.dateStr(calendar.getTime(), DateUtil.getDateFormat());
			toDate = DateUtil.dateStr(new Date(), DateUtil.getDateFormat());
			
		// To 날짜가 비어있을 경우, From 날짜를 기준으로 7일 이후 데이터까지 조회.(To 날짜가 오늘보다 클 경우, 오늘 날짜를 To로 지정.)
		} else if (ValueUtil.isNotEmpty(fromDate) && ValueUtil.isEmpty(toDate)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(DateUtil.parse(fromDate, DateUtil.getDateFormat()));
			calendar.add(Calendar.DAY_OF_MONTH, defaultPeriod);
			Date end = calendar.getTime();
			Date currentDate = new Date();
			toDate = DateUtil.dateStr(currentDate.compareTo(end) < 0 ? currentDate : end, DateUtil.getDateFormat());

		} else if (ValueUtil.isEmpty(fromDate) && ValueUtil.isNotEmpty(toDate)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(DateUtil.parse(toDate, DateUtil.getDateFormat()));
			calendar.add(Calendar.DAY_OF_MONTH, defaultPeriod * (-1));
			fromDate = DateUtil.dateStr(calendar.getTime(), DateUtil.getDateFormat());
		}

		map.put(FROM_DATE, fromDate);
		map.put(TO_DATE, toDate);
		return map;
	}
	
	/**
	 * Query to ParamMap
	 * 
	 * @param query
	 * @return
	 */
	public static Map<String, Object> queryToParamMap(String query) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Filter[] filters = FormatUtil.jsonToObject(query, Filter[].class);
		String startDate = null; 
		String endDate = null;

		if (filters != null) {
			for (Filter filter : filters) {
				String name = filter.getName();
				Object value = filter.getValue();

				if (ValueUtil.isNotEmpty(name) && ValueUtil.isNotEmpty(value)) {
					paramMap.put(name, value);
				}
			}
		}

		paramMap.putAll(ValueUtil.getSearchDate(startDate, endDate));
		return paramMap;
	}	
}
