package xyz.elidom.mw.rabbitmq.event.model;

/**
 * 래빗엠큐 큐 생성을 위한 인터페이스
 *  
 * @author yang
 */
public interface IQueueNameModel {
	/**
	 * 도메인 ID 리턴
	 * 
	 * @return
	 */
	public long getDomainId();

	/**
	 * 도메인 사이트 명 설정
	 * 
	 * @param domainSite
	 */
	public void setDomainSite(String domainSite);
	
	/**
	 * 도메인 사이트 명 리턴
	 * 
	 * @return
	 */
	public String getDomainSite();
	
	/**
	 * 이미 생성된 이전 큐의 이름 리턴
	 * 
	 * @return
	 */
	public String getBefQueueName();
	
	/**
	 * 현재 큐의 이름 리턴
	 * 
	 * @return
	 */
	public String getQueueName();
	
	/**
	 * 생성, 수정, 삭제 플래그 리턴
	 * 
	 * @return
	 */
	public String getCudFlag_();
}
