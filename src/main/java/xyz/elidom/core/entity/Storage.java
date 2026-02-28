/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.entity;

import java.util.List;
import java.util.Map;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@Table(name = "storage_infos", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,name", indexes = { 
	@Index(name = "ix_storage_0", columnList = "domain_id,name", unique = true),
	@Index(name = "ix_storage_1", columnList = "domain_id,path", unique = true),
	@Index(name = "ix_storage_2", columnList = "domain_id,category")
})
public class Storage extends ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -5646331298929209880L;

	@PrimaryKey
	@Column(name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column(name = "description", nullable = false, length = OrmConstants.FIELD_SIZE_DESCRIPTION)
	private String description;

	@Column(name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String name;

	@Column(name = "dir_rule", length = 20)
	private String dirRule;

	@Column(name = "path", nullable = false, length = OrmConstants.FIELD_SIZE_FILE_PATH)
	private String path;

	@Column(name = "default_flag")
	private Boolean defaultFlag;

	@Column(name = "category", length = OrmConstants.FIELD_SIZE_CATEGORY)
	private String category;

	@Column(name = "resource_flag")
	private Boolean resourceFlag;

	@Column(name = "public_flag")
	private Boolean publicFlag;
	
	@Column(name = "tag", length = 128)
	private String tag;

	@Ignore
	private List<Attachment> items;

	public Storage() {
	}
	
	public Storage(String id) {
		this.id = id;
	}
	
	public Storage(Long domainId, String name) {
		this.domainId = domainId;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDirRule() {
		return dirRule;
	}

	public void setDirRule(String dirRule) {
		this.dirRule = dirRule;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Boolean getResourceFlag() {
		return resourceFlag;
	}

	public void setResourceFlag(Boolean resourceFlag) {
		this.resourceFlag = resourceFlag;
	}

	public Boolean getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(Boolean publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public List<Attachment> getItems() {
		return items;
	}

	public void setItems(List<Attachment> items) {
		this.items = items;
	}
	
	@Override
	public void beforeCreate() {
		super.beforeCreate();
		
		this.checkDuplicated(true);
	}
	
	@Override
	public void beforeUpdate() {
		super.beforeUpdate();
		
		this.checkDuplicated(false);
	}
	
	private void checkDuplicated(boolean createFlag) {
		IQueryManager queryMan = BeanUtil.get(IQueryManager.class);
		int minSize = createFlag ? 0 : 1;
		
		// 1. path가 중복되는 것이 있는지 체크 
		Map<String, Object> params = ValueUtil.newMap("domainId,path", this.domainId, this.path);
		Integer size = queryMan.selectSizeBySql("select id from storage_infos where domain_id = :domainId and path = :path", params);
		
		if(size > minSize) {
			throw ThrowUtil.newDataDuplicated(MessageUtil.getTermByEntity("Storage", "path"), this.path);
		}
		
		// 2. Default Storage 이고 이미 Default Storage가 존재하는지 체크 
		if(this.defaultFlag != null && this.defaultFlag == true) {
			params.remove("path");
			params.put("defaultFlag", true);
			size = queryMan.selectSizeBySql("select id from storage_infos where domain_id = :domainId and default_flag = :defaultFlag", params);
			
			if(size > minSize) {
				throw ThrowUtil.newDataDuplicated(MessageUtil.getTermByEntity("Storage", "default_flag"), String.valueOf(this.defaultFlag));
			}
		}
		
		// 3. Resource Storage 이고 이미 Resource Storage가 존재하는지 체크
		if(this.resourceFlag != null && this.resourceFlag == true) {
			params.remove("defaultFlag");
			params.put("resourceFlag", true);
			size = queryMan.selectSizeBySql("select id from storage_infos where domain_id = :domainId and resource_flag = :resourceFlag", params);
			
			if(size > minSize) {
				throw ThrowUtil.newDataDuplicated(MessageUtil.getTermByEntity("Storage", "resource_flag"), String.valueOf(this.resourceFlag));
			}			
		}		
	}
	
}