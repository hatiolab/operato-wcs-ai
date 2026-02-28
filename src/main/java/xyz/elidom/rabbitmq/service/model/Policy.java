package xyz.elidom.rabbitmq.service.model;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * rabbitmq 사이트 정책 모델 
 * @author yang
 *
 */
public class Policy {
	
	private String pattern;
	private int priority;
	private Map<String,Object> definition;
	
	@SerializedName("apply-to")
	private String applyTo;
	
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getApplyTo() {
		return applyTo;
	}
	public void setApplyTo(String applyTo) {
		this.applyTo = applyTo;
	}
	public Map<String, Object> getDefinition() {
		return definition;
	}
	public void setDefinition(Map<String, Object> definition) {
		this.definition = definition;
	}
	
}
