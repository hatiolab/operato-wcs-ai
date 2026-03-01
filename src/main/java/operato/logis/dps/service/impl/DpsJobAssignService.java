package operato.logis.dps.service.impl;

import operato.logis.dps.model.DpsJobAssign;
import operato.logis.dps.query.store.DpsAssignQueryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DPS 작업 할당 서비스
 * 
 * @author shortstop
 */
@Component("dpsJobAssignService")
public class DpsJobAssignService extends AbstractLogisService {
	/**
	 * 작업 할당에서 제외할 주문 처리 커스텀 서비스
	 */
	private static final String CUSTOM_SERVICE_SKIP_ORDERS = "diy-dps-assign-skip-orders";
	/**
	 * DPS Query Store
	 */
	@Autowired
	private DpsAssignQueryStore dpsAssignQueryStore;
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	private ICustomService customService;
	/**
	 * Job 시작 시간
	 */
	private long jobStartedTime = 0;
	/**
	 * 한 번 실행시 최대 시간
	 */
	private long trxTimeout = 0;
	
	/**
	 * 작업 할당 서비스
	 * 
	 * @param domainId
	 */
	@Transactional
	public void assignDpsJobs(Long domainId) {
		// 1. 스케줄링 활성화 여부 && 이전 작업이 진행 중인 여부 체크
		if(this.isJobEnabeld()) {
			// 2. 진행 중인 배치 리스트 조회
			List<JobBatch> batchList = this.searchRunningBatchList(domainId);
			
			if(ValueUtil.isNotEmpty(batchList)) {
				// 3. 작업 시작 시간 및 한 번 실행시 최대 트랜잭션 시간 설정
				this.jobStartedTime = System.currentTimeMillis();
				this.trxTimeout = ValueUtil.toInteger(SettingUtil.getValue(domainId, "dps.job.assign.order.timeout", "-1"));
				
				try {
					// 4. 현재 진행 중인 작업 배치 별 작업 할당 처리
					for(JobBatch batch : batchList) {
						this.standardAssignJobs(batch);
					}
				} catch(Throwable th) {
					// 5. 에러 발생시 처리
					ElidomRuntimeException ere = new ElidomRuntimeException(th);
					ErrorEvent errorEvent = new ErrorEvent(domainId, "JOB_DPS_ASSIGN_ERROR", ere, null, true, true);
					this.eventPublisher.publishEvent(errorEvent);
					
				} finally {
					// 6. 작업 시작 시간 리셋
					this.jobStartedTime = 0;
				}
			}
		}
	}
	
	/**
	 * 트랜잭션을 계속 수행할 지 여부를 트랜잭션 시간으로 제어
	 * 
	 * @return
	 */
	private boolean isTransactionGoOn(Long domainId) {
		// 최대 시간을 제어하지 않는 경우
		if(this.trxTimeout < 0) {
			return true;
		} else {
			long trxTime = System.currentTimeMillis() - this.jobStartedTime;
			return this.trxTimeout > trxTime;
		}
	}
	
	/**
	 * Scheduler 활성화 여부
	 * 
	 * @return
	 */
	protected boolean isJobEnabeld() {
		// 현재 작업이 진행 중이라면 스킵
		return this.jobStartedTime <= 1; 
	}
	
	/**
	 * 현재 진행 중인 DPS 배치 리스트 조회
	 * 
	 * @param domainId
	 * @return
	 */
	private List<JobBatch> searchRunningBatchList(Long domainId) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("status", JobBatch.STATUS_RUNNING);
		condition.addFilter("jobType", LogisConstants.JOB_TYPE_DPS);
		condition.addOrder("jobDate", false);
		return this.queryManager.selectList(JobBatch.class, condition);
	}
	
	/**
	 * 표준 작업 할당 로직
	 * 
	 * @param batch
	 */
	private void standardAssignJobs(JobBatch batch) {
		// 1. 작업 배치 내 모든 재고 중에 가장 많은 재고 순으로 상품별 재고 수량 조회
		List<Stock> stockList = this.searchStocksForAssign(batch);

		// 2. 재고가 없다면 스킵
		if(ValueUtil.isEmpty(stockList)) {
			return;
		}
		
		// 별도 트랜잭션 처리를 위해 컴포넌트로 호출하기 위함
		DpsJobAssignService dpsJobAssignSvc = BeanUtil.get(DpsJobAssignService.class);

		// 3. 트랜잭션 분리를 위해 자신을 레퍼런스, 트랜잭션 분리는 각 주문 단위 ...
		List<String> skipOrderList = this.searchSkipOrders(batch);

		// 4. 배치 내 SKU가 적치된 재고 수량을 기준으로 많은 재고 조회
		for(Stock stock : stockList) {
			// 4.1 트랜잭션 타임아웃인지 체크 ...
			if(!this.isTransactionGoOn(batch.getDomainId())) {
				break;
			}
			
			// 4.2 현재 시점에 특정 상품의 할당 가능한 재고 총 수량 계산
			Integer stockQty = this.calcTotalStockQty(batch, stock);

			// 4.3 재고 총 수량 체크 
			if(stockQty == null || stockQty < 1) {
				continue;
			}

			// 4.4 재고의 상품이 필요한 주문번호 검색
			List<Order> orders = this.searchOrdersForStock(batch, stock, stockQty, skipOrderList);

			// 4.5 할당이 필요한 주문번호가 없다면 스킵
			if(ValueUtil.isEmpty(orders)) {
				continue;
			}

			// 4.6 할당이 필요한 주문 번호별로 ...
			for(Order order : orders) {
				// 4.6.1 트랜잭션 타임아웃인지 체크 ...
				if(!this.isTransactionGoOn(batch.getDomainId())) {
					break;
				}
				
				// 4.6.2 주문 번호 추출
				String orderNo = order.getOrderNo();
				
				// 4.6.3 처리 못하는 주문 번호 스킵
				if(skipOrderList.contains(orderNo)) {
					break;
				}
				
				// 4.6.4 남은 재고 수량이 주문 수량보다 적으면 해당 상품에 대한 주문 할당 처리는 종료하면서 처리 못하는 주문 번호 리스트에 추가
				if(stockQty < order.getOrderQty()) {
					if(!skipOrderList.contains(orderNo)) {
						skipOrderList.add(orderNo);
					}
					
					break;
				}

				// 4.6.5 해당 주문 별로 주문별 상품별 가용 재고 조회
				List<DpsJobAssign> candidates = this.searchAssignableCandidates(batch, orderNo);
				
				// 4.6.6 주문별 상품별 가용 재고 조회 할당 여부 판별 후 할당
				if(ValueUtil.isNotEmpty(candidates)) {
					try {
						stockQty = dpsJobAssignSvc.assignJobsByStock(stock, order, stockQty, candidates, skipOrderList);
					} catch(Exception e) {
						ErrorEvent errorEvent = new ErrorEvent(batch.getDomainId(), "JOB_ASSIGN_ERROR", e, null, true, true);
						this.eventPublisher.publishEvent(errorEvent);
					}
				}
			}
		}
	}

	/**
	 * 작업 배치 내 모든 재고 중에 가장 많은 재고 순으로 조회
	 * 
	 * @param batch
	 * @return
	 */
	private List<Stock> searchStocksForAssign(JobBatch batch) {
		String sql = this.dpsAssignQueryStore.getStockForJobAssignQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType,equipCd,activeFlag", batch.getDomainId(), batch.getId(), batch.getEquipType(), batch.getEquipCd(), true);
		return this.queryManager.selectListBySql(sql, params, Stock.class, 0, 0);
	}
	
	/**
	 * 스킵 처리할 주문 리스트 조회
	 * 
	 * @param batch
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> searchSkipOrders(JobBatch batch) {
		Object retList = this.customService.doCustomService(batch.getDomainId(), CUSTOM_SERVICE_SKIP_ORDERS, ValueUtil.newMap("batch", batch));
		return (retList == null) ? new ArrayList<String>(1) : (List)retList;
	}

	/**
	 * 현재 시점에 특정 상품의 할당 가능한 재고 총 수량 계산
	 * 
	 * @param batch
	 * @param stock
	 * @return
	 */
	private Integer calcTotalStockQty(JobBatch batch, Stock stock) {
		String sql = "SELECT SUM(S.LOAD_QTY - S.ALLOC_QTY) FROM STOCKS S WHERE S.DOMAIN_ID = :domainId AND S.EQUIP_CD = :equipCd AND S.EQUIP_TYPE = :equipType AND S.COM_CD = :comCd AND S.SKU_CD = :skuCd AND S.ACTIVE_FLAG = :activeFlag AND (S.LOAD_QTY IS NOT NULL AND S.LOAD_QTY > 0)";
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,comCd,skuCd,activeFlag", batch.getDomainId(), batch.getEquipType(), batch.getEquipCd(), stock.getComCd(), stock.getSkuCd(), true);
		return this.queryManager.selectBySql(sql, params, Integer.class);
	}
	
	/**
	 * 해당 재고가 필요한 주문 조회
	 * 
	 * @param batch
	 * @param stock
	 * @param stockQty
	 * @param skipOrderList
	 * @return
	 */
	private List<Order> searchOrdersForStock(JobBatch batch, Stock stock, int stockQty, List<String> skipOrderList) {
		String sql = this.dpsAssignQueryStore.getSearchOrderForStockQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,comCd,skuCd,stockQty,skipOrderIdList", batch.getDomainId(), batch.getId(), stock.getComCd(), stock.getSkuCd(), stockQty, ValueUtil.isEmpty(skipOrderList) ? null : skipOrderList);
		return this.queryManager.selectListBySql(sql, params, Order.class, 0, 0);
	}

	/**
	 * 작업 할당에 필요한 주문 및 재고 조합 정보 조회
	 *  
	 * @param batch
	 * @param orderNo
	 * @return
	 */
	private List<DpsJobAssign> searchAssignableCandidates(JobBatch batch, String orderNo) {
		String sql = this.dpsAssignQueryStore.getSearchAssignCandidatesQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipGroupCd,equipType,equipCd,orderNo", batch.getDomainId(), batch.getId(), batch.getEquipGroupCd(), batch.getEquipType(), batch.getEquipCd(), orderNo);
		return this.queryManager.selectListBySql(sql, params, DpsJobAssign.class, 0, 0);
	}

	/**
	 * 주문별 작업 할당 처리
	 * 
	 * @param stock
	 * @param order
	 * @param stockQty
	 * @param candidates
	 * @param skipOrderList
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int assignJobsByStock(Stock stock, Order order, int stockQty, List<DpsJobAssign> candidates, List<String> skipOrderList) {
		// 1. 주문별 주문 수량 초기화
		int orderQty = 0;
		
		// 2. 주문 할당 대상별로 ...
		for(DpsJobAssign candidate : candidates) {
			// 2.1 주문 번호 추출
			String orderNo = candidate.getOrderNo();
			
			// 2.2 스킵 주문에 포함된 주문이면 스킵
			if(skipOrderList.contains(orderNo)) {
				break;
			}
			
			// 2.3 할당할 수 있는 수량이 아니면 (주문 할당에 필요한 총 주문 수량 보다 총 재고 수량이 적은 경우) 스킵
			if(candidate.getCheckAssignable() != 0) {
				if(!skipOrderList.contains(orderNo)) {
					skipOrderList.add(orderNo);
				}
				
				break;
			}
			
			// 2.4 주문 라인 내 첫 번째 순위인 경우에 - 주문 상품별 첫 번째 순위인 경우 (주문 수량)
			if(candidate.getRanking() == 1) {
				orderQty = candidate.getOrderQty();
				
				// 주문 내 상품 정보가 stock의 상품 정보와 같은 경우는 stockQty를 업데이트
				if(ValueUtil.isEqual(candidate.getSkuCd(), stock.getSkuCd())) {
					stockQty = stockQty - orderQty;
				}
			}

			// 2.5 최종 작업 할당 - 할당 로케이션이 여러 개의 경우에는 할당 후 남은 주문 수량 리턴
			if(orderQty > 0) {
				// 주문 수량이 0 보다 큰 경우에만 할당 데이터 (DpsJobInstance) 생성
				orderQty = this.assignJob(order.getDomainId(), candidate, orderQty);
			}
		}
		
		// 3. 작업 할당 후 남은 재고 수량
		return stockQty;
	}
	
	/**
	 * 주문별 주문 라인별 작업 할당 처리
	 * 
	 * @param domainId
	 * @param candidate
	 * @param orderQty
	 * @return
	 */
	public int assignJob(Long domainId, DpsJobAssign candidate, int orderQty) {
		// 1. 할당 수량 초기화
		int assignQty = (orderQty > candidate.getLoadQty()) ? candidate.getLoadQty() : orderQty;
		if(assignQty == 0) {
			return orderQty;
		}
		
		// 2. JobInstance 데이터 생성
		String sql = this.dpsAssignQueryStore.getAssignJobInstanceQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,orderNo,skuCd,cellCd,assignQty,equipType,equipCd,indCd,colorCd", domainId, candidate.getBatchId(), candidate.getOrderNo(), candidate.getSkuCd(), candidate.getCellCd(), assignQty, candidate.getEquipType(), candidate.getEquipCd(), candidate.getIndCd(), null);
		this.queryManager.executeBySql(sql, params);
		
		// 3. 주문 정보에 작업 할당 상태 업데이트
		sql = "SELECT ID, BATCH_ID, SKU_CD, CLASS_CD, ORDER_QTY, ASSIGN_QTY, STATUS FROM ORDERS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId AND CLASS_CD = :orderNo AND SKU_CD = :skuCd AND STATUS = 'W' ORDER BY ASSIGN_QTY DESC, ORDER_QTY ASC";
		// 남은 할당 수량
		int remainAssignQty = assignQty;
		
		// 4. 동일 주문 번호에 동일 상품 주문이 두 개 이상 있을 수 있으니 이 부분 처리
		List<Order> orders = this.queryManager.selectListBySql(sql, params, Order.class, 0, 0);
		if(ValueUtil.isNotEmpty(orders)) {
			for(Order order : orders) {
				if(remainAssignQty > 0) {
					int prevAssignQty = ValueUtil.toInteger(order.getAssignQty(), 0);
					int orderAssignQty = (prevAssignQty + remainAssignQty > order.getOrderQty()) ? order.getOrderQty() : prevAssignQty + remainAssignQty;
					order.setAssignQty(orderAssignQty);
					if (order.getOrderQty() <= order.getAssignQty()) {
						order.setStatus(Order.STATUS_ASSIGN);
					}
					this.queryManager.update(order, "assignQty", "status", "updatedAt");
					// 남은 할당 수량 계산
					remainAssignQty = remainAssignQty - (orderAssignQty - prevAssignQty);
				}
			}
		} else {
			remainAssignQty = 0;
		}
		
		// 5. 재고 할당 처리
		Query condition = AnyOrmUtil.newConditionForExecution();
		condition.addSelect("id", "equip_type", "equip_cd", "cell_cd", "com_cd", "sku_cd", "alloc_qty", "load_qty", "picked_qty", "stock_qty");
		condition.addFilter("id", candidate.getStockId());
		Stock s = BeanUtil.get(IQueryManager.class).selectByCondition(Stock.class, condition);

		if(s != null) {
			s.assignJob(assignQty);
		}
		
		// 6. 할당 가능 수량을 제외한 주문 수량 리턴
		return orderQty - assignQty;
	}

}
