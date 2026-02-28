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

import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.event.master.SkuReceiptEvent;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.api.ISkuSearchService;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
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
@RequestMapping("/rest/sku")
@ServiceDesc(description = "SKU Service API")
public class SKUController extends AbstractRestService {
	
	/**
	 * 상품 수신 카운트 조회 커스텀 서비스
	 */
	private static final String DIY_SKU_READY_TO_RECEIVE = "diy-ready-to-receive-sku";
	/**
	 * 상품 수신 커스텀 서비스
	 */
	private static final String DIY_SKU_START_TO_RECEIVE = "diy-start-to-receive-sku";
	/**
	 * 상품 조회 서비스
	 */
	@Autowired
	private ISkuSearchService skuSearchService;
	/**
	 * 이벤트 퍼블리셔
	 */
	@Autowired
	protected EventPublisher eventPublisher;
	/**
	 * 커스텀 서비스 실행기
	 */
	@Autowired
	protected ICustomService customService;

	@Override
	protected Class<?> entityClass() {
		return SKU.class;
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
	public SKU findOne(@PathVariable("id") String id) {
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
	public SKU create(@RequestBody SKU input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public SKU update(@PathVariable("id") String id, @RequestBody SKU input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}
	
	@RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<SKU> checkImport(@RequestBody List<SKU> list) {
		Long domainId = Domain.currentDomainId();
		
		for (SKU item : list) {
			item.setDomainId(domainId);
			SKU sku = this.queryManager.selectByCondition(SKU.class, ValueUtil.newMap("domainId,comCd,skuCd", domainId, item.getComCd(), item.getSkuCd()));
			item.setCudFlag_(sku == null ? SysConstants.CUD_FLAG_CREATE : SysConstants.CUD_FLAG_UPDATE);
			
			if(sku != null) {
				item.setId(sku.getId());
			}
		}
		
		return list;
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<SKU> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value = "/receive/ready/{receive_type}/{com_cd}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Ready to Receive From Parent System")
	public Object readyToReceive(@PathVariable("receive_type") String receiveType, @PathVariable("com_cd") String comCd) {
		
		SkuReceiptEvent event = new SkuReceiptEvent(Domain.currentDomainId(), receiveType, comCd, SkuReceiptEvent.EVENT_STEP_BEFORE);
		event = (SkuReceiptEvent)this.eventPublisher.publishEvent(event);
		
		Object retVal = this.customService.doCustomService(Domain.currentDomainId(), DIY_SKU_READY_TO_RECEIVE, ValueUtil.newMap("receiveType,comCd", receiveType, comCd));
		if(retVal == null) {
			return ValueUtil.newMap("com_cd,receive_type,plan_count", comCd, receiveType, event.getPlanCount());
		} else {
			return retVal;
		}
	}
	
	@RequestMapping(value = "/receive/start/{receive_type}/{com_cd}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Receive (Interface) From Parent System")
	public Object startToReceive(@PathVariable("receive_type") String receiveType, @PathVariable("com_cd") String comCd) {
		
		SkuReceiptEvent event = new SkuReceiptEvent(Domain.currentDomainId(), receiveType, comCd, SkuReceiptEvent.EVENT_STEP_AFTER);
		event = (SkuReceiptEvent)this.eventPublisher.publishEvent(event);
		
		Object retVal = this.customService.doCustomService(Domain.currentDomainId(), DIY_SKU_START_TO_RECEIVE, ValueUtil.newMap("receiveType,comCd", receiveType, comCd));
		if(retVal == null) {
			return ValueUtil.newMap("com_cd,receive_type,plan_count,received_count,error_count", comCd, receiveType, event.getPlanCount(), event.getReceivedCount(), event.getErrorCount());
		} else {
			return retVal;
		}
	}
	
	@RequestMapping(value = "/find/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find by Sku Cd")
	public SKU findBySkuCd(@PathVariable("com_cd") String comCd, @PathVariable("sku_cd") String skuCd) {
		return AnyEntityUtil.findEntityBy(Domain.currentDomainId(), true, SKU.class, null, "comCd,skuCd", comCd, skuCd);
	}
	
	@RequestMapping(value = "/search/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search List by SkuCd")
	public List<SKU> searchBySkuCd(@PathVariable("sku_cd") String skuCd) {
		
		String sql = "SELECT * FROM SKU WHERE SKU_CD = :skuCd OR SKU_BARCD = :skuCd OR SKU_BARCD2 = :skuCd OR SKU_BARCD3 = :skuCd";
		Map<String, Object> condition = ValueUtil.newMap("skuCd", skuCd);
		return this.queryManager.selectListBySql(sql, condition, SKU.class, 0, 0);
	}
	
	@RequestMapping(value = "/search/{equip_type}/{equip_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search by SKU")
	public List<SKU> searchBySkuCd(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("sku_cd") String skuCd) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		return this.skuSearchService.searchList(equipBatchSet.getBatch(), skuCd);
	}

}