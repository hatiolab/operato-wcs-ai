/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.entity;

import java.util.List;
import java.util.Map;

import xyz.elidom.dbist.annotation.ChildEntity;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DetailRemovalStrategy;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.MasterDetailType;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;
import xyz.elidom.sec.SecConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.SessionUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@Table(name = "roles", idStrategy = GenerationRule.UUID, uniqueFields="domainId,name", indexes = { 
	@Index(name = "ix_role_0", columnList = "domain_id,name", unique = true),
	@Index(name = "ix_role_1", columnList = "domain_id,updated_at") 
}, childEntities = {
	@ChildEntity(entityClass = Permission.class, type = MasterDetailType.ONE_TO_MANY, refFields = "roleId", dataProperty = "permissions", deleteStrategy = DetailRemovalStrategy.DESTROY),
	@ChildEntity(entityClass = UsersRole.class, type = MasterDetailType.ONE_TO_MANY, refFields = "roleId", dataProperty = "usersroles", deleteStrategy = DetailRemovalStrategy.DESTROY)
})
public class Role extends ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -8872776796616029881L;
	/**
	 * 사용자 역할 조회 쿼리 
	 */
	private static final String USER_ROLES_QUERY = "SELECT ID, NAME, DESCRIPTION FROM ROLES WHERE DOMAIN_ID = :domainId and ID IN (SELECT ROLE_ID FROM USERS_ROLES WHERE USER_ID = :userId)";

	/**
	 * 사용자 도메인 권한 조회 쿼리
	 */
	private static final String PERMITTED_DOMAINS_QUERY = "SELECT DISTINCT DOMAIN_ID FROM ROLES WHERE ID IN (SELECT ROLE_ID FROM USERS_ROLES WHERE USER_ID = :userId)";

	@PrimaryKey
	@Column(name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column(name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String name;

	@Column(name = "description", length = OrmConstants.FIELD_SIZE_DESCRIPTION)
	private String description;

	public Role() {
	}

	public Role(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
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
	 * @param name
	 *            the name to set
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
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * 사용자 권한 목록 추출
	 * 
	 * @param userId
	 * @return
	 */
	public static List<Role> getRoles(String userId) {
		Map<String, Object> paramMap = ValueUtil.newMap("domainId,userId", Domain.currentDomainId(), userId);
		return BeanUtil.get(IQueryManager.class).selectListBySql(USER_ROLES_QUERY, paramMap, Role.class, 0, 0);
	}
	
	/**
	 * 접근 가능한 Domain 목록 추출
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Long> getPermittedDomains(String userId) {
		List<Long> permittedDomains = (List<Long>) SessionUtil.getAttribute(SecConstants.PERMITTED_DOMAINS);
		if (ValueUtil.isEmpty(permittedDomains)) {
			Map<String, Object> paramMap = ValueUtil.newMap("userId", userId);
			permittedDomains = BeanUtil.get(IQueryManager.class).selectListBySql(PERMITTED_DOMAINS_QUERY, paramMap, Long.class, 0, 0);

			// Session에 저장.
			SessionUtil.setAttribute(SecConstants.PERMITTED_DOMAINS, permittedDomains);
		}
		return permittedDomains;
	}
}