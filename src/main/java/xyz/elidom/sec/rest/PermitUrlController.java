package xyz.elidom.sec.rest;

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
import xyz.elidom.sec.entity.PermitUrl;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/permit_urls")
@ServiceDesc(description = "PermitUrl Service API")
public class PermitUrlController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return PermitUrl.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public PermitUrl findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	@CachePut(cacheNames = "PermitUrl", keyGenerator = "namedUpdateApiKeyGenerator")
	public PermitUrl create(@RequestBody PermitUrl input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	@CachePut(cacheNames = "PermitUrl", keyGenerator = "namedUpdateApiKeyGenerator")
	public PermitUrl update(@PathVariable("id") String id, @RequestBody PermitUrl input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	@CacheEvict(cacheNames = "PermitUrl", keyGenerator = "namedUpdateApiKeyGenerator")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.getClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	@CacheEvict(cacheNames = "PermitUrl", allEntries = true)
	public Boolean multipleUpdate(@RequestBody List<PermitUrl> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@Cacheable(cacheNames = "PermitUrl", key = "'PermitUrl-All-' + #domainId")
	public List<String> listAllPermitURL(Long domainId) {
		String sql = "SELECT DISTINCT NAME FROM PERMIT_URLS WHERE DOMAIN_ID = :domainId AND TYPE = 'ALL' AND ACTIVE = :active";
		return queryManager.selectListBySql(sql, ValueUtil.newMap("domainId,active", domainId,true), String.class, 0, 0);
	}

	@Cacheable(cacheNames = "PermitUrl", key = "'PermitUrl-Read-' + #domainId")
	public List<String> listReadOnlyURL(Long domainId) {
		String sql = "SELECT DISTINCT NAME FROM PERMIT_URLS WHERE DOMAIN_ID = :domainId AND TYPE = 'READ_ONLY' AND ACTIVE = :active";
		return queryManager.selectListBySql(sql, ValueUtil.newMap("domainId,active", domainId,true), String.class, 0, 0);
	}
}