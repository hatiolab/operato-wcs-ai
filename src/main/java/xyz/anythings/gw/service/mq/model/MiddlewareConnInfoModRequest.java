package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeName(Action.Values.MiddlewareConnInfoModRequest)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class MiddlewareConnInfoModRequest implements IMessageBody {
	@JsonIgnore
	private String action = Action.Values.MiddlewareConnInfoModRequest;

	// 미들웨어 접속 IP 정보
	private String[] mwIp;
	// 미들웨어 접속 PORT 정보
	private int[] mwPort;

	// 미들웨어 클라이언트 아이디 
	private String mwClientId;
	// 미들웨어 수신 topic 리스트 
	private String[] mwTopicId;
	// 미들웨어 사이트 구분 
	private String mwSite;

	public String[] getMwIp() {
		return mwIp;
	}

	public void setMwIp(String... mwIp) {
		this.mwIp = mwIp;
	}

	public int[] getMwPort() {
		return mwPort;
	}

	public void setMwPort(int... mwPort) {
		this.mwPort = mwPort;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getMwClientId() {
		return mwClientId;
	}

	public void setMwClientId(String mwClientId) {
		this.mwClientId = mwClientId;
	}

	public String[] getMwTopicId() {
		return mwTopicId;
	}

	public void setMwTopicId(String... mwTopicId) {
		this.mwTopicId = mwTopicId;
	}

	public String getMwSite() {
		return mwSite;
	}

	public void setMwSite(String mwSite) {
		this.mwSite = mwSite;
	}
}
