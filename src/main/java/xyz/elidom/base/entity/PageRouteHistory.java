package xyz.elidom.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.entity.basic.DomainCreateStampHook;

@Table(name = "page_route_histories", idStrategy = GenerationRule.UUID, indexes = {
		@Index(name = "ix_page_route_history_0", columnList = "domain_id,type,route"),
		@Index(name = "ix_page_route_history_1", columnList = "domain_id,type,creator_id"),
		@Index(name = "ix_page_route_history_2", columnList = "domain_id,type,created_at"),
		@Index(name = "ix_page_route_history_3", columnList = "domain_id,type,creator_id,created_at")
})
public class PageRouteHistory extends DomainCreateStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 595086585097566037L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "type", nullable = false, length = 20)
	private String type;

	@Column (name = "route", nullable = false)
	private String route;

	@Column (name = "uri")
	private String uri;

	@Column (name = "ip", length = 40)
	private String ip;

	@Column (name = "agent")
	private String agent;

	@Column (name = "year")
	private Integer year;

	@Column (name = "month")
	private Integer month;

	@Column (name = "day")
	private Integer day;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}	
}
