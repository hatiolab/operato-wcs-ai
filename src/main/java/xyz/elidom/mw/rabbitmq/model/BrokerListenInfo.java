package xyz.elidom.mw.rabbitmq.model;

/**
 * 브로커 리스너 정보 모델
 * 
 * @author shortstop
 */
public class BrokerListenInfo {
	/**
	 * IP 배열
	 */
	private String[] ip;
	/**
	 * 포트 배열
	 */
	private int[] port;
	
	public int[] getPort() {
		return port;
	}
	
	public void setPort(int[] port) {
		this.port = port;
	}
	
	public String[] getIp() {
		return ip;
	}
	
	public void setIp(String[] ip) {
		this.ip = ip;
	}	
}
