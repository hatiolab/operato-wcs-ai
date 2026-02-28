package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "page_preferences", idStrategy = GenerationRule.UUID, uniqueFields="userId,page,element,domainId", indexes = {
	@Index(name = "ix_page_preferences_0", columnList = "user_id,page,element,domain_id", unique = true)
})
public class PagePreference extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 339283770673112518L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "user_id", nullable = false, length = 40)
	private String userId;

	@Column (name = "page", nullable = false)
	private String page;

	@Column (name = "element", nullable = false)
	private String element;

	@Column (name = "preference", nullable = false, type = ColumnType.TEXT)
	private String preference;
  
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

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public String getPreference() {
		return preference;
	}

	public void setPreference(String preference) {
		this.preference = preference;
	}	
}
