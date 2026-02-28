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

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.entity.StockAdjust;
import xyz.anythings.base.entity.Stocktaking;
import xyz.anythings.base.service.impl.StockService;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/stocktakings")
@ServiceDesc(description = "Stocktaking Service API")
public class StocktakingController extends AbstractRestService {

	@Autowired
	private StockService stockService;
	
	@Override
	protected Class<?> entityClass() {
		return Stocktaking.class;
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
	public Stocktaking findOne(@PathVariable("id") String id) {
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
	public Stocktaking create(@RequestBody Stocktaking input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Stocktaking update(@PathVariable("id") String id, @RequestBody Stocktaking input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Stocktaking> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value = "/{id}/include_details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find One included all details by ID")
	public Map<String, Object> findDetails(@PathVariable("id") String id) {
		return this.findOneIncludedDetails(id);
	}

	@RequestMapping(value = "/{id}/items", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search detail list by master ID")
	public List<StockAdjust> findStockAdjust(@PathVariable("id") String id) {
		xyz.elidom.dbist.dml.Query query = new xyz.elidom.dbist.dml.Query();
		query.addFilter(new Filter("stocktakingId", id));
		return this.queryManager.selectList(StockAdjust.class, query);
	}

	@RequestMapping(value = "/{id}/items/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update, Delete multiple details at one time")
	public List<StockAdjust> updateStockAdjust(@PathVariable("id") String id, @RequestBody List<StockAdjust> list) {
		this.cudMultipleData(StockAdjust.class, list);
		return this.findStockAdjust(id);
	}
	
	@RequestMapping(value = "/start", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Start StockTaking (재고 실사 시작)")
	public Map<String, Object> startStockTaking(@RequestBody List<String> rackCdList) {
		this.stockService.startStocktaking(Domain.currentDomainId(), LogisConstants.EQUIP_TYPE_RACK, rackCdList);
		return ValueUtil.newMap("result", SysConstants.OK_STRING);
	}
	
	@RequestMapping(value = "/{id}/adjust/{ind_cd}/{from_qty}/{to_qty}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Stock Adjustment (재고 조정)")
	public Map<String, Object> adjustStock(@PathVariable("id") String id, @PathVariable("ind_cd") String indCd, @PathVariable("from_qty") Integer fromQty, @PathVariable("to_qty") Integer toQty) {
		Stock stock = this.stockService.adjustStock(Domain.currentDomainId(), id, indCd, fromQty, toQty);
		return ValueUtil.newMap("result,stock", SysConstants.OK_STRING, stock);
	}

	@RequestMapping(value = "/finish", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Finish StockTaking By Multiple (재고 실사 완료)")
	public Map<String, Object> finishStockTakingList(@RequestBody List<String> stockTakingIdList) {
		Long domainId = Domain.currentDomainId();
		
		for(String stockTakingId : stockTakingIdList) {
			this.stockService.finishStocktaking(domainId, stockTakingId);
		}
		
		return ValueUtil.newMap("result", SysConstants.OK_STRING);
	}
	
	@RequestMapping(value = "/{id}/finish", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Finish StockTaking (재고 실사 완료)")
	public Map<String, Object> finishStockTaking(@PathVariable("id") String id) {
		this.stockService.finishStocktaking(Domain.currentDomainId(), id);
		return ValueUtil.newMap("result", SysConstants.OK_STRING);
	}

}