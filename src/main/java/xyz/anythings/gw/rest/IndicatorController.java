package xyz.anythings.gw.rest;

import java.util.List;

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

import xyz.anythings.gw.entity.Indicator;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/indicators")
@ServiceDesc(description = "Indicator Service API")
public class IndicatorController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return Indicator.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		Query condition = this.parseQuery(this.entityClass(), page, limit, select, sort, query);
		List<Filter> filters = condition.getFilter();
		Filter equipTypeFilter = null;
		Filter equipCdFilter = null;
		
		for(Filter filter : filters) {
			if(ValueUtil.isEqualIgnoreCase(filter.getName(), "equip_type")) {
				equipTypeFilter = filter;
			} else 	if(ValueUtil.isEqualIgnoreCase(filter.getName(), "equip_cd")) {
				equipCdFilter = filter;
			}
		}
		
		if(equipCdFilter != null) {
			condition.removeFilter("equip_type");
			condition.removeFilter("equip_cd");
			
			String sql = "select ind_cd from cells where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd";
			List<String> indCdList = AnyEntityUtil.searchItems(Domain.currentDomainId(), true, String.class, sql, "domainId,equipType,equipCd", Domain.currentDomainId(), equipTypeFilter.getValue(), equipCdFilter.getValue());
			condition.addFilter("ind_cd", SysConstants.IN, indCdList);
		}
		
		return this.search(this.entityClass(), condition);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public Indicator findOne(@PathVariable("id") String id) {
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
	public Indicator create(@RequestBody Indicator input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Indicator update(@PathVariable("id") String id, @RequestBody Indicator input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Indicator> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

}