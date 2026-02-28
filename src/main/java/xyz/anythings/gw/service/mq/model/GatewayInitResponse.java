package xyz.anythings.gw.service.mq.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeName(Action.Values.GatewayInitResponse)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class GatewayInitResponse implements IMessageBody {
	@JsonIgnore
	private String action = Action.Values.GatewayInitResponse;

	// 게이트 웨이 최신 버전
	private String gwVersion;

	// 게이트 웨이 init 정보
	private GatewayInitResGwConfig gwConf;

	// 인디케이터 최신 버전 정보
	private String indVersion;
	
	// 게이트웨이가 관리하는 인디케이터 목록
	private List<GatewayInitResIndList> indList;

	// 게이트웨이가 관리하는 인디케이터 init 정보
	private GatewayInitResIndConfig indConf;
	
	// 서버 시간
	private long svrTime;

	// 상태 보고 주기 ( 초 ) 
	private int healthPeriod;
	
	public long getSvrTime() {
		return svrTime;
	}

	public void setSvrTime(long svrTime) {
		this.svrTime = svrTime;
	}

	public List<GatewayInitResIndList> getIndList() {
		return indList;
	}

	public void setIndList(List<GatewayInitResIndList> indList) {
		this.indList = indList;
	}

	public GatewayInitResIndConfig getIndConf() {
		return indConf;
	}

	public void setIndConf(GatewayInitResIndConfig indConf) {
		this.indConf = indConf;
	}

	public String getAction() {
		return action;
	}

	public GatewayInitResGwConfig getGwConf() {
		return gwConf;
	}

	public void setGwConf(GatewayInitResGwConfig gwConf) {
		this.gwConf = gwConf;
	}

	public String getIndVersion() {
		return indVersion;
	}

	public void setIndVersion(String indVersion) {
		this.indVersion = indVersion;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getGwVersion() {
		return gwVersion;
	}

	public void setGwVersion(String gwVersion) {
		this.gwVersion = gwVersion;
	}

	public int getHealthPeriod() {
		return healthPeriod;
	}

	public void setHealthPeriod(int healthPeriod) {
		this.healthPeriod = healthPeriod;
	}
}
