/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@Table(name = "properties", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,onType,onId,propType,name", indexes = {
	@Index(name = "ix_property_0", columnList = "domain_id,on_type,on_id,prop_type,name", unique = true),
	@Index(name = "ix_property_1", columnList = "domain_id,on_type") 
})
public class Property extends ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 6041582480847360014L;

	@PrimaryKey
	@Column(name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column(name = "on_type", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String onType;

	@Column(name = "on_id", nullable = false, length = OrmConstants.FIELD_SIZE_MEANINGFUL_ID)
	private String onId;

	@Column(name = "prop_type", length = 48)
	private String propType;

	@Column(name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String name;

	@Column(name = "description", length = OrmConstants.FIELD_SIZE_DESCRIPTION)
	private String description;

	@Column(name = "value", length = OrmConstants.FIELD_SIZE_VALUE_1000)
	private String value;

	public Property() {
	}
	
	public Property(String id) {
		this.id = id;
	}

	public Property(Long domainId, String name) {
		this.domainId = domainId;
		this.name = name;
	}

	public Property(Long domainId, String onType, String onId, String propType, String name) {
		this.domainId = domainId;
		this.onType = onType;
		this.onId = onId;
		this.propType = propType;
		this.name = name;
	}
	
	public Property(String onType, String onId, String name) {
		this(Domain.currentDomain().getId(), onType, onId, null, name);
	}

	public Property(String onType, String onId, String propType, String name) {
		this(Domain.currentDomain().getId(), onType, onId, propType, name);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the onType
	 */
	public String getOnType() {
		return onType;
	}

	/**
	 * @param onType the onType to set
	 */
	public void setOnType(String onType) {
		this.onType = onType;
	}

	/**
	 * @return the onId
	 */
	public String getOnId() {
		return onId;
	}

	/**
	 * @param onId the onId to set
	 */
	public void setOnId(String onId) {
		this.onId = onId;
	}

	/**
	 * @return the propType
	 */
	public String getPropType() {
		return propType;
	}

	/**
	 * @param propType the propType to set
	 */
	public void setPropType(String propType) {
		this.propType = propType;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * property 조회 
	 * 
	 * @param input
	 * @return
	 */
	public static String getPropertyValue(Property input) {
		Property property = BeanUtil.get(IQueryManager.class).selectByCondition(Property.class, input);
		return ValueUtil.isEmpty(property) ? null : property.getValue();
	}
}