package xyz.elidom.print.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.MwQueueListEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.mw.rabbitmq.event.model.IQueueNameModel;
import xyz.elidom.mw.rabbitmq.event.model.MwQueueNameModel;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.print.PrintConstants;
import xyz.elidom.print.entity.Printer;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.print.ElidomPrintingConfig;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/printers")
@ServiceDesc(description = "Printer Service API")
public class PrinterController extends AbstractRestService {
	/**
	 * Event Publisher
	 */
	@Autowired
	protected EventPublisher eventPublisher;
	
	@Autowired
	protected ElidomPrintingConfig printCfg;
	
	@Override
	protected Class<?> entityClass() {
		return Printer.class;
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
	public Printer findOne(@PathVariable("id") String id) {
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
	public Printer create(@RequestBody Printer input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Printer update(@PathVariable("id") String id, @RequestBody Printer input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Printer> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	/**
	 * 라벨 인쇄 요청 처리
	 * 
	 * @param printEvent { printType : barcode (바코드) / normal (일반), printerId : 프린터 ID, printTemplate : 인쇄할 커스텀 템플릿 명, templateParams : 인쇄 템플릿을 실행시킬 파라미터, printCount : 인쇄 매수 }
	 * @return
	 */
	@RequestMapping(value = "/print_label", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Print Label")
	public BaseResponse printLabel(@RequestBody PrintEvent printEvent) {
	    printEvent.setPrintType(PrintConstants.PRINTER_TYPE_BARCODE);
		this.eventPublisher.publishEvent(printEvent);
		return new BaseResponse(true);
	}
	
	@Order(Ordered.LOWEST_PRECEDENCE)
	@EventListener(condition = "#root.args[0].isExecuted() == false")
	public void getRabbitMqVhostQueueList(MwQueueListEvent event) {
		// 도메인 리스트
		List<Domain> domainList = this.queryManager.selectList(Domain.class, ValueUtil.newMap(SysConstants.EMPTY_STRING));
		List<IQueueNameModel> result = new ArrayList<IQueueNameModel>();
		
		for(Domain domain : domainList) {
			Long domainId = domain.getId();
			String mwSite = domain.getMwSiteCd();
			
			if(mwSite == null || !this.printCfg.isServerPrintCommType(domainId)) {
				continue;
			}
			
			Map<String, Object> params = ValueUtil.newMap("domainId", domainId);
			List<Printer> printerList = this.queryManager.selectList(Printer.class, params);
			
			for(Printer printer : printerList) {
				IQueueNameModel qm = new MwQueueNameModel(domainId, mwSite, printer.getPrinterNm(), "c");
				result.add(qm);
			}
		}
		
		event.setQueueNames(result);
		event.setExecuted(true);
	}
}