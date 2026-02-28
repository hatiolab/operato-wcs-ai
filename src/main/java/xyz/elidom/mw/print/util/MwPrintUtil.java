package xyz.elidom.mw.print.util;

import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;

/**
 * 미들웨어 기반 프린트 유틸리티
 * 
 * @author shortstop
 */
public class MwPrintUtil {

	/**
	 * 프린터 큐 이름 리턴
	 * 
	 * @param domain
	 * @param printerType
	 * @param printerCd
	 * @return
	 */
	public static String getPrinterMwQueueName(Domain domain, String printerType, String printerCd) {
		return domain.getMwSiteCd() + SysConstants.SLASH + "PRINTER" + SysConstants.SLASH + printerType.toUpperCase() + SysConstants.SLASH + printerCd.toLowerCase();
	}
}
