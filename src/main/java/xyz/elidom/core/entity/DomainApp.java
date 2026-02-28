/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.entity;


import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;

@Table(name = "domain_apps", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,name", indexes = { 
	@Index(name = "ix_domain_app_0", columnList = "domain_id,name", unique = true)
})
public class DomainApp extends ElidomStampHook {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 604096888126412653L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column (name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String name;

	@Column (name = "description", length = OrmConstants.FIELD_SIZE_DESCRIPTION)
	private String description;

	@Column (name = "brand_name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String brandName;

	@Column (name = "brand_image", length = OrmConstants.FIELD_SIZE_UUID)
	private String brandImage;

	@Column (name = "theme", length = OrmConstants.FIELD_SIZE_NAME)
	private String theme;
	
	public DomainApp() {
	}
	
	public DomainApp(String id) {
		this.id = id;
	}
	
	public DomainApp(Long domainId, String name) {
		this.domainId = domainId;
		this.name = name;
	}
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getBrandImage() {
		return brandImage;
	}

	public void setBrandImage(String brandImage) {
		this.brandImage = brandImage;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}
}