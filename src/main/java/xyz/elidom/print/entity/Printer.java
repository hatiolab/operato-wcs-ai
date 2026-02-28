package xyz.elidom.print.entity;

import java.util.List;

import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.MwQueueManageEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.mw.print.util.MwPrintUtil;
import xyz.elidom.mw.rabbitmq.event.model.IQueueNameModel;
import xyz.elidom.mw.rabbitmq.event.model.MwQueueNameModel;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.print.PrintConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.print.ElidomPrintingConfig;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 바코드 프린터
 * 
 * @author shortstop
 */
@Table(name = "printers", idStrategy = GenerationRule.UUID, uniqueFields="domainId,printerCd", indexes = {
	@Index(name = "ix_printers_0", columnList = "domain_id,printer_cd", unique = true),
	@Index(name = "ix_printers_1", columnList = "domain_id,stage_cd")
})
public class Printer extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 315339403280044039L;
	
	/**
	 * 프린터 유형 - 일반 프린터 (NORMAL)
	 */
	public static final String TYPE_NORMAL = "NORMAL";
	/**
	 * 프린터 유형 - 바코드 프린터 (BARCODE)
	 */
	public static final String TYPE_BARCODE = "BARCODE";

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;
	
	@Column (name = "printer_cd", nullable = false, length = 30)
	private String printerCd;

	@Column (name = "printer_nm", nullable = false, length = 100)
	private String printerNm;

	@Column (name = "printer_type", length = 20)
	private String printerType;

	@Column (name = "printer_ip", nullable = false, length = 16)
	private String printerIp;

	@Column (name = "printer_port", length = 12)
	private Integer printerPort;

	@Column (name = "printer_driver", length = 40)
	private String printerDriver;

	@Column (name = "printer_agent_url")
	private String printerAgentUrl;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "remark", length = 1000)
	private String remark;

	@Column (name = "default_flag", length = 1)
	private Boolean defaultFlag;
  
	@Column (name = "dpi", length = 4)
	private Integer dpi;
	
	@Ignore
	private String beforePrinterCd;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getPrinterCd() {
		return printerCd;
	}

	public void setPrinterCd(String printerCd) {
		this.printerCd = printerCd;
	}

	public String getPrinterNm() {
		return printerNm;
	}

	public void setPrinterNm(String printerNm) {
		this.printerNm = printerNm;
	}

	public String getPrinterType() {
		return printerType;
	}

	public void setPrinterType(String printerType) {
		this.printerType = printerType;
	}

	public String getPrinterIp() {
		return printerIp;
	}

	public void setPrinterIp(String printerIp) {
		this.printerIp = printerIp;
	}

	public Integer getPrinterPort() {
		return printerPort;
	}

	public void setPrinterPort(Integer printerPort) {
		this.printerPort = printerPort;
	}

	public String getPrinterDriver() {
		return printerDriver;
	}

	public void setPrinterDriver(String printerDriver) {
		this.printerDriver = printerDriver;
	}

	public String getPrinterAgentUrl() {
		return printerAgentUrl;
	}

	public void setPrinterAgentUrl(String printerAgentUrl) {
		this.printerAgentUrl = printerAgentUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Integer getDpi() {
		return dpi;
	}

	public void setDpi(Integer dpi) {
		this.dpi = dpi;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Boolean getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	@Override
	public void afterCreate() {
		super.afterCreate();
		
		if(!BeanUtil.get(ElidomPrintingConfig.class).isServerPrintCommType(this.domainId)) {
			return;
		}
		
		this.updateMwQueue("c");
	}
	
	@Override
	public void beforeUpdate() {
		super.beforeUpdate();
		
		if(!BeanUtil.get(ElidomPrintingConfig.class).isServerPrintCommType(this.domainId)) {
			return;
		}
		
		this.beforePrinterCd = BeanUtil.get(IQueryManager.class).select(Printer.class, this.id).getPrinterNm();
	}
	
	@Override
	public void afterUpdate() {
		super.afterUpdate();
		
		if(!BeanUtil.get(ElidomPrintingConfig.class).isServerPrintCommType(this.domainId)) {
			return;
		}
		
		if(ValueUtil.isNotEqual(this.beforePrinterCd, this.printerCd)) {
			this.updateMwQueue("u");
			this.beforePrinterCd = null;
		}
	}
	
	@Override
	public void afterDelete() {
		super.afterDelete();
		
		if(!BeanUtil.get(ElidomPrintingConfig.class).isServerPrintCommType(this.domainId)) {
			return;
		}
		
		this.updateMwQueue("d");
	}
	
	/**
	 * M/W 큐 업데이트
	 * 
	 * @param cudFlag_
	 */
	private void updateMwQueue(String cudFlag_) {
		Domain domain = Domain.currentDomain();
		String befQueueNm = (this.beforePrinterCd == null) ? null : MwPrintUtil.getPrinterMwQueueName(domain, this.printerType, this.beforePrinterCd);
		String curQueueNm = MwPrintUtil.getPrinterMwQueueName(domain, this.printerType, this.printerCd);
		List<IQueueNameModel> queueModels = ValueUtil.toList(new MwQueueNameModel(this.domainId, domain.getMwSiteCd(), befQueueNm, curQueueNm, cudFlag_));
		MwQueueManageEvent event = new MwQueueManageEvent(this.domainId, queueModels);
		BeanUtil.get(EventPublisher.class).publishEvent(event);
	}
	
	/**
	 * printerId로 프린터를 조회
	 * 
	 * @param domainId
	 * @param printerId
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static Printer find(Long domainId, String printerId, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("id", printerId);
		Printer printer = BeanUtil.get(IQueryManager.class).selectByCondition(Printer.class, condition);
		
		if(exceptionWhenEmpty && printer == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.Printer", printerId);
		}
		
		return printer;
	}

	/**
	 * printerId 혹은 printerName으로 프린터 조회
	 * 
	 * @param domainId
	 * @param printerIdOrName
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static Printer findByIdOrName(Long domainId, String printerIdOrName, boolean exceptionWhenEmpty) {
		Printer printer = Printer.find(domainId, printerIdOrName, false);
		
		if(printer == null) {
			printer = Printer.findByPrinterCd(domainId, printerIdOrName, false);
			
			if(printer == null) {
				printer = Printer.findByPrinterNm(domainId, printerIdOrName, exceptionWhenEmpty);
			}
		}
		
		return printer;
	}
	
	/**
	 * 프린터 코드로 프린터 조회
	 * 
	 * @param domainId
	 * @param printerCd
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static Printer findByPrinterCd(Long domainId, String printerCd, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("printerCd", printerCd);
		Printer printer = BeanUtil.get(IQueryManager.class).selectByCondition(Printer.class, condition);
		
		if(exceptionWhenEmpty && printer == null) {
			throw new ElidomValidationException(MessageUtil.getMessage("NOT_EXIST_PRINTER","프린터 [{0}]가 존재하지 않습니다.",ValueUtil.newStringList(printerCd)));
		}
		
		return printer;
	}
	
	/**
	 * 프린터 명으로 프린터 조회
	 * 
	 * @param domainId
	 * @param printerName
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static Printer findByPrinterNm(Long domainId, String printerName, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("printerNm", printerName);
		Printer printer = BeanUtil.get(IQueryManager.class).selectByCondition(Printer.class, condition);
		
		if(exceptionWhenEmpty && printer == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.Printer", printerName);
		}
		
		return printer;
	}
	
	/**
	 * 도메인 내 기본 바코드 프린터 조회
	 * 
	 * @param domainId
	 * @return
	 */
	public static Printer findDefaultBarcodePrinter(Long domainId) {
		return findDefaultPrinter(domainId, PrintConstants.PRINTER_TYPE_BARCODE);
	}
	
	/**
	 * 도메인 내 기본 일반 프린터 조회
	 * 
	 * @param domainId
	 * @return
	 */
	public static Printer findDefaultNormalPrinter(Long domainId) {
		return findDefaultPrinter(domainId, PrintConstants.PRINTER_TYPE_NORMAL);
	}

	/**
	 * 도메인 내 프린터 조회
	 * 
	 * @param domainId
	 * @param printerType
	 * @return
	 */
	public static Printer findDefaultPrinter(Long domainId, String printerType) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("defaultFlag", true);
		condition.addFilter("printerType", printerType);
		condition.setPageIndex(1);
		condition.setPageSize(1);
		
		List<Printer> printerList = BeanUtil.get(IQueryManager.class).selectList(Printer.class, condition);		
		return ValueUtil.isNotEmpty(printerList) ? printerList.get(0) : null;
	}
	
	/**
	 * 도메인 내 프린터 조회
	 * 
	 * @param domainId
	 * @param printerType
	 * @param exceptionWhenNotFound
	 * @return
	 */
	public static Printer findDefaultPrinter(Long domainId, String printerType, boolean exceptionWhenNotFound) {
		Printer printer = Printer.findDefaultBarcodePrinter(domainId);
		if(printer == null) {
			throw new ElidomRuntimeException("Default Barcode Printer is not exist!");
		}
		
		return printer;
	}
}
