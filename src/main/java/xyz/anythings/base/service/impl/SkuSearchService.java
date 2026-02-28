package xyz.anythings.base.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.query.store.BatchQueryStore;
import xyz.anythings.base.service.api.ISkuSearchService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.base.service.util.StageJobConfigUtil;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * SKU 검색 / 조회 서비스 API 기본 구현
 * 
 * @author shortstop
 */
@Component("baseSkuSearchService")
public class SkuSearchService extends AbstractLogisService implements ISkuSearchService {

	@Autowired
	private BatchQueryStore batchQueryStore;
	
	@Override
	public String validateSkuCd(JobBatch batch, String skuCd) {
		skuCd = ValueUtil.isEmpty(skuCd) ? skuCd : skuCd.trim();
		
		// 1. SKU 바코드 최대 길이 체크
		int maxLength = BatchJobConfigUtil.getMaxBarcodeSize(batch);
		if(maxLength > 1) {
			skuCd = (skuCd.length() > maxLength) ? skuCd.substring(0, maxLength) : skuCd;
		}
		
		// 2. 상품 코드 유효성 체크 설정에 따라서 정규표현식 체크
		String skuCdRule = BatchJobConfigUtil.getSkuCdValidationRule(batch);
		if(ValueUtil.isNotEmpty(skuCdRule)) {
			if(!Pattern.matches(skuCdRule, skuCd)) {
				// 상품 코드가 유효하지 않습니다.
				throw ThrowUtil.newAIsInvalid("terms.label.sku_cd");
			}
		}
		
		return skuCd;
	}
	
	@Override
	public List<SKU> searchListInBatchGroupForReturn(Long domainId, String batchGroupId, String skuCd, boolean exceptionWhenEmpty) {
		String sql = this.batchQueryStore.getSearchSkuInBatchGroupForReturnQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchGroupId,skuCd", domainId, batchGroupId, skuCd);
		return this.searchSkuList(skuCd, sql, params, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatchGroup(JobBatch batch, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		return this.searchListInBatch(batch, batch.getComCd(), skuCd, todoOnly, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatchGroup(JobBatch batch, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		//skuCd = this.validateSkuCd(batch, skuCd);
		String[] skuCodeFields = BatchJobConfigUtil.getSkuSearchConditionFields(batch);
		
		String sql = this.batchQueryStore.getSearchSkuInBatchGroupQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchGroupId,comCd", batch.getDomainId(), batch.getBatchGroupId(), comCd);
		if(todoOnly) {
			params.put(SysConstants.ENTITY_FIELD_STATUS, LogisConstants.JOB_STATUS_WIPC);
		}
		
		for(String skuCodeField : skuCodeFields) {
			params.put(skuCodeField, skuCd);
		}
		
		return this.searchSkuList(skuCd, sql, params, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatch(JobBatch batch, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		return this.searchListInBatch(batch, batch.getComCd(), skuCd, todoOnly, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatch(JobBatch batch, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		return this.searchListInBatch(batch, null, comCd, skuCd, todoOnly, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatch(JobBatch batch, String stationCd, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		return this.searchListInBatch(batch, stationCd, comCd, skuCd, null, todoOnly, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchList(JobBatch batch, String skuCd) {
		String[] skuCodeFields = BatchJobConfigUtil.getSkuSearchConditionFields(batch);
		String selectFields = "*"; //BatchJobConfigUtil.getSkuSearchSelectFields(batch);
		Map<String, Object> params = new HashMap<String, Object>();
		
		StringJoiner sql = new StringJoiner(LogisConstants.LINE_SEPARATOR);
		sql.add("SELECT ").add(selectFields).add(" FROM SKU WHERE ");

		int idx = 0;
		for(String skuCodeField : skuCodeFields) {
			params.put(skuCodeField, skuCd);
			sql.add((idx > 0 ? "or" : "") + " " + FormatUtil.toUnderScore(skuCodeField) + " = :" + skuCodeField);
			idx++;
		}
		
		return this.queryManager.selectListBySql(sql.toString(), params, SKU.class, 0, 0);
	}
	
    @Override
    public List<SKU> searchList(Long domainId, String stageCd, String skuCd) {
        String skuSearchCondFields = ValueUtil.toString(StageJobConfigUtil.getSearchSkuFields(stageCd, null), "skuCd,skuBarcd,skuBarcd2,skuBarcd3");
        String[] skuCodeFields = skuSearchCondFields.split(SysConstants.COMMA);
        String selectFields = ValueUtil.toString(StageJobConfigUtil.getSkuSearchSelectFields(stageCd, null), "*");
        Map<String, Object> params = new HashMap<String, Object>();
        
        StringJoiner sql = new StringJoiner(LogisConstants.LINE_SEPARATOR);
        sql.add("SELECT ").add(selectFields).add(" FROM SKU WHERE ");

        int idx = 0;
        for(String skuCodeField : skuCodeFields) {
            params.put(skuCodeField, skuCd);
            sql.add((idx > 0 ? "or" : "") + " " + FormatUtil.toUnderScore(skuCodeField) + " = :" + skuCodeField);
            idx++;
        }
        
        return this.queryManager.selectListBySql(sql.toString(), params, SKU.class, 0, 0);
    }
	
	/**
	 * 조회 쿼리, 파라미터로 상품 조회
	 * 
	 * @param skuCd
	 * @param sql
	 * @param params
	 * @param exceptionWhenEmpty
	 * @return
	 */
	protected List<SKU> searchSkuList(String skuCd, String sql, Map<String, Object> params, boolean exceptionWhenEmpty) {
		List<SKU> skuList = this.queryManager.selectListBySql(sql, params, SKU.class, 0, 0);
		
		if(ValueUtil.isEmpty(skuList) && exceptionWhenEmpty) {
			throw ThrowUtil.newValidationErrorWithNoLog(ThrowUtil.notFoundRecordMsg("terms.menu.SKU", skuCd));
		}
		
		return skuList;
	}

	@Override
	public SKU findSku(Long domainId, String comCd, String skuCd, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("comCd", comCd);
		condition.addFilter("skuCd", skuCd);
		SKU sku = this.queryManager.selectByCondition(SKU.class, condition);

		if(sku == null && exceptionWhenEmpty) {
			throw ThrowUtil.newValidationErrorWithNoLog(ThrowUtil.notFoundRecordMsg("terms.menu.SKU", skuCd));
		}

		return sku;
	}

	@Override
	public SKU findSku(Long domainId, String stageCd, String comCd, String skuCd, String skuBarcd, boolean exceptionFlag) {
		return findSKU(domainId, exceptionFlag, StageJobConfigUtil.getSearchSkuFields(stageCd, null), "comCd,skuCd,skuBarcd", comCd, skuCd, skuBarcd);
	}
	
	@Override
	public SKU findSKU(Long domainId, boolean exceptionWhenEmpty, String selectFields, String paramNames, Object ... paramValues) {
		SKU sku = AnyEntityUtil.findEntityBy(domainId, false, SKU.class, selectFields, paramNames, paramValues);

		if(sku == null && exceptionWhenEmpty) {
			throw ThrowUtil.newValidationErrorWithNoLog(ThrowUtil.notFoundRecordMsg("terms.menu.SKU"));
		}

		return sku;
	}

	@Override
	public Float findSkuWeight(Long domainId, String comCd, String skuCd, boolean exceptionFlag) {
		SKU sku = this.getSkuForWeight(domainId, comCd, skuCd, exceptionFlag);
		return (sku == null || sku.getSkuWt() == null) ? 0.0f : sku.getSkuWt();
	}

	@Override
	public Float findSkuWeight(Long domainId, String comCd, String skuCd, String toUnit, boolean exceptionFlag) {
		SKU sku = this.getSkuForWeight(domainId, comCd, skuCd, exceptionFlag);
		Float weight = (sku == null) ? 0.0f : sku.getSkuWt();

		if(sku != null && ValueUtil.isNotEmpty(weight) && weight > 0.0f) {
			weight = this.convertWeightToUnit(weight, sku.getWtUnit(), LogisCodeConstants.WEIGHT_UNIT_KG);
		}

		return weight;
	}
	
	@Override
	public SKU getSkuWeight(Long domainId, String comCd, String skuCd, boolean exceptionFlag) {
		return this.getSkuWeight(domainId, comCd, skuCd, null, exceptionFlag);
	}
	
	@Override
	public SKU getSkuWeight(Long domainId, String comCd, String skuCd, String toUnit, boolean exceptionFlag) {
		SKU sku = this.getSkuForWeight(domainId, comCd, skuCd, exceptionFlag);
		Float skuWt = sku.getSkuWt();
		
		if(ValueUtil.isNotEmpty(skuWt) && skuWt > 0.0f) {
			skuWt = this.convertWeightToUnit(skuWt, sku.getWtUnit(), LogisCodeConstants.WEIGHT_UNIT_KG);
			sku.setSkuWt(skuWt);
		}
		
		return sku;
	}
	
	/**
	 * 고객사 코드, 상품 코드로 상품 조회
	 * 
	 * @param comCd
	 * @param skuCd
	 * @param exceptionFlag
	 * @return
	 */
	protected SKU getSkuForWeight(Long domainId, String comCd, String skuCd, boolean exceptionFlag) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId, 1, 1, "sku_wt", "wt_unit");
		condition.addFilter("comCd", comCd);
		condition.addFilter("skuCd", skuCd);
		SKU sku = this.queryManager.selectByCondition(SKU.class, condition);

		if(sku == null && exceptionFlag) {
			throw ThrowUtil.newValidationErrorWithNoLog(ThrowUtil.notFoundRecordMsg("terms.menu.SKU", skuCd));
		}
		
		return sku;
	}
	
	/**
	 * 중량 단위 변환 (KG -> G, G -> KG)
	 * 
	 * @param skuWeight
	 * @param skuWtUnit
	 * @param toUnit
	 * @return
	 */
	protected Float convertWeightToUnit(Float skuWeight, String skuWtUnit, String toUnit) {
		if(ValueUtil.isNotEmpty(skuWeight) && skuWeight > 0.0f) {
			if(!ValueUtil.isEqualIgnoreCase(skuWtUnit, toUnit)) {
				// 1. KG -> G
				if(ValueUtil.isEqualIgnoreCase(toUnit, LogisCodeConstants.WEIGHT_UNIT_G)) {
					skuWeight = (skuWeight * 1000.0f);
				// 2. G -> KG
				} else if(ValueUtil.isEqualIgnoreCase(toUnit, LogisCodeConstants.WEIGHT_UNIT_KG)) {
					skuWeight = (skuWeight / 1000.0f);
				}
			}
		}

		return skuWeight;
	}

	@Override
	public List<SKU> searchListInBatch(JobBatch batch, String stationCd, String comCd, String skuCd, String boxId,
			boolean todoOnly, boolean exceptionWhenEmpty) {
		//skuCd = this.validateSkuCd(batch, skuCd);
		String[] skuCodeFields = BatchJobConfigUtil.getSkuSearchConditionFields(batch);
		String sql = BeanUtil.get(BatchQueryStore.class).getSearchSkuInBatchQuery();
		
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,comCd", batch.getDomainId(), batch.getId(), comCd);
		stationCd = LogisServiceUtil.filterAllCondition(stationCd);
		if(ValueUtil.isNotEmpty(stationCd)) {
			params.put("stationCd", stationCd);
		}
		if(ValueUtil.isNotEmpty(boxId)) {
			params.put("boxId", boxId);
		}
		
		if(todoOnly) {
			params.put(SysConstants.ENTITY_FIELD_STATUS, LogisConstants.JOB_STATUS_WIPC);
		}
		
		for(String skuCodeField : skuCodeFields) {
			params.put(skuCodeField, skuCd);
		}
		
		return this.searchSkuList(skuCd, sql, params, exceptionWhenEmpty);
	}

}
