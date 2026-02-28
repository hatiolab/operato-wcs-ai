package xyz.anythings.sec.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "board_histories", idStrategy = GenerationRule.UUID, indexes = { 
	@Index(name = "ix_board_histories_0", columnList = "domain_id,name,version", unique = true),
	@Index(name = "ix_board_histories_1", columnList = "domain_id,group_id")
})
public class BoardHistory extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 369910987635895234L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "name", nullable = false)
	private String name;

	@Column (name = "description")
	private String description;
	
	@Column (name = "version")
	private Integer version = 1;

	@Column (name = "model", type = ColumnType.TEXT)
	private String model;
	
	@Column (name = "thumbnail", type = ColumnType.TEXT)
	private String thumbnail;

	@Column (name = "group_id")
	private String groupId;
	
	@Column (name = "privilege")
	private String privilege;
	
	@Column (name = "original_id", length = 50)
	private String originalId;
	
	@Column (name = "action", length = 50)
	private String action = "CREATED";
	
    @Column(name = "deleted_at", type = ColumnType.DATETIME)
    private Date deletedAt;

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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	public String getOriginalId() {
		return originalId;
	}

	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Date deletedAt) {
		this.deletedAt = deletedAt;
	}
}
