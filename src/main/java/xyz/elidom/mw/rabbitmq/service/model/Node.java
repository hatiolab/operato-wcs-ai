package xyz.elidom.mw.rabbitmq.service.model;

/**
 * rabbitmq 클러스터 노드 모델
 * 
 * @author yang
 */
public class Node {
	
	private String name;
	private long memUsed;
	private long procUsed;
	private long socketsUsed;
	private String memUsedStr;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getMemUsed() {
		return memUsed;
	}
	
	public void setMemUsed(long memUsed) {
		this.memUsed = memUsed;
	}
	
	public long getProcUsed() {
		return procUsed;
	}
	
	public void setProcUsed(long procUsed) {
		this.procUsed = procUsed;
	}
	
	public long getSocketsUsed() {
		return socketsUsed;
	}
	
	public void setSocketsUsed(long socketsUsed) {
		this.socketsUsed = socketsUsed;
	}
	
	public String getMemUsedStr() {
		return memUsedStr;
	}
	
	public void setMemUsedStr(String memUsedStr) {
		this.memUsedStr = memUsedStr;
	}
}
