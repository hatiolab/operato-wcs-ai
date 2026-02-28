package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.sec.entity.relation.RoleRef;

@Table(name = "lite_menus", idStrategy = GenerationRule.UUID, indexes = { 
	@Index(name = "ix_lite_menus_0", columnList = "domain_id,name", unique = true),
	@Index(name = "ix_lite_menus_1", columnList = "domain_id,app_name"),
	@Index(name = "ix_lite_menus_2", columnList = "domain_id,active"),
})
public class LiteMenu extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 64838235123502983L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "name", nullable = false)
	private String name;

	@Column (name = "description")
	private String description;
	
	@Column (name = "app_name")
	private String appName;
	
	@Column (name = "rank")
	private Integer rank;
	
	@Column (name = "type")
	private String type;
	
	@Column (name = "value")
	private String value;
	
	@Column (name = "icon")
	private String icon;
	
	@Column (name = "active")
	private Boolean active;
	
	@Column (name = "parent")
	private String parent;
	
	@Column(name = "role_id", length = 40)
	private String roleId;

	@Relation(field = "roleId")
	private RoleRef role;

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

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public RoleRef getRole() {
		return role;
	}

	public void setRole(RoleRef role) {
		this.role = role;

		if(this.role != null) {
			String refId = this.role.getId();
			if (refId != null) {
				this.roleId = refId;
			}
		}
	}

}
