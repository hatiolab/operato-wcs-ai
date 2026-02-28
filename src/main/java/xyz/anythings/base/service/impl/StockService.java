package xyz.anythings.base.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.entity.StockAdjust;
import xyz.anythings.base.entity.Stocktaking;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.query.store.StockQueryStore;
import xyz.anythings.base.query.util.IndicatorQueryUtil;
import xyz.anythings.base.service.api.IStockService;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.gw.entity.Gateway;
import xyz.anythings.gw.service.IndicatorDispatcher;
import xyz.anythings.gw.service.api.IIndRequestService;
import xyz.anythings.gw.service.model.IIndOnInfo;
import xyz.anythings.gw.service.model.IndOnStockReq;
import xyz.anythings.gw.service.util.MwMessageUtil;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.DateUtil;

/**
 * 재고 서비스 기본 구현
 * 
 * @author shortstop
 */
@Component
public class StockService extends AbstractLogisService implements IStockService {
	/**
	 * 재고 보충 전 커스텀 서비스
	 */
	public static final String CUSTOM_STOCK_PRE_SUPPLEMENT = "diy-dps-pre-stock-supplement";
	/**
	 * 재고 보충 후 커스텀 서비스
	 */
	public static final String CUSTOM_STOCK_POST_SUPPLEMENT = "diy-dps-post-stock-supplement";

	/**
	 * 인디케이터 벤더별 서비스 디스패처 
	 */
	@Autowired
	private IndicatorDispatcher indicatorDispatcher;
	/**
	 * 재고 쿼리 스토어
	 */
	@Autowired
	private StockQueryStore stockQueryStore;
	/**
	 * 커스텀 서비스 
	 */
	@Autowired
	protected ICustomService customService;
	
	
	@Override
	public Stock findStock(Long domainId, String cellCd, boolean exceptionWhenEmpty) {
		String sql = this.stockQueryStore.getSearchStocksQuery();
		return AnyEntityUtil.findItem(domainId, exceptionWhenEmpty, Stock.class, sql, "domainId,cellCd", domainId, cellCd);
	}

	@Override
	public Stock findStock(Long domainId, String cellCd, String comCd, String skuCd, boolean exceptionWhenEmpty) {
		String sql = this.stockQueryStore.getSearchStocksQuery();
		return AnyEntityUtil.findItem(domainId, exceptionWhenEmpty, Stock.class, sql, "domainId,cellCd,comCd,skuCd", domainId, cellCd, comCd, skuCd);
	}

	@Override
	public Stock findOrCreateStock(Long domainId, String cellCd) {
		Stock stock = this.findStock(domainId, cellCd, false);
		
		if(stock == null) {
			stock = this.createStock(domainId, cellCd, null, null, null);
		}
		
		return stock;
	}
	
	@Override
	public Stock findOrCreateStock(Long domainId, String cellCd, String comCd, String skuCd) {
		SKU sku = null;
		
		if(ValueUtil.isNotEmpty(skuCd)) {
			sku = AnyEntityUtil.findEntityBy(domainId, true, SKU.class, "id,com_cd,sku_cd,sku_barcd,sku_nm", "comCd,skuCd", comCd, skuCd);
		}
		
		if(sku != null) {
			return this.findOrCreateStock(domainId, cellCd, sku);
			
		} else {
			Stock stock = this.findStock(domainId, cellCd, false);
			
			if(stock == null) {
				stock = this.createStock(domainId, cellCd, comCd, skuCd, null);
			}
			
			if(ValueUtil.isNotEmpty(skuCd)) {
				stock.setComCd(comCd);
				stock.setSkuCd(skuCd);
			}
			
			return stock;
		}
	}
	
	@Override
	public Stock findOrCreateStock(Long domainId, String cellCd, SKU sku) {
		Stock stock = this.findStock(domainId, cellCd, false);
		
		if(stock == null) {
			stock = this.createStock(domainId, cellCd, sku);
			
		} else {
			if(sku != null) {
				// 재고에 상품 정보가 없다면 SKU 정보를 설정
				if(ValueUtil.isEmpty(stock.getSkuCd())) {
					stock.setComCd(sku.getComCd());
					stock.setSkuCd(sku.getSkuCd());
					stock.setSkuBarcd(sku.getSkuBarcd());
					stock.setSkuNm(sku.getSkuNm());
					
				// 재고의 상품 정보가 SKU의 상품 정보가 다르면
				} else if(ValueUtil.isNotEqual(stock.getSkuCd(), sku.getSkuCd())) {
					// 프리 셀 재고이고 재고가 0이고 모든 피킹이 완료되었다면
					if((stock.getFixedFlag() == null || !stock.getFixedFlag()) && (stock.recalcStock() == 0 && stock.getPickedQty() == stock.getLoadQty())) {
						stock.setComCd(sku.getComCd());
						stock.setSkuCd(sku.getSkuCd());
						stock.setSkuBarcd(sku.getSkuBarcd());
						stock.setSkuNm(sku.getSkuNm());
						stock.setLoadQty(0);
						stock.setAllocQty(0);
						stock.setPickedQty(0);
						stock.setStockQty(0);
						this.queryManager.update(stock);
						
					} else {
						throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("ALREADY_STOCK_ANOTHER_PRODUCT"));
					}
				}
			}
		}
		
		return stock;
	}

	@Override
	public Stock createStock(Long domainId, String cellCd, SKU sku) {
		Cell cell = AnyEntityUtil.findEntityBy(domainId, true, Cell.class, null, "domainId,cellCd", domainId, cellCd);
		Stock stock = new Stock();
		stock.setComCd(sku != null ? sku.getComCd() : null);
		stock.setSkuCd(sku != null ? sku.getSkuCd() : null);
		stock.setSkuNm(sku != null ? sku.getSkuNm() : null);
		stock.setSkuBarcd(sku != null ? sku.getSkuBarcd() : null);
		stock.setCellCd(cellCd);
		stock.setEquipType(cell.getEquipType());
		stock.setEquipCd(cell.getEquipCd());
		stock.setActiveFlag(cell.getActiveFlag());
		stock.setLoadQty(0);
		stock.setAllocQty(0);
		stock.setStockQty(0);
		stock.setPickedQty(0);
		stock.setMinStockQty(0);
		stock.setMaxStockQty(0);
		stock.setLastTranCd(Stock.TRX_CREATE);
		this.queryManager.insert(stock);
		
		return stock;
	}

	@Override
	public Stock createStock(Long domainId, String cellCd, String comCd, String skuCd, String skuNm) {
		SKU sku = null;
		
		if(ValueUtil.isNotEmpty(comCd) && ValueUtil.isNotEmpty(skuCd) && ValueUtil.isEmpty(skuNm)) {
			sku = AnyEntityUtil.findEntityBy(domainId, true, SKU.class, "id,com_cd,sku_cd,sku_barcd,sku_nm", "comCd,skuCd", comCd, skuCd);
		} else {
			sku = new SKU();
			sku.setComCd(comCd);
			sku.setSkuCd(skuCd);
			sku.setSkuNm(skuNm);
		}
		
		return this.createStock(domainId, cellCd, sku);
	}

	@Override
	public Stock addStock(Stock stock, String tranCd, int addQty) {
		long domainId = stock.getDomainId();
		
		// 1. 재고 보충 전 커스텀 서비스 
		Map<String, Object> params = ValueUtil.newMap("domainId,stock,addQty", domainId, stock, addQty);
		this.customService.doCustomService(domainId, CUSTOM_STOCK_PRE_SUPPLEMENT, params);
		
		// 2. 재고 보충 
		Stock retStock = stock.addStock(addQty);
		
		// 3. 재고 보충 후 커스텀 서비스 
		params.put("stock", retStock);
		this.customService.doCustomService(domainId, CUSTOM_STOCK_POST_SUPPLEMENT, params);
		
		return retStock;
	}

	@Override
	public Stock removeStock(Stock stock, String tranCd, int removeQty) {
		// tranCd가 pick이면 할당 수량을 빼야하고 그렇지 않으면 적치 수량을 뺀다.
		if(ValueUtil.isEqualIgnoreCase(tranCd, Stock.TRX_PICK)) {
			return stock.pickJob(removeQty);
		} else {
			return stock.removeStock(removeQty);
		}
	}
	
	@Override
	public Stock adjustStock(Long domainId, String tranCd, String cellCd, String comCd, String skuCd, int adjustQty) {
		Stock stock = this.findStock(domainId, cellCd, comCd, skuCd, false);
		
		if(stock == null) {
			stock = this.findStock(domainId, cellCd, true);
		}
		
		return stock.adjustStock(adjustQty);
	}
	
	@Override
	public Stock supplyStock(Long domainId, String cellCd, SKU sku, int loadQty) {
		Stock stock = this.findOrCreateStock(domainId, cellCd);
		
		if(ValueUtil.isEmpty(stock.getSkuCd()) || ValueUtil.isEqualIgnoreCase(stock.getSkuCd(), sku.getSkuCd())) {
			stock.setComCd(sku.getComCd());
			stock.setSkuCd(sku.getSkuCd());
			stock.setSkuNm(sku.getSkuNm());
			stock.setSkuBarcd(sku.getSkuBarcd());
		}
		
		return stock.addStock(loadQty);
	}
	
	@Override
	public List<Stock> searchStocksBySku(Long domainId, String equipType, String equipCd, String comCd, String skuCd) {
		String sql = this.stockQueryStore.getSearchStocksQuery();
		return AnyEntityUtil.searchItems(domainId, false, Stock.class, sql, "domainId,equipType,equipCd,comCd,skuCd", domainId, equipType, equipCd, comCd, skuCd);
	}
	
	@Override
	public List<Stock> searchStocksBySku(Long domainId, String equipType, String equipCd, Boolean fixedFlag, String comCd, String skuCd) {
		String sql = this.stockQueryStore.getSearchStocksQuery();
		return AnyEntityUtil.searchItems(domainId, false, Stock.class, sql, "domainId,equipType,equipCd,comCd,skuCd", domainId, equipType, equipCd, comCd, skuCd);
	}

	@Override
	public List<Stock> recommendCells(Long domainId, String batchId, String comCd, String skuCd, Boolean fixedFlag) {
		// 1. 조회 조건
		String sql = this.stockQueryStore.getSearchStocksQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,comCd,skuCd", domainId, batchId, comCd, skuCd);
		if(fixedFlag != null) {
			params.put("fixedFlag", fixedFlag);
		}
		
		// 2. 추천 셀 조회
		List<Stock> stocks = AnyEntityUtil.searchItems(domainId, false, Stock.class, sql, params);
		
		// 3. 추천 셀이 하나 이상이라면 자유식 셀 부터 
		if(ValueUtil.isNotEmpty(stocks)) {
			List<Stock> newStocks = new ArrayList<Stock>();
			
			for(Stock stock : stocks) {
				if(stock.getFixedFlag() == null || !stock.getFixedFlag()) {
					newStocks.add(stock);
				}
			}
			
			for(Stock stock : stocks) {
				if(stock.getFixedFlag()) {
					newStocks.add(stock);
				}
			}
			
			return newStocks;
		}
		
		// 4. 리턴 
		return stocks;
	}
	
	@Override
	public List<Stock> recommendFixedCells(Long domainId, String equipType, String equipCd, String comCd, String skuCd) {
		// 1. Fixed 셀 추천 조회 조건
		String sql = this.stockQueryStore.getSearchStocksQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,comCd,skuCd,fixedFlag", domainId, equipType, equipCd, comCd, skuCd, true);
		
		// 2. 추천 셀 조회
		return AnyEntityUtil.searchItems(domainId, false, Stock.class, sql, params);
	}
	
	@Override
	public List<Stock> recommendFreeCells(Long domainId, String batchId, String comCd, String skuCd) {
		// 1. Free 셀 추천 조회 조건
		String sql = this.stockQueryStore.getSearchStocksQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,comCd,skuCd,fixedFlag", domainId, batchId, comCd, skuCd, false);
		
		// 2. 추천 셀 조회
		return AnyEntityUtil.searchItems(domainId, false, Stock.class, sql, params);
	}
	
	@Override
	public Stock calcuateOrderStock(Stock stock) {
		// 1. 고정식인 경우
		if(stock.getFixedFlag() != null && stock.getFixedFlag()) {
			int inputQty = stock.getMinStockQty() - stock.getLoadQty();
			if(inputQty < 0) {
				inputQty = 0;
			}
			
			stock.setStockQty(stock.getLoadQty() - stock.getAllocQty());
			stock.setInputQty(inputQty);
			return stock;
			
		// 2. 자유식인 경우
		} else {
			EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(stock.getDomainId(), stock.getEquipType(), stock.getEquipCd());
			JobBatch batch = equipBatchSet.getBatch();
			return this.calculateSkuOrderStock(stock.getDomainId(), batch.getId(), stock.getEquipType(), stock.getEquipCd(), stock.getComCd(), stock.getSkuCd());
		}
	}
	
	@Override
	public int calcSkuInputQty(String batchId, Stock stock) {
		String sql = this.stockQueryStore.getCalcSkuSupplementQtyQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType,equipCd,cellCd,comCd,skuCd", stock.getDomainId(), batchId, stock.getEquipType(), stock.getEquipCd(), stock.getCellCd(), stock.getComCd(), stock.getSkuCd());
		Stock stockStatus = this.queryManager.selectBySql(sql, params, Stock.class);
		return stockStatus.getInputQty();
	}
	
	@Override
	public Stock calculateSkuOrderStock(Long domainId, String batchId, String equipType, String equipCd, String comCd, String skuCd) {
		// 1. 여러 호기를 하나로 쓰느냐 개별 호기별로 별도 배치로 운영하느냐 (멀티 호기 모드) 하는 운영 모드 조회
		String sql = "select rack_cd from racks where domain_id = :domainId and batch_id = :batchId";
		int multiRackCount = this.queryManager.selectSizeBySql(sql, ValueUtil.newMap("domainId,batchId", domainId, batchId));
		boolean multiRackMode = multiRackCount > 1;
		
		// 2. 보충 수량 조회
		sql = multiRackMode ? this.stockQueryStore.getCalcSkuSupplementQty2Query() : this.stockQueryStore.getCalcSkuSupplementQtyQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType,comCd,skuCd", domainId, batchId, equipType, comCd, skuCd);
		if(!multiRackMode) {
			params.put("equipCd", equipCd);
		}
		
		// 3. 조회 결과 리턴
		return this.queryManager.selectBySql(sql, params, Stock.class);
	}

	@Override
	public boolean toggleLedSettingForStock(Long domainId, boolean on, String equipType, String equipCd) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int ledOnShortageStocks(Long domainId, String equipType, String equipCd) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int ledOffByEquip(Long domainId, String equipType, String equipCd) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public Stock adjustStock(Long domainId, String stocktakingId, String indCd, int fromQty, int toQty) {
		// 1. 재고 실사 정보를 추출
		Stocktaking stockTaking = this.queryManager.select(Stocktaking.class, stocktakingId);
		
		// 2. 재고 실사 정보 유효성 체크
		this.checkStocktakingRunning(stockTaking);
		
		// 3. 표시기 호출 서비스 & 표시기 색깔 조회
		String stageCd = stockTaking.getStageCd();
		IIndRequestService indReqSvc = this.getIndicatorRequestService(domainId, stageCd);
		String indColor = this.getIndicatorColor(domainId, stageCd, stockTaking.getEquipType(), stockTaking.getEquipCd());
		
		// 4. 재고 정보 조회
		IndOnStockReq stockOnReq = this.findStockByIndCd(domainId, stockTaking.getEquipType(), stockTaking.getEquipCd(), indCd, indColor);
		
		// 5. 재고 정보가 없다면 에러
		if(stockOnReq == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_FOUND_STOCK_INDICATOR"));
		}
		
		// 6. 재고 수량과 조정 수량이 다른지 체크 - 재고 수량과 조정 수량이 같으면 조정의 의미가 없음
		if (fromQty == toQty) {
			// 재고 정보로 display로 재점등
			indReqSvc.requestIndDisplayOnly(domainId, stageCd, LogisConstants.JOB_TYPE_DPS, stockOnReq.getGwPath(), indCd, stockOnReq.getStockId(), stockOnReq.getLoadQty());
			return null;
		}

		// 7. Stock 정보를 메시지에서 추출하여 재고 조회
		Stock stock = Stock.findByIndCd(domainId, indCd, false);
		if(stock == null) {
			return null;
		}
		
		// 8. 재고 조정 정보 생성
		StockAdjust adjustment = new StockAdjust();
		adjustment.setDomainId(domainId);
		adjustment.setStocktakingId(stocktakingId);
		adjustment.setComCd(stock.getComCd());
		adjustment.setCellCd(stock.getCellCd());
		adjustment.setIndCd(indCd);
		adjustment.setSkuCd(stock.getSkuCd());
		adjustment.setPrevStockQty(fromQty);
		adjustment.setAfterStockQty(toQty);
		adjustment.setAdjustQty(toQty - fromQty);
		this.queryManager.insert(adjustment);

		// 9. 적치 수량 변경.
		stock.adjustStock(toQty - fromQty);
		
		// 10. 현재 수량을 다시 점등
		stockOnReq = this.findStockByIndCd(domainId, stockTaking.getEquipType(), stockTaking.getEquipCd(), indCd, indColor);
		if(stockOnReq != null) {
			List<IndOnStockReq> stockOnList = ValueUtil.toList(stockOnReq);
			Map<String, List<IIndOnInfo>> groupStockByGw = MwMessageUtil.groupStockByGwPath(stocktakingId, indColor, stockOnList);
			indReqSvc.requestIndListOnForStocktake(domainId, stageCd, groupStockByGw);
		}
		
		// 11. 재고 정보 리턴
		return stock;
	}
	
	@Override
	public void startStocktaking(Long domainId, String equipType, List<String> equipCdList) {
		String today = DateUtil.todayStr();
		
		for(String equipCd : equipCdList) {
			this.startStocktaking(domainId, today, LogisConstants.EQUIP_TYPE_RACK, equipCd);
		}
	}

	@Override
	public void startStocktaking(Long domainId, String today, String equipType, String equipCd) {
		// 1. 랙 조회
		Map<String, Object> condition = ValueUtil.newMap("domainId,rackCd", domainId, equipCd);
		Rack rack = this.queryManager.selectByCondition(Rack.class, condition);
		
		if(rack == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("RACK_NOT_EXIST"));
		}
		
		// 2. 이미 재고 실사 중인지 체크 - 재고 실사 상태가 진행 중인 것이 있는지 체크
		Stocktaking latestStockTaking = this.findLatestStocktaking(domainId, today, equipType, equipCd);
		
		if(latestStockTaking != null && ValueUtil.isEqualIgnoreCase(latestStockTaking.getStatus(), Stocktaking.STATUS_RUNNING)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("LOGIS_STOCK_TAKING_IN_THE_REGION"));
		}
		
		// 3. 표시기 색깔 조회
		String stageCd = rack.getStageCd();
		String indColor = this.getIndicatorColor(domainId, equipType, equipCd, stageCd);
		
		// 4. 재고 정보가 없다면 재고 실사를 할 수 없으므로 재고 정보가 있는지 체크 
		List<IndOnStockReq> stockList = this.searchStocksByEquipCd(domainId, equipType, equipCd, indColor, true);
		if (ValueUtil.isEmpty(stockList)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("LOGIS_IMPOSSIBLE_STOCK_TAKING"));
		}
		
		// 5. 재고 실사 정보 추가
		Stocktaking newStockTaking = new Stocktaking();
		newStockTaking.setDomainId(domainId);
		newStockTaking.setStageCd(stageCd);
		newStockTaking.setEquipType(equipType);
		newStockTaking.setEquipCd(equipCd);
		newStockTaking.setJobDate(DateUtil.todayStr());
		newStockTaking.setJobSeq(latestStockTaking == null ? 1 : latestStockTaking.getJobSeq() + 1);
		newStockTaking.setStatus(Stocktaking.STATUS_RUNNING);
		this.queryManager.insert(newStockTaking);
		
		// 6. 재고 정보 기반으로 표시기에 점등 요청
		IIndRequestService indReqSvc = this.getIndicatorRequestService(domainId, stageCd);
		Map<String, List<IIndOnInfo>> groupStockByGw = MwMessageUtil.groupStockByGwPath(newStockTaking.getId(), indColor, stockList);
		indReqSvc.requestIndListOnForStocktake(domainId, stageCd, groupStockByGw);
	}

	@Override
	public void finishStocktaking(Long domainId, String stocktakingId) {
		// 1. 재고 실사 정보 조회
		Stocktaking stockTaking = this.queryManager.select(Stocktaking.class, stocktakingId);
		
		// 2. 재고 실사 정보 유효성 체크
		this.checkStocktakingRunning(stockTaking);
		
		// 3. 상태 업데이트
		stockTaking.setStatus(Stocktaking.STATUS_FINISHED);
		this.queryManager.update(stockTaking, OrmConstants.ENTITY_FIELD_STATUS, OrmConstants.ENTITY_FIELD_UPDATER_ID, OrmConstants.ENTITY_FIELD_UPDATED_AT);
		
		// 4. 해당 호기의 모든 표시기 소등
		String stageCd = stockTaking.getStageCd();
		IIndRequestService indReqSvc = this.getIndicatorRequestService(domainId, stageCd);
		List<Gateway> gwList = IndicatorQueryUtil.searchGatewayListByEquip(domainId, stageCd, stockTaking.getEquipType(), stockTaking.getEquipCd(), null);
		
		if(ValueUtil.isNotEmpty(gwList)) {
			for(Gateway gw : gwList) {
				List<String> indCdList = IndicatorQueryUtil.searchIndCdList(domainId, gw.getGwNm(), stockTaking.getEquipType(), stockTaking.getEquipCd(), null);
				indReqSvc.requestIndListOff(domainId, stageCd, gw.getGwNm(), indCdList, false);
			}
		}
	}

	@Override
	public Stocktaking findLatestStocktaking(Long domainId, String date, String equipType, String equipCd) {
		String sql = this.stockQueryStore.getLatestStocktakingQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,date", domainId, equipType, equipCd, date);
		return this.queryManager.selectBySql(sql.toString(), params, Stocktaking.class);
	}

	@Override
	public void removeStockForPicking(Long domainId, String equipType, String equipCd, String cellCd, String comCd, String skuCd, int pickQty) {
		// 1. Lock을 걸고 재고 조회
		Stock stock = AnyEntityUtil.findEntityBy(domainId, false, false, Stock.class, null, "equipType,equipCd,cellCd,comCd,skuCd", equipType, equipCd, cellCd, comCd, skuCd);
		// 2. 재고 피킹 처리
		if(stock != null) {
			stock.pickJob(pickQty);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//												Private Methods
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 표시기 점, 소등 요청 서비스
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	private IIndRequestService getIndicatorRequestService(Long domainId, String stageCd) {
		IIndRequestService indReqSvc = this.indicatorDispatcher.getIndicatorRequestServiceByStage(domainId, stageCd);
		return indReqSvc;
	}
	
	/**
	 * 재고 실사가 가능한 상태인 지 체크
	 * 
	 * @param stockTaking
	 */
	private void checkStocktakingRunning(Stocktaking stockTaking) {
		// 재고 실사 정보 유효성 체크
		if(stockTaking == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NO_STOCK_DUE_DILIGENCE_INFO"));
		} else {
			if(ValueUtil.isEqualIgnoreCase(Stocktaking.STATUS_FINISHED, stockTaking.getStatus())) {
				throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("STOCK_DUE_DILIGENCE_ALREAY_END"));
			}
		}
	}
	
	/**
	 * 랙 별 재고 목록 조회
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @param indColor
	 * @param fixedCell
	 * @return
	 */
	private List<IndOnStockReq> searchStocksByEquipCd(Long domainId, String equipType, String equipCd, String indColor, boolean fixedCell) {
		
		String sql = this.stockQueryStore.getSearchStocksForStocktakingQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,indColor", domainId, equipType, equipCd, indColor);
		if(fixedCell) params.put("fixedCell", true);
		return queryManager.selectListBySql(sql, params, IndOnStockReq.class, 0, 0);
	}
	
	/**
	 * 표시기 별 재고 조회
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @param indCd
	 * @param indColor
	 * @return
	 */
	private IndOnStockReq findStockByIndCd(Long domainId, String equipType, String equipCd, String indCd, String indColor) {
		
		String sql = this.stockQueryStore.getSearchStocksForStocktakingQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,indCd,indColor", domainId, equipType, equipCd, indCd, indColor);
		return queryManager.selectBySql(sql, params, IndOnStockReq.class);
	}
	
	/**
	 * 표시기 점등 컬러 조회
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	private String getIndicatorColor(Long domainId, String stageCd, String equipType, String equipCd) {
		return LogisConstants.COLOR_RED;
	}

}
