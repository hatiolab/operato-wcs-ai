package xyz.anythings.base.query.util;

import java.util.List;
import java.util.Map;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.query.store.IndicatorQueryStore;
import xyz.anythings.gw.entity.Gateway;
import xyz.anythings.gw.service.model.IndCommonReq;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 라우터 쿼리 유틸리티
 * 
 * @author shortstop
 */
public class IndicatorQueryUtil {

	/**
	 * 라우터 표시기 코드 리스트 리턴
	 * 
	 * @param gw
	 * @return
	 */
	public static List<String> searchIndCdList(Gateway gw) {
		return searchIndCdList(gw.getDomainId(), gw.getGwNm(), null, null);
	}
	
	/**
	 * 라우터 및 설비 소속 표시기 코드 리스트 리턴
	 * 
	 * @param gw
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public static List<String> searchIndCdList(Gateway gw, String equipType, String equipCd) {
		return searchIndCdList(gw.getDomainId(), gw.getGwNm(), equipType, equipCd);
	}
	
	/**
	 * 설비 소속 표시기 코드 리스트 리턴
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public static List<String> searchIndCdList(Long domainId, String equipType, String equipCd) {
		return searchIndCdList(domainId, null, equipType, equipCd);
	}
	
	/**
	 * 라우터 표시기 코드 리스트 리턴
	 * 
	 * @param domainId
	 * @param gwNm
	 * @return
	 */
	public static List<String> searchIndCdList(Long domainId, String gwNm) {
		return searchIndCdList(domainId, gwNm, null, null);
	}
	
	/**
	 * 라우터 및 설비 소속 표시기 코드 리스트 리턴
	 * 
	 * @param domainId
	 * @param gwNm
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public static List<String> searchIndCdList(Long domainId, String gwNm, String equipType, String equipCd) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).getIndCdListQuery();
		return AnyEntityUtil.searchItems(domainId, false, String.class, sql, "domainId,gwNm,equipType,equipCd,activeFlag", domainId, gwNm, equipType, equipCd, true);
	}
	
	/**
	 * 라우터 및 설비 소속 표시기 코드 리스트 리턴
	 * 
	 * @param domainId
	 * @param gwNm
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @return
	 */
	public static List<String> searchIndCdList(Long domainId, String gwNm, String equipType, String equipCd, String stationCd) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).getIndCdListQuery();
		return AnyEntityUtil.searchItems(domainId, false, String.class, sql, "domainId,gwNm,equipType,equipCd,stationCd,activeFlag", domainId, gwNm, equipType, equipCd, stationCd, true);
	}
	
	/**
	 * 라우터 및 설비 소속 표시기 코드 리스트 리턴
	 * 
	 * @param domainId
	 * @param gwNm
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param equipZone
	 * @return
	 */
	public static List<String> searchIndCdList(Long domainId, String gwNm, String equipType, String equipCd, String stationCd, String equipZone) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).getIndCdListQuery();
		return AnyEntityUtil.searchItems(domainId, false, String.class, sql, "domainId,gwNm,equipType,equipCd,stationCd,equipZone", domainId, gwNm, equipType, equipCd, stationCd, equipZone);
	}
	
	/**
	 * 게이트웨이 소속의 배치 리스트 조회 
	 * 
	 * @param gateway
	 * @return
	 */
	public static List<JobBatch> searchRunningBatchesByGwCd(Gateway gateway) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).getSearchBatchListByGateway();
		Map<String, Object> params = ValueUtil.newMap("domainId,stageCd,gwCd", gateway.getDomainId(), gateway.getStageCd(), gateway.getGwCd());
		return AnyEntityUtil.searchItems(gateway.getDomainId(), false, JobBatch.class, sql, params);
	}
	
	/**
	 * 게이트웨이 리부팅시에 게이트웨이에 내려주기 위한 게이트웨이 소속 표시기 정보 리스트
	 * 
	 * @param gateway
	 * @return
	 */
	/*public static List<GatewayInitResIndList> searchIndListForGwInit(Gateway gateway) {
		return searchIndListForGwInit(gateway.getDomainId(), gateway.getStageCd(), gateway.getGwNm());
	}*/
	
	/**
	 * 게이트웨이 리부팅시에 게이트웨이에 내려주기 위한 게이트웨이 소속 표시기 정보 리스트
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @return
	 */
	/*public static List<GatewayInitResIndList> searchIndListForGwInit(Long domainId, String stageCd, String gwPath) {
		String viewType = StageIndConfigUtil.getIndViewType(domainId, stageCd);
		String sql = BeanUtil.get(IndicatorQueryStore.class).getSearchIndListForGwInitQuery();
		return AnyEntityUtil.searchItems(domainId, true, GatewayInitResIndList.class, sql, "domainId,gwNm,bizType,viewType", domainId, gwPath, "DAS", viewType);
	}*/
	
	/**
	 * 게이트웨이 리부팅시에 게이트웨이에 내려주기 위한 게이트웨이 및 설비 소속 표시기 정보 리스트
	 * 
	 * @param gateway
	 * @param batch
	 * @return
	 */
	/*public static List<GatewayInitResIndList> searchIndListForGwInit(Gateway gateway, JobBatch batch) {
		Long domainId = gateway.getDomainId();
		String viewType = BatchIndConfigUtil.getIndViewType(batch.getId());
		String sql = BeanUtil.get(IndicatorQueryStore.class).getSearchIndListForGwInitQuery();
		return AnyEntityUtil.searchItems(domainId, true, GatewayInitResIndList.class, sql, "domainId,gwCd,bizType,viewType,equipType,equipCd", domainId, gateway.getGwCd(), batch.getJobType(), viewType, batch.getEquipType(), batch.getEquipCd());
	}*/
	
	/**
	 * 장비 소속의 게이트웨이 리스트를 조회
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @return
	 */
	public static List<Gateway> searchGatewayListByEquip(Long domainId, String stageCd, String equipType, String equipCd, String stationCd) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).getSearchGatewaysByEquip();
		return AnyEntityUtil.searchItems(domainId, false, Gateway.class, sql, "domainId,stageCd,equipType,equipCd,stationCd", domainId, stageCd, equipType, equipCd, stationCd);
	}

	/**
	 * Gateway Code로 Gateway Path 조회
	 * 
	 * @param domainId
	 * @param gwCd
	 * @return
	 */
	public static String findGatewayPathByGwCd(Long domainId, String gwCd) {
		Gateway gw = AnyEntityUtil.findEntityBy(domainId, true, Gateway.class, "gw_nm", "domainId,gwCd", domainId, gwCd);
		return gw != null ? gw.getGwNm() : null;
	}
	
	/**
	 * Indicator Code로 Gateway Path 조회
	 * 
	 * @param domainId
	 * @param indCd
	 * @return
	 */
	public static String findGatewayPathByIndCd(Long domainId, String indCd) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).getFindGwPathByInd();
		return AnyEntityUtil.findItem(domainId, false, String.class, sql, "domainId,indCd", domainId, indCd);
	}
	
	/**
	 * 호기 및 장비 작업 존 별 라우터 Path 정보 조회
	 * 
	 * @param domainId
	 * @param rackCd
	 * @param equipZoneCd
	 * @param sideCd
	 * @return
	 */
	public static List<String> searchGwByEquipZone(Long domainId, String rackCd, String equipZoneCd, String sideCd) {
		sideCd = LogisConstants.checkSideCdForQuery(domainId, sideCd);
		String sql = BeanUtil.get(IndicatorQueryStore.class).getSearchIndicatorsQuery();
		return AnyEntityUtil.searchItems(domainId, false, String.class, sql, "domainId,rackCd,equipZone,sideCd,activeFlag", domainId, rackCd, equipZoneCd, sideCd);
	}
	
	/**
	 * 호기 및 장비 존 코드 사이드 코드로 표시기 리스트를 조회  
	 * 
	 * @param domainId
	 * @param rackCd
	 * @param equipZoneCd
	 * @param sideCd
	 * @return
	 */
	public static List<IndCommonReq> searchIndByEquipZone(Long domainId, String rackCd, String equipZoneCd, String sideCd) {
		sideCd = LogisConstants.checkSideCdForQuery(domainId, sideCd);
		String sql = BeanUtil.get(IndicatorQueryStore.class).getSearchIndicatorsQuery();
		return AnyEntityUtil.searchItems(domainId, false, IndCommonReq.class, sql, "domainId,rackCd,equipZone,sideCd,activeFlag,indQueryFlag", domainId, rackCd, equipZoneCd, sideCd, true, true);
	}
	
	/**
	 * 호기 및 호기 작업 존 별 라우터 Path 리스트 조회 
	 * 
	 * @param domainId
	 * @param rackCd
	 * @param stationCd
	 * @return
	 */
	public static List<String> searchGwByStation(Long domainId, String rackCd, String stationCd) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).getSearchIndicatorsQuery();
		return AnyEntityUtil.searchItems(domainId, false, String.class, sql, "domainId,rackCd,stationCd,activeFlag", domainId, rackCd, stationCd, true);
	}
	
	/**
	 * 호기 및 작업 존 코드로 표시기 리스트를 조회
	 * 
	 * @param domainId
	 * @param rackCd
	 * @param zoneCd
	 * @return
	 */
	public static List<IndCommonReq> searchIndByStation(Long domainId, String rackCd, String stationCd) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).getSearchIndicatorsQuery();
		return AnyEntityUtil.searchItems(domainId, false, IndCommonReq.class, sql, "domainId,rackCd,stationCd,activeFlag,indQueryFlag", domainId, rackCd, stationCd, true, true);
	}
	
	/**
	 * 호기 및 장비 존 코드 사이드 코드로 표시기 리스트를 조회
	 * 
	 * @param domainId
	 * @param batchId
	 * @param stationCd
	 * @return
	 */
	public static List<IndCommonReq> searchPickingIndList(Long domainId, String batchId, String stationCd) {
		String sql = BeanUtil.get(IndicatorQueryStore.class).searchPickingIndListQuery();
		return AnyEntityUtil.searchItems(domainId, false, IndCommonReq.class, sql, "domainId,batchId,stationCd,status", domainId, batchId, stationCd, LogisConstants.JOB_STATUS_PICKING);
	}

}
