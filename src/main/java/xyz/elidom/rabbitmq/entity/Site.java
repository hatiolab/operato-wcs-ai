package xyz.elidom.rabbitmq.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 사이트 관리 
 * @author yang
 *
 */
@Table(name = "mq_site", idStrategy = GenerationRule.UUID)
public class Site extends xyz.elidom.orm.entity.basic.ElidomStampHook{
	
	private static final long serialVersionUID = 5827241366361461547L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	/**
	 * 사이트 코드 : rabbitmq vhost 와 맵핑 
	 */
	@Column(name = "site_code", nullable = false, length = 255, unique= true)
	private String siteCode;

	/**
	 * 사이트 명칭 
	 */
	@Column(name = "site_name", nullable = false, length = 255, unique= true)
	private String siteName;

	/**
	 * 트레이스 기능 사용 유무 
	 */
	@Column(name = "use_trace")
	private Boolean useTrace;
	
	
	/**
	 * 브로커에 존재 유무
	 */
	@Ignore
	private Boolean existsBroker;
	/**
	 * 브로커 수신 네트워크 사용량 
	 */
	@Ignore
	private String networkFromClient;
	/**
	 * 브로커 송신 네트워크 사용량 
	 */
	@Ignore
	private String networkToClient;
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSiteCode() {
		return siteCode;
	}

	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public Boolean getExistsBroker() {
		return existsBroker;
	}

	public void setExistsBroker(Boolean existsBroker) {
		this.existsBroker = existsBroker;
	}

	public String getNetworkFromClient() {
		return networkFromClient;
	}

	public void setNetworkFromClient(String networkFromClient) {
		this.networkFromClient = networkFromClient;
	}

	public String getNetworkToClient() {
		return networkToClient;
	}

	public void setNetworkToClient(String networkToClient) {
		this.networkToClient = networkToClient;
	}

	public Boolean getUseTrace() {
		return useTrace;
	}

	public void setUseTrace(Boolean useTrace) {
		this.useTrace = useTrace;
	}
}
