package xyz.elidom.mw.print.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * PDF Print Message Body
 * 
 * @author shortstop
 */
@JsonTypeName(Action.Values.PdfPrint)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class PdfPrintBody implements IPrintBody {
	/**
	 * PDF 인쇄 액션
	 */
	private String action = Action.Values.PdfPrint;
	/**
	 * PDF doc  
	 */
	private byte[] pdf;
	/**
	 * 프린터 명  
	 */
	private String printer;
	/**
	 * 프린터 ID
	 */
	private String printerId;
	/**
	 * 출력 DPI 
	 */
	private int dpi;
	/**
	 * 출력 페이지 count 
	 */
	private int count;
	
	public PdfPrintBody() {
	}
	
	public PdfPrintBody(String printer, int dpi, byte[] pdf, int count) {
		this.printer = printer;
		this.dpi = dpi;
		this.pdf = pdf;
		this.count = count;
	}

	@Override
	public void setAction(String action) {
	}

	@Override
	public String getAction() {
		return this.action;
	}

	public byte[] getPdf() {
		return pdf;
	}

	public void setPdf(byte[] pdf) {
		this.pdf = pdf;
	}

	public String getPrinter() {
		return printer;
	}

	public void setPrinter(String printer) {
		this.printer = printer;
	}

	public String getPrinterId() {
		return printerId;
	}

	public void setPrinterId(String printerId) {
		this.printerId = printerId;
	}

	public int getDpi() {
		return dpi;
	}

	public void setDpi(int dpi) {
		this.dpi = dpi;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}