package operato.logis.dpc.model;

import java.util.List;

import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.model.BatchProgressRate;

/**
 * DPC 배치 서머리 리턴 모델
 * 
 * @author shortstop
 */
public class DpcBatchSummary {
	/**
	 * 작업 배치 ID
	 */
	private String batchId;
	/**
	 * 카트 작업 번호
	 */
	private String cartJobId;
	/**
	 * 배치 전체 진행율
	 */
	private BatchProgressRate totalRate;
	/**
	 * 보관 존 별 투입 리스트
	 */
	private List<DpcZoneInput> inputList;
	/**
	 * 카트별 셀 리스트
	 */
	private List<DpcCellBox> cellList;
	/**
	 * 상품 정보
	 */
	private SKU sku;
	
	/**
	 * 생성자
	 */
	public DpcBatchSummary() {
	}

	/**
	 * 생성자
	 * 
	 * @param batchId
	 * @param cartJobId
	 * @param totalRate
	 * @param inputList
	 * @param cellList
	 * @param sku
	 */
	public DpcBatchSummary(String batchId, String cartJobId, BatchProgressRate totalRate, List<DpcZoneInput> inputList, List<DpcCellBox> cellList, SKU sku) {
		this.batchId = batchId;
		this.cartJobId = cartJobId;
		this.totalRate = totalRate;
		this.inputList = inputList;
		this.cellList = cellList;
		this.sku = sku;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getCartJobId() {
		return cartJobId;
	}

	public void setCartJobId(String cartJobId) {
		this.cartJobId = cartJobId;
	}
	
	public BatchProgressRate getTotalRate() {
		return totalRate;
	}

	public void setTotalRate(BatchProgressRate totalRate) {
		this.totalRate = totalRate;
	}

	public List<DpcZoneInput> getInputList() {
		return inputList;
	}

	public void setInputList(List<DpcZoneInput> inputList) {
		this.inputList = inputList;
	}

	public List<DpcCellBox> getCellList() {
		return cellList;
	}

	public void setCellList(List<DpcCellBox> cellList) {
		this.cellList = cellList;
	}

	public SKU getSku() {
		return sku;
	}

	public void setSku(SKU sku) {
		this.sku = sku;
	}

}
