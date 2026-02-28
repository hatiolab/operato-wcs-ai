package xyz.elidom.rabbitmq.service;

import xyz.elidom.rabbitmq.service.model.DetailRate;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * elings-rabbitmq 서비스에서 사용할 util 모음 
 * @author yang
 *
 */
public class ServiceUtil {

	/**
	 * broker 리턴 값에 대한 변환 0 B/s  
	 * @param rate
	 * @return
	 */
	public static String valueToByteStringPerSec(DetailRate rate) {
		return ServiceUtil.valueToByteString(rate) + "/s";
	}
	
	/**
	 * broker 리턴 값에 대한 변환 0 B
	 * @param rate
	 * @return
	 */
	public static String valueToByteString(DetailRate rate) {
		if(ValueUtil.isEmpty(rate)) return "0 B";
		
		double value = rate.getRate();
	 	long lValue = Math.round(value);
	 	
	 	return ServiceUtil.valueToByteString(lValue);
	}
	
	/**
	 * long 값을 String K /M /G /T 변 환 
	 * @param lValue
	 * @return
	 */
	public static String valueToByteString(long lValue) {
		String[] unitArr = new String[] { " ", " K", " M", " G", " T" };
		int divValue = 1024;
		
		int repeatCnt = 0;
		
		while(true) {
	 		long temp = lValue / divValue;
	 		if(temp < 1) break;
	 		
	 		lValue = temp;
	 		repeatCnt++;
	 	}
		return lValue + unitArr[repeatCnt] + "B";
	}
	
	/**
	 * String Array to String CamelCase Array 
	 * @param values
	 * @return
	 */
	public static String[] arrayStringToCamelCase(String[] values) {
		
		for(int i = 0 ; i < values.length ;i++) {
			values[i] = FormatUtil.toCamelCase(values[i]);
		}
		
		return values;
	}
}
