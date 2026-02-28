package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "popular_ranks", idStrategy = GenerationRule.UUID, uniqueFields="domainId,skuRank", indexes = {
	@Index(name = "ix_popular_ranks_0", columnList = "domain_id,sku_rank", unique = true),
	@Index(name = "ix_popular_ranks_1", columnList = "com_cd,sku_cd")
})
public class PopularRank extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 708711792913006378L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "sku_rank", nullable = false, length = 12)
	private Integer skuRank;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "sku_cd", nullable = false, length = 50)
	private String skuCd;

	@Column (name = "sku_nm", nullable = false, length = 200)
	private String skuNm;

	@Column (name = "tot_ord_qty", nullable = false, length = 12)
	private Integer totOrdQty;

	@Column (name = "avg_ord_qty", nullable = false, length = 12)
	private Float avgOrdQty;

	@Column (name = "unit_ord_qty", length = 12)
	private Float unitOrdQty;

	@Column (name = "multi_flag", length = 1)
	private Boolean multiFlag;

	@Column (name = "multi_idx", length = 12)
	private Integer multiIdx;

	@Column (name = "total_job_days")
	private Integer totalJobDays;

	@Column (name = "last_job_date", length = 10)
	private String lastJobDate;

	@Column (name = "station_cd", length = 30)
	private String stationCd;

	@Column (name = "cell_cd", length = 30)
	private String cellCd;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getSkuRank() {
		return skuRank;
	}

	public void setSkuRank(Integer skuRank) {
		this.skuRank = skuRank;
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

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public Integer getTotOrdQty() {
		return totOrdQty;
	}

	public void setTotOrdQty(Integer totOrdQty) {
		this.totOrdQty = totOrdQty;
	}

	public Float getAvgOrdQty() {
		return avgOrdQty;
	}

	public void setAvgOrdQty(Float avgOrdQty) {
		this.avgOrdQty = avgOrdQty;
	}

	public Float getUnitOrdQty() {
		return unitOrdQty;
	}

	public void setUnitOrdQty(Float unitOrdQty) {
		this.unitOrdQty = unitOrdQty;
	}

	public Boolean getMultiFlag() {
		return multiFlag;
	}

	public void setMultiFlag(Boolean multiFlag) {
		this.multiFlag = multiFlag;
	}

	public Integer getMultiIdx() {
		return multiIdx;
	}

	public void setMultiIdx(Integer multiIdx) {
		this.multiIdx = multiIdx;
	}

	public Integer getTotalJobDays() {
		return totalJobDays;
	}

	public void setTotalJobDays(Integer totalJobDays) {
		this.totalJobDays = totalJobDays;
	}

	public String getLastJobDate() {
		return lastJobDate;
	}

	public void setLastJobDate(String lastJobDate) {
		this.lastJobDate = lastJobDate;
	}

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}

	public String getCellCd() {
		return cellCd;
	}

	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}	
}
