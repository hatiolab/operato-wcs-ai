package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 재고 이력
 * 
 * @author shortstop
 */
@Table(name = "stock_hists", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_stock_hists_0", columnList = "domain_id,cell_cd"),
	@Index(name = "ix_stock_hists_1", columnList = "domain_id,com_cd,sku_cd"),
	@Index(name = "ix_stock_hists_2", columnList = "domain_id,cell_cd,tran_cd"),
	@Index(name = "ix_stock_hists_3", columnList = "domain_id,created_at")
})
public class StockHist extends xyz.elidom.orm.entity.basic.DomainCreateStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 194333069817661062L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "cell_cd", nullable = false, length = 30)
	private String cellCd;
	
	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "sku_cd", length = 30)
	private String skuCd;

	@Column (name = "prev_stock_qty", length = 12)
	private Integer prevStockQty;

	@Column (name = "in_qty", length = 12)
	private Integer inQty;

	@Column (name = "out_qty", length = 12)
	private Integer outQty;

	@Column (name = "stock_qty", length = 12)
	private Integer stockQty;

	@Column (name = "tran_cd", length = 30)
	private String tranCd;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCellCd() {
		return cellCd;
	}

	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
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

	public Integer getInQty() {
		return inQty;
	}

	public void setInQty(Integer inQty) {
		this.inQty = inQty;
	}

	public Integer getOutQty() {
		return outQty;
	}

	public void setOutQty(Integer outQty) {
		this.outQty = outQty;
	}

	public Integer getStockQty() {
		return stockQty;
	}

	public void setStockQty(Integer stockQty) {
		this.stockQty = stockQty;
	}

	public String getTranCd() {
		return tranCd;
	}

	public void setTranCd(String tranCd) {
		this.tranCd = tranCd;
	}	
}
