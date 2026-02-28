package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "privileges", idStrategy = GenerationRule.UUID, notnullFields="name,category", indexes = { 
	@Index(name = "ix_privileges_0", columnList = "name,category")
})
public class Privilege extends xyz.elidom.orm.entity.basic.UserTimeStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 150514255749525468L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "name", nullable = false, length = 36)
	private String name;

	@Column (name = "category", nullable = false, length = 30)
	private String category;

	@Column (name = "description")
	private String description;
  
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
