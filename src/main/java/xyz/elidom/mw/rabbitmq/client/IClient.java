package xyz.elidom.mw.rabbitmq.client;

/**
 * 브로커 클라이언트 인터페이스
 * 
 * @author yang
 */
public interface IClient {
	/**
	 * Logger 초기화
	 */
	public void initLogger();
	/**
	 * Virtual Host 추가
	 * 
	 * @param vHost
	 */
	public void addVirtualHost(String vHost);
	/**
	 * Virtual Host 제거
	 * 
	 * @param vHost
	 */
	public void removeVirtualHost(String vHost);
}
