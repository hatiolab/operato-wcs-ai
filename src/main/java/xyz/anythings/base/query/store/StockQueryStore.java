package xyz.anythings.base.query.store;

import org.springframework.stereotype.Component;

/**
 * 재고 관련 쿼리 스토어 
 * 
 * @author shortstop
 */
@Component
public class StockQueryStore extends LogisBaseQueryStore {

	/**
	 * 셀 검색 쿼리
	 * 
	 * @return
	 */
	public String getSearchCellsQuery() {
		return this.getQueryByPath("stock/SearchCells");
	}
	
	/**
	 * 재고 검색 쿼리
	 * 
	 * @return
	 */
	public String getSearchStocksQuery() {
		return this.getQueryByPath("stock/SearchStocks");
	}
	
	/**
	 * 상품별 보충 필요 수량 재고 검색 쿼리 - 싱글 호기의 경우
	 * 
	 * @return
	 */
	public String getCalcSkuSupplementQtyQuery() {
		return this.getQueryByPath("stock/CalcSkuSupplementQty");
	}
	
	/**
	 * 상품별 보충 필요 수량 재고 검색 쿼리 - 멀티 호기의 경우
	 * 
	 * @return
	 */
	public String getCalcSkuSupplementQty2Query() {
		return this.getQueryByPath("stock/CalcSkuSupplementQty2");
	}
	
	/**
	 * 최근 재고 실사 조회 쿼리
	 * 
	 * @return
	 */
	public String getLatestStocktakingQuery() {
		return this.getQueryByPath("stock/FindLatestStocktaking");
	}
	
	/**
	 * 재고 실사를 위한 재고 조회
	 * 
	 * @return
	 */
	public String getSearchStocksForStocktakingQuery() {
		return this.getQueryByPath("stock/SearchStocksForStocktaking");
	}

}
