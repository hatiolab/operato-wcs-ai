package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeName(Action.Values.IndicatorDepRequest)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class IndicatorDepRequest implements IMessageBody {
	@JsonIgnore
	private String action=Action.Values.IndicatorDepRequest;

	// 표시기 펌웨어 다운로드 url
	private String indUrl;
	// 표시기 펌웨어 버전 
	private String version;
	// 펌웨어 파일명 
	private String filename;
	// 강제배포 여부 
	private Boolean forceFlag;
	
	public String getIndUrl() {
		return indUrl;
	}

	public void setIndUrl(String indUrl) {
		this.indUrl = indUrl;
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

	public Boolean getForceFlag() {
		return forceFlag;
	}

	public void setForceFlag(Boolean forceFlag) {
		this.forceFlag = forceFlag;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
