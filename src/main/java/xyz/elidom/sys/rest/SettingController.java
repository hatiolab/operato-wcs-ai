/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sys.rest;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Setting;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.util.BeanUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/settings")
@ServiceDesc(description="Setting Service API")
public class SettingController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return Setting.class;
	}
	
	@RequestMapping(method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Setting (Pagination) By Search Conditions")	
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {		
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Setting By ID or Name")
	@Cacheable(cacheNames="Setting", condition="#p0 != null", keyGenerator="namedFindApiKeyGenerator")
	public Setting findOne(@PathVariable("id") String id, @RequestParam(name = "name", required = false) String name) {
		if(SysConstants.SHOW_BY_NAME_METHOD.equalsIgnoreCase(id)) {
			AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
			return this.selectByCondition(Setting.class, new Setting(name));
		} else {
			return this.getOne(this.entityClass(), id);
		}
	}
	
	@RequestMapping(value="/find_by_name/{name}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Setting By Name")
	@Cacheable(cacheNames="Setting", keyGenerator="namedFindApiKeyGenerator")
	public Setting findOne(@PathVariable("name") String name) {
		return this.selectByCondition(Setting.class, new Setting(name));
	}
	
	@RequestMapping(value="/{id}/exist", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check if Setting exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<Setting> checkImport(@RequestBody List<Setting> list) {
		for (Setting item : list) {
			this.checkForImport(Setting.class, item);
		}
		
		return list;
	}
	
	@RequestMapping(method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create Setting")
	@CachePut(cacheNames="Setting", keyGenerator="namedUpdateApiKeyGenerator")
	public Setting create(@RequestBody Setting setting) {
		return this.createOne(setting);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update Setting")
	@CachePut(cacheNames="Setting", keyGenerator="namedUpdateApiKeyGenerator")
	public Setting update(@PathVariable("id") String id, @RequestBody Setting setting) {
		return this.updateOne(setting);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete Setting By ID")
	@CacheEvict(cacheNames="Setting", allEntries=true)
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple Settings at one time")
	@CacheEvict(cacheNames="Setting", allEntries=true)
	public Boolean multipleUpdate(@RequestBody List<Setting> settingList) {
		return this.cudMultipleData(this.entityClass(), settingList);
	}
	
	@RequestMapping(value = "/clear_cache", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean settingsClearCache() {
		return BeanUtil.get(DomainController.class).requestClearCache("settings");
	}

	@ApiDesc(description = "Clear Settings Cache")	
	@CacheEvict(cacheNames = "Setting", allEntries = true)
	public boolean clearCache() {
		return true;
	}
	
	/**
	 * 수정1. domainId, name으로 설정값 조회 API 추가
	 * 
	 * @param domainId
	 * @param name 
	 * @return
	 */
	@Cacheable(cacheNames="Setting", key="#p0 + '-' + #p1")
	public Setting findByName(Long domainId, String name) {
		Setting condition = new Setting();
		condition.setDomainId(domainId);
		condition.setName(name);
		return this.selectByCondition(Setting.class, condition);
	}
}