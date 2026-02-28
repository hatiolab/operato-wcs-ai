package xyz.elidom.mw.rabbitmq.model;

import java.util.Map;

import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.ValueUtil;

/**
 * Message 전송 테스트 모델 
 * @author yang
 *
 */
public class PublishTestModel {

	// MW SITE
	String vhost;
	// Exchange Name
	String name;
	// Basic Property
	Map<String,Object> properties = ValueUtil.newMap("delivery_mode,headers", 1,ValueUtil.newMap(SysConstants.EMPTY_STRING));
//	Map<String,Object> properties = ValueUtil.newMap(SysConstants.EMPTY_STRING);
	// Dest Queue Name
	String routing_key;
	
	String delivery_mode = "1";
	// Message 
	String payload;
	// Def 
	Map<String,Object> headers = ValueUtil.newMap(SysConstants.EMPTY_STRING);
	Map<String,Object> props = ValueUtil.newMap(SysConstants.EMPTY_STRING);
	String payload_encoding = "string";
	
	public PublishTestModel(String vhost, String name, String routing_key, String payload) {
		this.vhost = vhost;
		this.name = name;
		this.routing_key = routing_key;
		this.payload = payload;
	}
	
	public String getVhost() {
		return vhost;
	}
	public void setVhost(String vhost) {
		this.vhost = vhost;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Object> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	public String getRouting_key() {
		return routing_key;
	}
	public void setRouting_key(String routing_key) {
		this.routing_key = routing_key;
	}
	public String getDelivery_mode() {
		return delivery_mode;
	}
	public void setDelivery_mode(String delivery_mode) {
		this.delivery_mode = delivery_mode;
	}
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	public Map<String, Object> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}
	public Map<String, Object> getProps() {
		return props;
	}
	public void setProps(Map<String, Object> props) {
		this.props = props;
	}
	public String getPayload_encoding() {
		return payload_encoding;
	}
	public void setPayload_encoding(String payload_encoding) {
		this.payload_encoding = payload_encoding;
	}
}
