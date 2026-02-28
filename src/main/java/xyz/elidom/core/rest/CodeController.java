/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.core.entity.Code;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.core.model.CodeLabel;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/common_codes")
@ServiceDesc(description="CommonCode Service API")
public class CodeController extends AbstractRestService {
	
	@Override
	protected Class<?> entityClass() {
		return Code.class;
	}	
	
	@GetMapping( produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search CommonCode (Pagination) by Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}
	
	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find one CommonCode by ID")
	public Code findOne(@PathVariable("id") String id, @RequestParam(name = "name", required = false) String name) {
		// 1. locale 추출
		String locale = User.currentUser().getLocale();
		locale = locale.split(SysConstants.DASH)[0];
		
		// 2. id가 공통 코드 id 이면
		if(!SysConstants.SHOW_BY_NAME_METHOD.equalsIgnoreCase(id)) {
			Code code = this.getOne(true, this.entityClass(), id);
			name = code.getName();
		}
		
		// 3. 캐쉬를 통한 언어별 공통 코드 조회 
		return BeanUtil.get(CodeController.class).findByName(Domain.currentDomainId(), locale, name);
	}
	
	/**
	 * 코드 상세 조회 (공통 코드 편집기 용)
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value="/{id}/find_for_edit", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find one CommonCode by ID")
	public Code findForEdit(@PathVariable("id") String id) {
		// 1. locale 추출
		String locale = User.currentUser().getLocale();
		locale = locale.split(SysConstants.DASH)[0];
		
		// 2. 공통 코드 조회
		Query query = new Query();
		query.addFilter("id", id);
		query.addSelect("id", "name");
		Code code = this.queryManager.selectByCondition(Code.class, query);
		
		// 3. 코드 상세 조회
		query = new Query();
		query.addFilter(new Filter("parentId", id));
		query.addOrder("rank", true);
		query.addOrder("name", true);
		query.addSelect("id", "rank", "name", "description");
		List<CodeDetail> codeItems = this.queryManager.selectList(CodeDetail.class, query); 		
		code.setItems(codeItems);
		
		// 4. 코드 리턴
		return code;
	}
	
	@GetMapping(value="/{id}/exist", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check if CommonCode exists by ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@PostMapping(value = "/check_import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<Code> checkImport(@RequestBody List<Code> list) {
		for (Code item : list) {
			this.checkForImport(Code.class, item);
		}
		
		return list;
	}
	
	@PostMapping( consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create CommonCode")
	@CachePut(cacheNames="CommonCode", keyGenerator="namedUpdateApiKeyGenerator")
	public Code create(@RequestBody Code commonCode) {
		return this.createOne(commonCode);
	}
	
	@PutMapping(value="/{id}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update CommonCode")
	@CachePut(cacheNames="CommonCode", keyGenerator="namedUpdateApiKeyGenerator")
	public Code update(@PathVariable("id") String id, @RequestBody Code commonCode) {
		return this.updateOne(commonCode);
	}
	
	@GetMapping(value="/search_with_details", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search all data with details")
	public List<Map<String, Object>> indexWithDetails(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query,
			@RequestParam(name = "include_default_fields", required = false) boolean includeDefaultFields) {
		
		Page<?> pageResult = this.search(this.entityClass(), page, limit, OrmConstants.ENTITY_FIELD_ID, sort, query);
		List<?> list = pageResult.getList();
		
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		for(Object data : list) {
			String id = ((Code)data).getId();
			results.add(this.findDetails(id, includeDefaultFields));
		}
		
		return results;
	}

	@DeleteMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete CommonCode by ID")
	public boolean delete(@PathVariable("id") String id) {
		Code code = this.getOne(true, this.entityClass(), id);
		return BeanUtil.get(CodeController.class).deleteCode(code);
	}
	
	@CacheEvict(cacheNames="CommonCode", keyGenerator="namedUpdateApiKeyGenerator")
	public boolean deleteCode(Code code) {
		this.queryManager.delete(code);
		return true;
	}
	
	@PostMapping(value="/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple CommonCode at one time")
	@CacheEvict(cacheNames="CommonCode", allEntries=true)
	public Boolean multipleUpdate(@RequestBody List<Code> commonCodeList) {
		return this.cudMultipleData(this.entityClass(), commonCodeList);
	}
	
	@GetMapping(value = "/{id}/include_details", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find One included all details by ID")
	public Map<String, Object> findDetails(@PathVariable("id") String id, @RequestParam(name = "include_default_fields", required = false) boolean includeDefaultFields) {
		return this.findOneIncludedDetails(id, includeDefaultFields);
	}
	
	@GetMapping(value="/{id}/codes", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Sub Codes By Common Code ID")
	public List<CodeDetail> findSubCodes(@PathVariable("id") String id) {
		Query query = new Query();
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_DOMAIN_ID, Domain.currentDomainId()));
		query.addFilter(new Filter("parentId", id));
		query.addOrder("rank", true);
		return this.queryManager.selectList(CodeDetail.class, query);
	}	
	
	@PostMapping(value="/{id}/codes/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple Sub codes at one time")
	@CacheEvict(cacheNames="CommonCode", allEntries = true)
	public Boolean multipleUpdateCodes(@PathVariable("id") String id, @RequestBody List<CodeDetail> commonDetailCodeList) {
		for(CodeDetail detail : commonDetailCodeList) {
			if(ValueUtil.isEmpty(detail.getParentId())) {
				detail.setParentId(id);
			}
		}
		
		return this.cudMultipleData(CodeDetail.class, commonDetailCodeList);
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value="/codes/{code_detail_id}/multi_descriptions", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Sub Code Multilingual descriptions By Sub Code ID")
	public List<CodeLabel> searchSubcodeMultilingual(@PathVariable("code_detail_id") String codeDetailId) {
		// 1. Find sub code
		CodeDetail codeDetail = this.queryManager.select(CodeDetail.class, codeDetailId);
		List<CodeLabel> multilingualLabels = new ArrayList<CodeLabel>();
		
		// 2. 기준 언어 코드 정보 조회
		Code code = BeanUtil.get(CodeController.class).findByName(codeDetail.getDomainId(), SysConstants.LANG_EN, SysConstants.LANGUAGE_CODE);
		List<CodeDetail> codeDetails = code.getItems();
		Map<String, String> labelData = codeDetail.getLabels() != null ? FormatUtil.jsonToObject(codeDetail.getLabels(), HashMap.class) : null;
		
		// 3. Parse labels data
		for(CodeDetail cd : codeDetails) {
			String langCd = cd.getName();
			String langDesc = (labelData == null) ? SysConstants.EMPTY_STRING : (labelData.containsKey(langCd) ? labelData.get(langCd) : SysConstants.EMPTY_STRING);
			CodeLabel codeLabel = new CodeLabel(codeDetailId, langCd, langDesc);
			multilingualLabels.add(codeLabel);
		}
		
		// 4. Return multilingual descriptions
		return multilingualLabels;
	}	
	
	@SuppressWarnings("unchecked")
	@PostMapping(value="/codes/{code_detail_id}/multi_descriptions/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple Sub code multilingual description at one time")
	@CacheEvict(cacheNames="CommonCode", allEntries = true)
	public Boolean updateSubcodeMultilingual(@PathVariable("code_detail_id") String codeDetailId, @RequestBody List<CodeLabel> codeLabelList) {
		// 1. Find sub code
		CodeDetail codeDetail = this.queryManager.select(CodeDetail.class, codeDetailId);
		
		// 2. 기준 언어 코드 정보 조회
		Code code = BeanUtil.get(CodeController.class).findByName(codeDetail.getDomainId(), SysConstants.LANG_EN, SysConstants.LANGUAGE_CODE);
		List<CodeDetail> codeDetails = code.getItems();
		
		Map<String, String> originalData = 
				ValueUtil.isNotEmpty(codeDetail.getLabels()) ? FormatUtil.jsonToObject(codeDetail.getLabels(), HashMap.class) : new HashMap<String, String>();
		
		// 2. Extract data
		for(CodeDetail langCode : codeDetails) {
			String langCd = langCode.getName();
			CodeLabel newData = null;
			
			for(CodeLabel cl : codeLabelList) {
				if(ValueUtil.isEqualIgnoreCase(langCd, cl.getName())) {
					newData = cl;
					break;
				}
			}
			
			String newDesc = (newData != null) ? newData.getDescription() : originalData.containsKey(langCd) ? originalData.get(langCd) : SysConstants.EMPTY_STRING;
			originalData.put(langCd, newDesc);
		}
		
		// 3. Build multilingual description data
		String labels = FormatUtil.toJsonString(originalData);
		codeDetail.setLabels(labels);
		
		// 4. Update
		this.queryManager.update(codeDetail, "labels");
		return true;
	}
	
	@PostMapping(value="/{id}/sync_code/to_other_domains", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Syncronize common code to other domains")
	public BaseResponse syncCodeToOtherDomains(@PathVariable("id") String id) {
		// 1. Resource 조회
		Code code = this.queryManager.select(Code.class, id);
		List<CodeDetail> items = this.findSubCodes(id);
		
		// 2. Domain 조회
		List<Domain> domains = this.queryManager.selectList(Domain.class, new Domain());
		
		// 3. 도메인 별로 동일한 이름의 코드 & 코드 상세 제거
		for(Domain domain : domains) {
			if(ValueUtil.isNotEqual(code.getDomainId(), domain.getId())) {
				Code cCondition = new Code(domain.getId(), code.getName());
				Code domainCode = this.queryManager.selectByCondition(Code.class, cCondition);
				
				if(domainCode != null) {
					CodeDetail cdCondition = new CodeDetail();
					cdCondition.setDomainId(domain.getId());
					cdCondition.setParentId(domainCode.getId());
					this.queryManager.deleteList(CodeDetail.class, cdCondition);
					this.queryManager.delete(domainCode);
				}
			}
		}
		
		// 4. 도메인 별로 코드 복사
		for(Domain domain : domains) {
			if(ValueUtil.isNotEqual(code.getDomainId(), domain.getId())) {
				Code domainCode = ValueUtil.populate(code, new Code());
				domainCode.setDomainId(domain.getId());
				domainCode.setId(null);
				this.queryManager.insert(domainCode);
				List<CodeDetail> newItems = new ArrayList<CodeDetail>(items.size());
				
				for(CodeDetail item : items) {
					CodeDetail domainItem = ValueUtil.populate(item, new CodeDetail());
					domainItem.setDomainId(domain.getId());
					domainItem.setParentId(domainCode.getId());
					domainItem.setId(null);
					newItems.add(domainItem);
				}
				
				this.queryManager.insertBatch(newItems);
			}
		}
		
		// 5. Clear Cache Resource Column
		BeanUtil.get(CodeController.class).clearCache();
		
		// 6. Resource Column 조회 
		return new BaseResponse(true, "ok");
	}
	
	@PutMapping(value = "/clear_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean codeClearCache() {
		return BeanUtil.get(DomainController.class).requestClearCache("code");
	}

	
	@ApiDesc(description = "Clear CommonCode Cache")	
	@CacheEvict(cacheNames = "CommonCode", allEntries = true)
	public boolean clearCache() {
		return true;
	}
	
	@ApiDesc(description="Find one CommonCode by Name")
	@Cacheable(cacheNames = "CommonCode", key="#p0 + #p1 + #p2")
	public Code findByName(Long domainId, String locale, String name) {
		AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
		return Code.findByName(domainId, name);
	}
	
	@ApiDesc(description="Find one CommonCode by Name")
	@Cacheable(cacheNames="CommonCode", key="#domainId + '-' + #name")
	public Code findByName(Long domainId, String name) {
		AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
		return Code.findByName(domainId, name);
	}
}