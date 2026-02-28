package xyz.elidom.sys.system.pdf;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dev.entity.DiyTemplate;
import xyz.elidom.exception.ElidomException;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.engine.IScriptEngine;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * PDF 인쇄 서비스
 * 
 * @author shortstop
 */
@Component
public class PdfPrintingService {

	/**
	 * 리포트 정보를 로딩하여 PDF를 byte[]로 변환
	 * 
	 * @param domainId
	 * @param templateName
	 * @param params
	 * @return
	 */
	public byte[] loadPdfReportToBytes(Long domainId, String templateName, Map<String, Object> params) {
		JasperPrint jasperPrint = this.loadJasperReport(domainId, templateName, params);
		
		try {
			return JasperExportManager.exportReportToPdf(jasperPrint);
		} catch(ElidomException ee) {
			throw ee;
		} catch(JRException jre) {
			throw ThrowUtil.newFailToProcessTemplate(templateName + " Report", jre);
		}
	}
	
	/**
	 * 리포트 정보를 로딩하여 PDF를 XML로 변환
	 * 
	 * @param domainId
	 * @param templateName
	 * @param params
	 * @return
	 */
	public String loadPdfReportToXml(Long domainId, String templateName, Map<String, Object> params) {
		JasperPrint jasperPrint = this.loadJasperReport(domainId, templateName, params);
		
		try {
			return JasperExportManager.exportReportToXml(jasperPrint);
		} catch(ElidomException ee) {
			throw ee;
		} catch(JRException jre) {
			throw ThrowUtil.newFailToProcessTemplate(templateName + " Report", jre);
		}
	}
	
	/**
	 * 리포트 정보를 로딩하여 JasperPrint로 리턴
	 * 
	 * @param domainId
	 * @param templateName
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public JasperPrint loadJasperReport(Long domainId, String templateName, Map<String, Object> params) {
		// 1. 프린트 템플릿으로 부터 리포트 템플릿, 서비스 로직 조회
		DiyTemplate template = AnyEntityUtil.findEntityByCode(domainId, true, DiyTemplate.class, "name", templateName);
		String reportContent = template.getTemplate();
		String reportLogic = template.getLogic();

		// 2. Report Template 체크
		if(ValueUtil.isEmpty(reportContent)) {
			throw new ElidomValidationException(MessageUtil.getMessage("REPORT_TEMPLATE_CONTENT_NOT_EXIST"));
		}
		
		// 3. Report Logic 체크
		if(ValueUtil.isEmpty(reportLogic)) {
			throw new ElidomValidationException(MessageUtil.getMessage("REPORT_TEMPLATE_SERVICE_NOT_REGISTERED"));
		}
		
		// 4. 리포트에 데이터 매핑 및 다운로드
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		if(!params.containsKey("domain_id")) {
		    params.put("domain_id", domainId);
		}
		Map<String, Object> result = (Map<String, Object>)scriptEngine.runScript("groovy", reportLogic, params);
		JasperPrint jasperPrint = null;
		
		try {
			// 5. JasperReport 정보를 로딩
			InputStream is = new ByteArrayInputStream(reportContent.getBytes());
			JasperDesign jasperDesign = JRXmlLoader.load(is);
			JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
			
			// 6. JasperReport에 데이터 바인딩하기 위한 리포트 데이터소스를 빌드
			String jsonData = FormatUtil.toUnderScoreJsonString(result);
			InputStream stream = new ByteArrayInputStream(jsonData.getBytes(SysConstants.CHAR_SET_UTF8));
			JsonDataSource jds = new JsonDataSource(stream);
			
			// 7. 리포트를 JasperPrint로 변환 
			jasperPrint = JasperFillManager.fillReport(jasperReport, null, jds);
			
		} catch(ElidomException ee) {
			throw ee;

		} catch(JRException jre) {
			throw ThrowUtil.newFailToProcessTemplate(templateName + " Report", jre);

		} catch(Exception e) {
			throw ThrowUtil.newFailToProcessTemplate(templateName + " Report", e);
		}
		
		// 8. 결과 리턴
		return jasperPrint;
	}
	
	/**
     * 리포트 다운로드 
     * 
     * @param res
     * @param domainId
     * @param templateName
     * @param parameters
     */
    public void downloadPdfReport(HttpServletResponse res, Long domainId, String templateName, Map<String, Object> parameters) {
        // 1. response에 헤더 설정 
        res.setCharacterEncoding(SysConstants.CHAR_SET_UTF8);
        res.setContentType("text/plain;charset=" + SysConstants.CHAR_SET_UTF8);
        res.addHeader("Content-Type", "application/pdf");
        res.setHeader("Content-Disposition", "attachment; filename=" + templateName + ".pdf");
        res.addHeader("Content-Transfer-Encoding", "binary;");
        res.addHeader("Pragma", "no-cache;");
        res.addHeader("Expires", "-1;");
        
        try {
            // 2. JasperReport 정보를 로딩
            JasperPrint jasperPrint = this.loadJasperReport(domainId, templateName, parameters);

            // 3. 다운로드 처리
            if (jasperPrint != null) {
                JasperExportManager.exportReportToPdfStream(jasperPrint, res.getOutputStream());
            }
            
        } catch(ElidomException ee) {
            throw ee;

        } catch(JRException jre) {
            throw ThrowUtil.newFailToProcessTemplate(templateName + " Report", jre);

        } catch(Exception e) {
            throw ThrowUtil.newFailToProcessTemplate(templateName + " Report", e);
        }
    }
}
