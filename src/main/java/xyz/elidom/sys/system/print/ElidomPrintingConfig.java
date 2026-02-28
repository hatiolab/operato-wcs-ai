package xyz.elidom.sys.system.print;

import org.springframework.stereotype.Component;

import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 공통 프린트 서비스
 * 
 * @author shortstop
 */
@Component
public class ElidomPrintingConfig {    
    /**
     * 프린트 통신 유형이 M/W 인지 여부
     * 
     * @param domainId
     * @return
     */
    public boolean isMwPrintCommType(Long domainId) {
    	return ValueUtil.isEqualIgnoreCase("mw", this.getPrintCommType(domainId));
    }
    
    /**
     * 프린트 통신 유형이 REST 인지 여부
     * 
     * @param domainId
     * @return
     */
    public boolean isRestPrintCommType(Long domainId) {
    	return ValueUtil.isEqualIgnoreCase("rest", this.getPrintCommType(domainId));
    }
    
    /**
     * 프린트 통신 유형이 Server 인지 여부
     * 
     * @param domainId
     * @return
     */
    public boolean isServerPrintCommType(Long domainId) {
    	return ValueUtil.isEqualIgnoreCase("server", this.getPrintCommType(domainId));
    }
    
    /**
     * 프린트 통신 유형 리턴
     * 
     * @param domainId
     * @return
     */
    public String getPrintCommType(Long domainId) {
    	return SettingUtil.getValue(domainId, "operato.printing.communication.type");
    }
    
	/**
	 * 디폴트 프린터 명 리턴
	 * 
	 * @param domainId
	 * @return
	 */
	public String getDefaultPrinterName() {
		return SettingUtil.getValue("operato.printing.default.printer.name");
	}
	
	/**
	 * 디폴트 캐릭터 셋 리턴 
	 * 
	 * @return
	 */
	public String getDefaultCharSet() {
		return SettingUtil.getValue("operato.printing.default.printer.charset", "UTF-8");
	}
}