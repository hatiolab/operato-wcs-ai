package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonTypeName(Action.Values.GatewayDepRequest)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GatewayDepRequest implements IMessageBody {
	@JsonIgnore
	private String action=Action.Values.GatewayDepRequest;

	// 게이트웨이 펌웨어 다운로드 url
	private String gwUrl;
	// 게이트웨이 펌웨어 버전 
	private String version;
	// 게이트웨이 펌웨어 파일명 
	private String filename;
	// 강제배포 여부 
	private Boolean forceFlag;
	
	public String getGwUrl() {
		return gwUrl;
	}

	public void setGwUrl(String gwUrl) {
		this.gwUrl = gwUrl;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Boolean isForceFlag() {
		return forceFlag;
	}

	public void setForceFlag(boolean forceFlag) {
		this.forceFlag = forceFlag;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
