package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeName(Action.Values.TimesyncResponse)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class TimesyncResponse implements IMessageBody {
	@JsonIgnore
	private String action = Action.Values.TimesyncResponse;

	private long svrTime;

	public TimesyncResponse() {
	}

	public TimesyncResponse(long svrTime) {
		this.svrTime = svrTime;
	}

	public long getSvrTime() {
		return svrTime;
	}

	public void setSvrTime(long svrTime) {
		this.svrTime = svrTime;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
