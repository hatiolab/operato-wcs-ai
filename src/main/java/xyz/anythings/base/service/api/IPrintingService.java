package xyz.anythings.base.service.api;

import xyz.anythings.base.entity.BoxPack;

/**
 * 라벨 프린터, 일반 프린터 출력 서비스 API
 * 
 * @author shortstop
 */
public interface IPrintingService {

	/**
	 * 박스 라벨 인쇄
	 * 
	 * @param box
	 */
	public void printBarcodeLabel(BoxPack box);
	
	/**
	 * 박스 라벨 인쇄
	 * 
	 * @param domainId
	 * @param boxPackId
	 * @param printerIdOrName
	 */
	public void printBarcodeLabel(Long domainId, String boxPackId, String printerIdOrName);
	
	/**
	 * 박스 라벨 인쇄
	 * 
	 * @param box
	 * @param printerIdOrName
	 */
	public void printBarcodeLabel(BoxPack box, String printerIdOrName);
	
	/**
	 * 인쇄 명령으로 라벨 인쇄
	 * 
	 * @param domainid
	 * @param command
	 * @param printCount
	 * @param printerIdOrName
	 */
	public void printBarcodeLabel(Long domainId, String command, int printCount, String printerIdOrName);
	
	/**
	 * 박스 라벨 재인쇄
	 * 
	 * @param batchId
	 * @param boxId
	 * @param printerIdOrName
	 * @return
	 */
	public int reprintBarcodeLabel(String batchId, String boxId, String printerIdOrName);
	
	/**
	 * 송장 라벨 재인쇄
	 * 
	 * @param batchId
	 * @param invoiceNo
	 * @param printerIdOrName
	 * @return
	 */
	public int reprintInvoiceLabel(String batchId, String invoiceNo, String printerIdOrName);
	
	/**
	 * 리포트 인쇄
	 * 
	 * @param domainId
	 * @param boxPackId
	 * @param printerIdOrName
	 */
	public void printReport(Long domainId, String boxPackId, String printerIdOrName);
	
	/**
	 * 리포트 인쇄
	 * 
	 * @param box
	 * @param printerIdOrName
	 */
	public void printReport(BoxPack box, String printerIdOrName);
	
	/**
	 * 리포트 인쇄
	 * 
	 * @param box
	 */
	public void printReport(BoxPack box);
	
}
