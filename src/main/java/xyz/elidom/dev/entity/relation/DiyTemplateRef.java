/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.dev.entity.relation;

import java.io.Serializable;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.entity.relation.UniqueNameStringIdRef;

@Table(name = "diy_templates", isRef = true)
public class DiyTemplateRef extends UniqueNameStringIdRef implements Serializable {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -6049066835670281452L;

	@PrimaryKey
	private String id;
	
	@Column (name = "name")
	private String name;

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
}
