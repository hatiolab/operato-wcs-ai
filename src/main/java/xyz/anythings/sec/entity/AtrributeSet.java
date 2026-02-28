package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "attribute_sets", idStrategy = GenerationRule.UUID, indexes = { 
	@Index(name = "ix_attr_sets_0", columnList = "entity", unique = true)
})
public class AtrributeSet extends xyz.elidom.orm.entity.basic.TimeStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 369374631239283634L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "entity", nullable = false)
	private String entity;

	@Column (name = "description")
	private String description;

	@Column (name = "items", type = ColumnType.TEXT)
	private String items;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}
}
