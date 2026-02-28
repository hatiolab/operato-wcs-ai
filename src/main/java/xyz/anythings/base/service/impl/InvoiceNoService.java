package xyz.anythings.base.service.impl;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.service.api.IInvoiceNoGenerator;
import xyz.anythings.base.service.api.IInvoiceNoService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * IInvoiceNoService 기본 구현
 * 
 * @author shortstop
 */
@Component
public class InvoiceNoService extends AbstractLogisService implements IInvoiceNoService {

	/**
	 * BeanFactory
	 */
	@Autowired
	protected BeanFactory beanFactory;
	
	@Override
	public int generateInvoiceNo(Long domainId, String stageCd, String comCd, String customerCd, Object... params) {
		IInvoiceNoGenerator generator = this.getInvoceNoGenerator(domainId, stageCd);
		return generator.generateInvoiceNumbers(domainId, comCd, customerCd, ValueUtil.toString(params[0]), ValueUtil.toString(params[1]));
	}

	@Override
	public String nextInvoiceId(Long domainId, String stageCd, String comCd, String customerCd) {
		IInvoiceNoGenerator generator = this.getInvoceNoGenerator(domainId, stageCd);
		return generator.nextInvoiceId(domainId, stageCd, comCd, customerCd);
	}

	private IInvoiceNoGenerator getInvoceNoGenerator(Long domainId, String stageCd) {
		String courierVendor = SettingUtil.getValue(domainId, stageCd + ".courier.vendor", "cj");
		String generatorType = courierVendor.toLowerCase() + "InvoiceNoGenerator";
		return (IInvoiceNoGenerator)this.beanFactory.getBean(generatorType);
	}

}
