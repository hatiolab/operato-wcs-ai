package xyz.elidom.mw.print.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * ZPL Print Message Body
 * 
 * @author shortstop
 */
@JsonTypeName(Action.Values.ZplPrint)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ZplPrintBody implements IPrintBody {
	/**
	 * 액션
	 */
	private String action = Action.Values.ZplPrint;
	/**
	 * ZPL COMMAND
	 */
	private String zpl;
	/**
	 * 프린터 명
	 */
	private String printer;
	/**
	 * 프린터 ID
	 */
	private String printerId;
	/**
	 * 출력 페이지 count
	 */
	private int count;
	
	/**
	 * 생성자
	 */
	public ZplPrintBody() {
	}
	
	/**
	 * 생성자
	 * 
	 * @param printerName
	 * @param zpl
	 * @param count
	 */
	public ZplPrintBody(String printerName, String zpl, int count) {
		this.printer = printerName;
		this.zpl = zpl;
		this.count = count;
	}
	
	@Override
	public void setAction(String action) {
	}

	@Override
	public String getAction() {
		return this.action;
	}

	public String getZpl() {
		return zpl;
	}

	public void setZpl(String zpl) {
		this.zpl = zpl;
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

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}