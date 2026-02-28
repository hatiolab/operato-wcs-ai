package xyz.anythings.base.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import xyz.anythings.base.entity.Invoice;
import xyz.anythings.base.entity.Stage;
import xyz.anythings.base.service.api.IInvoiceNoService;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/invoices")
@ServiceDesc(description = "Invoice Service API")
public class InvoiceController extends AbstractRestService {

	/**
	 * 송장 번호 생성 서비스
	 */
	@Autowired
	private IInvoiceNoService invoiceNoService;
	
	@Override
	protected Class<?> entityClass() {
		return Invoice.class;
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
	public Invoice findOne(@PathVariable("id") String id) {
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
	public Invoice create(@RequestBody Invoice input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Invoice update(@PathVariable("id") String id, @RequestBody Invoice input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Invoice> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value = "/create_by_range", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create Invoices by range")
	public Map<String, Object> createInvoiceRange(@RequestBody Map<String, Object> range) {
		String stageCd = ValueUtil.isEmpty(range.get("stage_cd")) ? null : ValueUtil.toString(range.get("stage_cd"));
		String comCd = ValueUtil.isEmpty(range.get("com_cd")) ? null : ValueUtil.toString(range.get("com_cd"));
		String customerCd = ValueUtil.isEmpty(range.get("customer_cd")) ? null : ValueUtil.toString(range.get("customer_cd"));
		Long from = ValueUtil.toLong(range.get("from"));
		Long to = ValueUtil.toLong(range.get("to"));
		Long domainId = Domain.currentDomainId();
		
		if(stageCd == null) {
			List<Stage> stageList = this.queryManager.selectList(Stage.class, ValueUtil.newMap("domainId", domainId));
			stageCd = stageList.get(0).getStageCd();
		}
		
		int count = this.invoiceNoService.generateInvoiceNo(domainId, stageCd, comCd, customerCd, from, to);
		return ValueUtil.newMap("success,count", true, count);
	}

}