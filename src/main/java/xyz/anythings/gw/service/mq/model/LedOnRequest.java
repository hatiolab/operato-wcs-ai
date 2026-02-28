package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeName(Action.Values.LedOnRequest)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class LedOnRequest implements IMessageBody {
	@JsonIgnore
	private String action=Action.Values.LedOnRequest;

	// 인디케이터 아이디 
	private String id;
	
	// LED바 점등 방식
	private String ledBarMode;
	
	// LED바 깜박임 주기
	private Integer ledBarIntvl;
	
	// LED바 밝기
	private Integer ledBarBrtns;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLedBarMode() {
		return ledBarMode;
	}

	public void setLedBarMode(String ledBarMode) {
		this.ledBarMode = ledBarMode;
	}

	public Integer getLedBarIntvl() {
		return ledBarIntvl;
	}

	public void setLedBarIntvl(Integer ledBarIntvl) {
		this.ledBarIntvl = ledBarIntvl;
	}

	public Integer getLedBarBrtns() {
		return ledBarBrtns;
	}

	public void setLedBarBrtns(Integer ledBarBrtns) {
		this.ledBarBrtns = ledBarBrtns;
	}
}
