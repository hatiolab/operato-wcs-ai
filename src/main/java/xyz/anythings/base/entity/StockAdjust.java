package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 재고 조사 상세 - 재고 조정
 * 
 * @author shortstop
 */
@Table(name = "stock_adjusts", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_stock_adjusts_0", columnList = "domain_id,stocktaking_id")
})
public class StockAdjust extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 165727410692846478L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "stocktaking_id", nullable = false, length = 40)
	private String stocktakingId;
	
	@Column (name = "cell_cd", nullable = false, length = 30)
	private String cellCd;
	
	@Column (name = "ind_cd", nullable = true, length = 30)
	private String indCd;
	
	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "sku_cd", nullable = false, length = 30)
	private String skuCd;

	@Column (name = "prev_stock_qty", nullable = false, length = 12)
	private Integer prevStockQty;

	@Column (name = "after_stock_qty", nullable = false, length = 12)
	private Integer afterStockQty;

	@Column (name = "adjust_qty", length = 12)
	private Integer adjustQty;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStocktakingId() {
		return stocktakingId;
	}

	public void setStocktakingId(String stocktakingId) {
		this.stocktakingId = stocktakingId;
	}

	public String getCellCd() {
		return cellCd;
	}

	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}

	public String getIndCd() {
		return indCd;
	}

	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public Integer getPrevStockQty() {
		return prevStockQty;
	}

	public void setPrevStockQty(Integer prevStockQty) {
		this.prevStockQty = prevStockQty;
	}

	public Integer getAfterStockQty() {
		return afterStockQty;
	}

	public void setAfterStockQty(Integer afterStockQty) {
		this.afterStockQty = afterStockQty;
	}

	public Integer getAdjustQty() {
		return adjustQty;
	}

	public void setAdjustQty(Integer adjustQty) {
		this.adjustQty = adjustQty;
	}

}
