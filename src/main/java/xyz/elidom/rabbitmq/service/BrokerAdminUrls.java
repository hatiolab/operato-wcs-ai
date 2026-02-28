package xyz.elidom.rabbitmq.service;

public class BrokerAdminUrls {
//	public static final String VIRTUAL_HOST_ALL_LIST= "http://{address}:{port}/api/vhosts";
	
	/**
	 * Virtual host 관리 URL
	 */
	// 전체 사이트 리스트 
	public static final String VIRTUAL_HOST_LIST_ALL= "http://{address}:{port}/api/vhosts";
	// 사이트 삭제 
	public static final String VIRTUAL_HOST_REMVOE = "http://{address}:{port}/api/vhosts/{vhost}";
	// 사이트 추가 
	public static final String VIRTUAL_HOST_ADD = "http://{address}:{port}/api/vhosts/{vhost}";
	// 사이트 트레이스 on
	public static final String VIRTUAL_HOST_TRACE = "http://{address}:{port}/api/vhosts/{vhost}";
	
	
	/**
	 * Policy
	 */
	// 사이트별 정책 
	public static final String POLICY_ADD = "http://{address}:{port}/api/policies/{vhost}/{name}";
	
	
	/**
	 * Cluster
	 */
	// 전체 클러스터 리스트 
	public static final String CLUSTER_LIST_ALL = "http://{address}:{port}/api/nodes";
	
	
	/**
	 * Queue
	 */
	// 전체 큐 검색 
	public static final String QUEUE_SEARCH = "http://{address}:{port}/api/queues?page={page}&page_size={size}&name={name}&use_regex=true&sort={sort_col}&sort_reverse={is_sort_asc}&pagination=true";
	// 사이트 큐 검색 
	public static final String QUEUE_SEARCH_VHOST = "http://{address}:{port}/api/queues/{vhost}?page={page}&page_size={size}&name={name}&use_regex=true&sort={sort_col}&sort_reverse={is_sort_asc}&pagination=true";
	// 큐 상세 정보 
	public static final String QUEUE_DETAIL_INFO = "http://{address}:{port}/api/queues/{vhost}/{name}";

	// http://192.168.0.248:15672/api/queues/yangji?page=1&page_size=100&name=&use_regex=true&sort=name&sort_reverse=false&pagination=true
	//http://192.168.0.248:15672/api/queues?page=1&page_size=500&name=&use_regex=false&pagination=true
	//http://192.168.0.248:15672/api/queues/dongtan?page=1&page_size=500&name=&use_regex=false&pagination=true
}
