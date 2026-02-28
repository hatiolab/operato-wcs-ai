/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.dev.entity;

import java.util.HashMap;
import java.util.Map;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dev.rest.IdRuleController;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.engine.IScriptEngine;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;

@Table(name = "id_rules", idStrategy = GenerationRule.MEANINGFUL, meaningfulFields = "domainId,name", uniqueFields = "domainId,name", indexes = { 
	@Index(name = "ix_id_rules_0", columnList = "domain_id,name", unique = true)
})
public class IdRule extends ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -8690446356895099623L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column (name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String name;

	@Column (name = "description", length = OrmConstants.FIELD_SIZE_DESCRIPTION)
	private String description;

	@Column (name = "logic", type = ColumnType.TEXT)
	private String logic;
	
	public IdRule() {
	}
	
	public IdRule(String id) {
		this.id = id;
	}
		
	public IdRule(Long domainId, String name) {
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

	public String getLogic() {
		return logic;
	}

	public void setLogic(String logic) {
		this.logic = logic;
	}

	@Override
	public Object findAndSetId() {
		return this.getId();
	}
	
	/**
	 * query로 새로운 아이디를 생성한다. 
	 * 
	 * @param query
	 * @return
	 */
	public String createId(String query) {
		@SuppressWarnings("unchecked")
		Map<String, Object> inputMap = ValueUtil.isNotEmpty(query) ? FormatUtil.jsonToObject(query, Map.class) : new HashMap<String, Object>();
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		Object newId = scriptEngine.runScript("groovy", this.getLogic(), inputMap);
		return newId.toString();
	}
	
	/**
	 * query로 새로운 아이디를 생성한다. 
	 * 
	 * @param query
	 * @return
	 */
	public String generateId(Map<String, Object> params) {
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		Object newId = scriptEngine.runScript("groovy", this.getLogic(), params);
		return newId.toString();
	}	
	
	/**
	 * name으로 IdRule을 찾고 query 파라미터로 새로운 아이디를 생성한다.
	 * 
	 * @param name
	 * @param query
	 * @return
	 */
	public static String createId(String name, String query) {
		IdRule idRule = BeanUtil.get(IdRuleController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, name);
		return idRule == null ? null : idRule.createId(query);
	}
	
	public static String generateId(String name, Map<String, Object> params) {
		IdRule idRule = BeanUtil.get(IdRuleController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, name);
		return idRule == null ? null : idRule.generateId(params);
	}
}