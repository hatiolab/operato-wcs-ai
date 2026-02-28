package xyz.elidom.mw.rabbitmq.service;

/**
 * Elastic Search 관련 서비스 URL 정의
 * 
 * @author shortstop
 */
public class ElasticUrls {
	/**
	 * Bulk Operator : insert Bulk
	 */
	public static final String BULK_OPE= "/%s/%s/_bulk";
	/**
	 * Index List 
	 */
	public static final String ALL_INDEXS= "/_aliases";
	/**
	 * Index Delete
	 */
	public static final String INDEX_DELETE= "/%s";
	
	/**
	 * Index Data Search
	 */
	// 인덱스 포함 조회 
	public static final String INDEX_SEARCH="/%s/_search?ignore_unavailable=true";
	// 인덱스 전체 조회 
	public static final String _SEARCH="/_search";
	// 맥스 result 값 수정 
	public static final String _SEARCH_MAX_ROW="/%s/_settings?preserve_existing=true";
}
