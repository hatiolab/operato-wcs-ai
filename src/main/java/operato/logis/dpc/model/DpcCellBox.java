package operato.logis.dpc.model;

/**
 * DPC 셀에 표시할 정보
 * 
 * @author shortstop
 */
public class DpcCellBox {
	/**
	 * 카트 셀 코드
	 */
	private String cellCd;
	/**
	 * 카트 셀 분류 코드
	 */
	private String classCd;
	/**
	 * 카트 셀에 매핑된 박스 ID
	 */
	private String boxId;
	/**
	 * 상품 분류 수량
	 */
	private Integer pickQty;
	/**
	 * 카트 셀 활성화 여부
	 */
	private boolean active;
	
	/**
	 * 주문 완성 여부
	 */
	private boolean finished;
	
	public String getCellCd() {
		return cellCd;
	}
	
	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}
	
	public String getClassCd() {
		return classCd;
	}
	
	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}
	
	public String getBoxId() {
		return boxId;
	}
	
	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}
	
	public Integer getPickQty() {
		return pickQty;
	}
	
	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

}
