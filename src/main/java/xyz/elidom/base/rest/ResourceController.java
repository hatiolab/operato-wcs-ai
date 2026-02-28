/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.base.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.entity.ResourceColumn;
import xyz.elidom.base.system.meta.ResourceMetaService;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/entities")
@ServiceDesc(description="Entity Service API")
public class ResourceController extends ResourceMetaService {
	
	@GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Entity (Pagination) By Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {

		if(ValueUtil.isEmpty(sort)) {
			sort = INDEX_DEFAULT_SORT;
		}
		
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}
	
	/**
	 * 엔티티 이름으로 Resource Code 컴포넌트에서 필요한 데이터 정보 조회 리턴
	 * 
	 * @param entityName
	 * @return
	 */
	@GetMapping(value="/{name}/search_records_as_code", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Resource Data as Code")	
	public List<CodeDetail> searchResourceDataAsCode(@PathVariable("name") String entityName) {
		return this.searchResourceDataAsCodeBy(entityName, null);
	}
	
	/**
	 * 메뉴 컬럼 ID로 Resource Code 컴포넌트에서 필요한 데이터 정보 조회 리턴
	 * 
	 * @param menuColumnId
	 * @return
	 */
	@GetMapping(value="/search_records_as_code/menu_column/{menu_column_id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Resource Data as Code")	
	public List<CodeDetail> searchResourceDataAsCodeByMenuColumn(@PathVariable("menu_column_id") String menuColumnId) {
		MenuColumn menuColumn = this.queryManager.select(MenuColumn.class, menuColumnId);
		return this.searchResourceDataAsCodeByMenuColumn(menuColumn);
	}
	
	/**
	 * 엔티티 컬럼 ID로 Resource Code 컴포넌트에서 필요한 데이터 정보 조회 리턴
	 * 
	 * @param entityColumnId
	 * @return
	 */
	@GetMapping(value="/search_records_as_code/entity_column/{entity_column_id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Resource Data as Code")	
	public List<CodeDetail> searchResourceDataAsCodeByEntityColumn(@PathVariable("entity_column_id") String entityColumnId) {
		ResourceColumn entityColumn = this.queryManager.select(ResourceColumn.class, entityColumnId);
		return this.searchResourceDataAsCodeByEntityColumn(entityColumn);
	}
	
	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Entity By ID")
	@Cacheable(cacheNames="Resource", condition="#p1 == null", key="'Resource-' + #p0")
	public Resource findOne(@PathVariable("id") String id, @RequestParam(name = "name", required = false) String name) {
		if(SysConstants.SHOW_BY_NAME_METHOD.equalsIgnoreCase(id)) {
			AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
			return this.selectByCondition(true, Resource.class, new Resource(Domain.currentDomainId(), name));
		} else {
			return this.getOne(true, this.entityClass(), id);
		}
	}
	
	@GetMapping(value="/{id}/exist", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check if Entity exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@PostMapping(value = "/check_import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<Resource> checkImport(@RequestBody List<Resource> list) {
		for (Resource item : list) {
			this.checkForImport(Resource.class, item);
		}
		
		return list;
	}
	
	@GetMapping(value="/export/{bundle}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Export Entity && Entity Columns")
	public Object export(HttpServletRequest request, HttpServletResponse response, @PathVariable("bundle") String bundle) {
		Workbook workbook = this.exportExcel(bundle);
		return this.excelDownloader.handleRequest(request, response, bundle, workbook);
	}
	
	@PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create Entity")
	public Resource create(@RequestBody Resource resource) {
		return this.createOne(resource);
	}
	
	@PutMapping(value="/{id}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update Entity")
	@CachePut(cacheNames="Resource", key="'Resource-' + #p0")
	public Resource update(@PathVariable("id") String id, @RequestBody Resource resource) {
		return this.updateOne(resource);
	}

	@DeleteMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete Entity")
	@CacheEvict(cacheNames="Resource", key="'Resource-' + #p0")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}
	
	@PostMapping(value="/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple Entity at one time")
	public Boolean multipleUpdate(@RequestBody List<Resource> resourceList) {
		return this.updateMultipleData(resourceList);
	}
	
	@GetMapping(value="/search_with_details", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search all data with details")
	public List<Map<String, Object>> indexWithDetails(
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query,
			@RequestParam(name = "include_default_fields", required = false) boolean includeDefaultFields) {
		
		if(ValueUtil.isEmpty(sort)) {
			sort = INDEX_WITH_DETAILS_DEFAULT_SORT;
		}
		
		Page<?> pageResult = this.search(this.entityClass(), page, limit, OrmConstants.ENTITY_FIELD_ID, sort, query);
		List<?> list = pageResult.getList();
		
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		for(Object data : list) {
			String id = ((Resource)data).getId();
			results.add(this.findDetails(id, includeDefaultFields));
		}
		
		return results;
	}
	
	@GetMapping(value = "/{id}/include_details", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find One included all details by ID")
	public Map<String, Object> findDetails(@PathVariable("id") String id, @RequestParam(name = "include_default_fields", required = false) boolean includeDefaultFields) {
		return this.findOneIncludedDetails(id, includeDefaultFields);
	}
	
	@PostMapping(value="/{id}/create_default_columns", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create Default Entity Columns")
	@CachePut(cacheNames="ResourceColumn", key="'ResourceColumn-' + #p0")
	public Resource checkResourceColumns(@PathVariable("id") String id) {
		Resource resource = this.getOne(true, this.entityClass(), id);
		return this.createDefaultResourceColumns(resource);
	}
	
	@PostMapping(value="/{id}/update_multiple_entity_columns", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple Entity Columns at one time")
	@CachePut(cacheNames="ResourceColumn", key="'ResourceColumn-' + #p0")
	public Resource updateMultipleColumns(@PathVariable("id") String id, @RequestBody List<ResourceColumn> resourceColumnList) {
		Resource resource = this.getOne(true, this.entityClass(), id);
		return this.updateResourceColumns(resource, resourceColumnList);
	}
	
	@GetMapping(value="/{id}/entity_columns", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Entity Columns By Entity ID")
	@Cacheable(cacheNames="ResourceColumn", key="'ResourceColumn-' + #p0")
	public Resource resourceColumns(@PathVariable("id") String id) {
		Resource resource = this.getOne(true, this.entityClass(), id);
		resource.resourceColumns();
		return resource;
	}
	
	@GetMapping(value="/{name}/meta", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Entity Columns By Entity Name")
	public Resource resourceColumnsByMeta(@PathVariable("name") String name, @RequestParam(name = "columns_off", required = false) boolean columnsOff) {
		return this.getResourceMeta(Domain.currentDomainId(), name, columnsOff);
	}
	
	/**
	 * 엔티티 & 엔티티 컬럼 정보로 부터 화면 구성을 위한 메타 정보로 빌드하여 리턴
	 * 
	 * @param entityName
	 * @param codeOnSearchForm
	 * @param columnsOff
	 * @return
	 */
	@Transactional(readOnly=true, propagation=Propagation.NEVER)
	@GetMapping(value="/{entity_name}/screen_menu_meta", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find operato all meta data of the entity")
	public Map<String, Object> operatoEntityMeta(
			@PathVariable("entity_name") String entityName, 
			@RequestParam(name = "codes_on_search_form", required = false) boolean codeOnSearchForm,
			@RequestParam(name = "columns_off", required = false) boolean columnsOff) {
		
	    ResourceController ctrl = BeanUtil.get(ResourceController.class);
	    Resource resource = ctrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, entityName);
	    return this.getScreenMeta(resource, codeOnSearchForm, columnsOff);
	}
	
    @PostMapping(value="/{id}/register_terms", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Register unregistered terminologies By Entity ID")
    public BaseResponse registerTerminologies(@PathVariable("id") String id) {
        // 1. Resource 조회
        Resource resource = this.queryManager.select(Resource.class, id);
        this.resourceColumns(id);
        this.translateResourceColumns(resource);
        
        // 2. Clear Cache Resource Column
        BeanUtil.get(ResourceController.class).clearResourceColumnCache();
        
        // 3. 결과 리턴 
        return new BaseResponse(true, "ok");
    }
    
	@PostMapping(value="/{id}/create_entity_columns", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create Entity Columns By Entity ID")
	public Resource syncResourceColumnsWithEntity(@PathVariable("id") String id) {
		// 1. Resource 데이터 추출 
		this.syncTableAndResourceColumns(id, true, false);
		
		// 2. Clear Cache Resource Column
		BeanUtil.get(ResourceController.class).clearCache();
		
		// 3. Resource Column 조회 
		return BeanUtil.get(ResourceController.class).resourceColumns(id);
	}
	
	@PostMapping(value="/{id}/sync_resource/from_menu", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Syncrhonize Entity Columns By Menu")
	public BaseResponse syncFromMenu(@PathVariable("id") String id) {
		// 1. id로 엔티티를 조회하여 엔티티 명과 동일한 메뉴를 찾아서 메뉴 정보를 소스로 엔티티 정보를 타겟으로 동기화
		this.syncResourceFromMenu(id);
		
		// 2. Clear Cache Resource Column
		BeanUtil.get(ResourceController.class).clearCache();
		
		// 3. Resource Column 조회 
		return new BaseResponse(true, "ok");
	}
	
	@PostMapping(value="/{id}/sync_resource/to_other_domains", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Syncronize entities to other domains")
	public BaseResponse syncReourceToOtherDomains(@PathVariable("id") String id) {
		// 1. Resource 조회
		this.syncWithOtherDomains(id);
		
		// 2. Clear Cache Resource Column
		BeanUtil.get(ResourceController.class).clearCache();
		
		// 3. Resource Column 조회 
		return new BaseResponse(true, "ok");
	}
	
	@PutMapping(value="/sync_managed_columns", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Synchronize multiple Table, Entity, Menu Columns at one time")
	public String syncManagedColumnsWithEntity(@RequestBody List<String> resourceIdList) {
		for(String resourceId : resourceIdList) {
			this.syncTableAndResourceColumns(resourceId, true, true);
		}
		
		BeanUtil.get(ResourceController.class).clearCache();
		BeanUtil.get(MenuController.class).clearCache();
		return SysConstants.OK_STRING;
	}	
	
	@PutMapping(value = "/clear_resource_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	@CacheEvict(cacheNames = "Resource", allEntries = true)
	public boolean clearResourceCache() {
		return true;
	}
	
	@PutMapping(value = "/clear_resource_column_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	@CacheEvict(cacheNames = "ResourceColumn", allEntries = true)
	public boolean clearResourceColumnCache() {
		return true;
	}
	
	@PutMapping(value = "/clear_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean resourceClearCache() {
		return BeanUtil.get(DomainController.class).requestClearCache("resource");
	} 
	
	@ApiDesc(description = "Find Extends Entity By Name")
	@Cacheable(cacheNames = "Resource", key = "'Resource-custom-' + #p0")
	public Resource findExtResource(String name) {
		return this.findExtendedResource(Domain.currentDomainId(), name);
	}
	
    @GetMapping(value="/find_by_name/{name}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Find Entity By Name")
    @Cacheable(cacheNames="Resource", key="#p0")
    public Resource findByName(@PathVariable("name") String name) {
        AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
        return this.selectByCondition(true, Resource.class, new Resource(Domain.currentDomainId(), name));
    }
}