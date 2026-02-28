package xyz.elidom.rabbitmq.model;

public class BrokerListenInfo {
	private String[] ip;
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
