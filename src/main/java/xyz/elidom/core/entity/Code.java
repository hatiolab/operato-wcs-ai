/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.dbist.annotation.ChildEntity;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DetailRemovalStrategy;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.MasterDetailType;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

@Table(name = "common_codes", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,name", indexes = { 
	@Index(name = "ix_code_0", columnList = "domain_id,name", unique = true),
	@Index(name = "ix_code_1", columnList = "domain_id,bundle") 
}, childEntities = {
	@ChildEntity(entityClass = CodeDetail.class, type = MasterDetailType.ONE_TO_MANY, refFields = "parentId", dataProperty = "codes", deleteStrategy = DetailRemovalStrategy.EXCEPTION)
})
public class Code extends ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -3661910818162472263L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column(name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String name;

	@Column(name = "description", length = OrmConstants.FIELD_SIZE_DESCRIPTION)
	private String description;
	
	@Column(name = "bundle", length = OrmConstants.FIELD_SIZE_CATEGORY)
	private String bundle;	

	@Ignore
	private List<CodeDetail> items;

	public Code() {
	}
	
	public Code(String id) {
		this.id = id;
	}
	
	public Code(Long domainId, String name) {
		this.domainId = domainId;
		this.name = name;
	}
	
	public Code(Long domainId, String name, String bundle) {
		this(domainId, name);
		this.bundle = bundle;
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
	 * @return the bundle
	 */
	public String getBundle() {
		return bundle;
	}

	/**
	 * @param bundle the bundle to set
	 */
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	/**
	 * @return the items
	 */
	public List<CodeDetail> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<CodeDetail> items) {
		this.items = items;
	}
	
	@Override
	public void beforeDelete() {
		super.beforeDelete();
		
		// 현재 도메인에서 공통 코드가 사용된 메뉴가 존재하는지 체크
		MenuColumn condition = new MenuColumn();
		condition.setRefType("CommonCode");
		condition.setRefName(this.getName());
		
		int count = BeanUtil.get(IQueryManager.class).selectSize(MenuColumn.class, condition);
		if(count > 0) {
			throw ThrowUtil.newValidationErrorWithNoLog("Code [" + this.name + "] is referenced at menu!");
		}
	}
	
	/**
	 * domainId, name으로 공통 코드를 조회 
	 * 
	 * @param domainId
	 * @param name
	 * @return
	 */
	public static Code findByName(Long domainId, String name) {
		IQueryManager qm = BeanUtil.get(IQueryManager.class);
		Code code = (Code) qm.selectByCondition(Code.class, new Code(domainId, name));
		
		if(code != null) {
			code.setItems(Code.subCodes(code.getId()));
			return code;
			
		} else {
			throw ThrowUtil.newNotFoundRecord("terms.menu.Code", name);
		}
	}
	
	/**
	 * 공통 코드 아이디로 CodeDetail 리스트를 찾아 조회 
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CodeDetail> subCodes(String id) {
		IQueryManager qm = BeanUtil.get(IQueryManager.class);
		Query query = new Query();
		query.addFilter(new Filter("parentId", id));
		query.addOrder("rank", true);
		query.addOrder("name", true);
		List<CodeDetail> codeItems = qm.selectList(CodeDetail.class, query);
		String langCd = SettingUtil.getUserLocale(); 
		
		for(CodeDetail item : codeItems) {
			String labelStr = item.getLabels();
			Map<String, String> labelData = ValueUtil.isNotEmpty(labelStr) ? FormatUtil.jsonToObject(labelStr, HashMap.class) : null;
			// 다국어 처리
			String desc = (labelData == null) ? item.getDescription() : (labelData.containsKey(langCd) && ValueUtil.isNotEmpty(labelData.get(langCd)) ? labelData.get(langCd) : item.getDescription());
			item.setDescription(desc);
		}
		
		return codeItems;
	}
}