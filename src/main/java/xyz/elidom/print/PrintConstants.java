package xyz.elidom.print;

/**
 * Print 모듈 관련 상수 정의 
 * 
 * @author yang
 */
public class PrintConstants  {
	
	/**
	 * 프린터 / 템플릿 유형 BARCODE : 바코드 프린터
	 */
	public static final String PRINTER_TYPE_BARCODE = "barcode";
	/**
	 * 프린터 / 템플릿  유형 NORMAL : 일반 프린터 
	 */
	public static final String PRINTER_TYPE_NORMAL = "normal";
	
	/**
	 * 바코드 프린트 REST URL
	 */
	public static final String BARCODE_REST_URL = "/barcode?printer=";
	/**
	 * PDF 프린트 REST URL
	 */
	public static final String NORMAL_REST_URL = "/pdf?printer=";
	
	/**
	 * 미들웨어 통신 유형 
	 */
	public static final String COMM_TYPE_MW = "mw";
    /**
     * REST 통신 유형
     */
    public static final String COMM_TYPE_REST = "rest";
    /**
     * 서버 통신 유형
     */
    public static final String COMM_TYPE_SERVER = "server";
}
