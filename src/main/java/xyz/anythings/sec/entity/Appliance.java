package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "appliances", idStrategy = GenerationRule.UUID, uniqueFields="domainId,name", indexes = {
	@Index(name = "ix_appliances_0", columnList = "domain_id,name", unique = true)
})
public class Appliance extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 626214033356526982L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "serial_no", length = 40)
	private String serialNo;

	@Column (name = "name", nullable = false, length = 50)
	private String name;

	@Column (name = "brand", nullable = false, length = 30)
	private String brand;

	@Column (name = "model", nullable = false, length = 50)
	private String model;

	@Column (name = "netmask", length = 20)
	private String netmask;

	@Column (name = "description")
	private String description;

	@Column (name = "access_token", length = 50)
	private String accessToken;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}
