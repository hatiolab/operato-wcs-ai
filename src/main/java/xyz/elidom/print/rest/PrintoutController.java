package xyz.elidom.print.rest;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.print.PrintConstants;
import xyz.elidom.print.entity.Printout;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.pdf.PdfPrintingService;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.util.ValueUtil;

//@RestController
//@Transactional
//@ResponseStatus(HttpStatus.OK)
//@RequestMapping("/rest/printouts")
//@ServiceDesc(description = "Printout Service API")
public class PrintoutController extends AbstractRestService {
    /**
     * 프린팅 서비스
     */
    @Autowired
    private PdfPrintingService pdfService;
    /**
     * 이벤트 퍼블리셔
     */
    @Autowired
    protected EventPublisher eventPublisher;
    
	@Override
	protected Class<?> entityClass() {
		return Printout.class;
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
	public Printout findOne(@PathVariable("id") String id) {
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
	public Printout create(@RequestBody Printout input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Printout update(@PathVariable("id") String id, @RequestBody Printout input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Printout> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value = "/print_pdf/by_template/{template_name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Printing Report PDF By Print Template Name")
	public Map<String, Object> printReportTemplate(
			@PathVariable("template_name") String templateName,
			@RequestBody Map<String, Object> params,
			@RequestParam(name = "printer_id", required = false) String printerId) {
		
        // 프린트 이벤트 생성 후 퍼블리쉬
        PrintEvent printEvent = new PrintEvent(Domain.currentDomainId(), "PRINT", PrintConstants.PRINTER_TYPE_NORMAL, printerId, null, params, true);
        this.eventPublisher.publishEvent(printEvent);
        return ValueUtil.newMap("success,result", printEvent.isExecuted(), AnyConstants.OK_STRING);
	}
	
    @RequestMapping(value = "/show_pdf/by_template_name/{template_name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Show PDF by Print Template")
    public void showPdfByPrintTemplateName(
            HttpServletRequest req, 
            HttpServletResponse res,
            @PathVariable("template_name") String templateName,
            @RequestBody Map<String, Object> params) {

        // PDF 다운로드
        this.pdfService.downloadPdfReport(res, Domain.currentDomainId(), templateName, params);
    }
    
	@RequestMapping(value = "/show_pdf/by_template/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Show PDF by Print Template")
	public void showPdfByPrintTemplate(
			HttpServletRequest req, 
			HttpServletResponse res,
			@PathVariable("id") String id,
			@RequestBody Map<String, Object> params) {
		
		// 1. ID로 객체 찾기
		Printout report = this.findOne(id);
		
		// 2. 리포트를 못 찾거나 템플릿 명이 없다면 에러
        if(report == null || ValueUtil.isEmpty(report.getTemplateCd())) {
            throw new ElidomValidationException(MessageUtil.getMessage("REPORT_TEMPLATE_NOT_FOUND"));
        }
        
		// 3. 리포트 템플릿으로 부터 리포트 다운로드
		this.pdfService.downloadPdfReport(res, report.getDomainId(), report.getTemplateCd(), params);
	}
}