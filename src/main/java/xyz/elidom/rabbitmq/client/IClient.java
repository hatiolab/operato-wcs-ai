package xyz.elidom.rabbitmq.client;

/**
 * 브로커 클라이언트 인터페이스 
 * @author yang
 *
 */
public interface IClient {
	void initLogger();
	void addVirtualHost(String vHost);
	void removeVirtualHost(String vHost);
}
