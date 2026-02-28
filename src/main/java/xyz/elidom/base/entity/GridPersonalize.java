package xyz.elidom.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 그리드 개인화 정보
 * 
 * @author shortstop
 */
@Table(name = "grid_personalize", idStrategy = GenerationRule.UUID, uniqueFields="domainId,userId,menuId", indexes = {
	@Index(name = "ix_grid_personalize_0", columnList = "domain_id,user_id,menu_id", unique = true)
})
public class GridPersonalize extends xyz.elidom.orm.entity.basic.ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -2371236028118953477L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "user_id", nullable = false, length = 40)
	private String userId;

	@Column (name = "menu_id", nullable = false, length = 40)
	private String menuId;

	@Column(name = "template", nullable = true, type = ColumnType.TEXT)
	private String template;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
}
