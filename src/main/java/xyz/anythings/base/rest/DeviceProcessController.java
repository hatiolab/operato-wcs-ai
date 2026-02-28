package xyz.anythings.base.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.SerialInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.IClassifyInEvent;
import xyz.anythings.base.event.classfy.CategorizeEvent;
import xyz.anythings.base.event.classfy.ClassifyInEvent;
import xyz.anythings.base.event.classfy.ClassifyOutEvent;
import xyz.anythings.base.event.classfy.ClassifyRunEvent;
import xyz.anythings.base.event.rest.DeviceProcessRestEvent;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.model.Category;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.api.IAssortService;
import xyz.anythings.base.service.api.IIndicationService;
import xyz.anythings.base.service.impl.LogisServiceDispatcher;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.event.model.FindPrintInfoEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.rest.DynamicControllerSupport;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 작업자 디바이스와의 인터페이스 API
 * 
 * @author shortstop
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/device_process")
@ServiceDesc(description = "Device Process Controller API")
public class DeviceProcessController extends DynamicControllerSupport {
	
	/**
	 * 쿼리 매니저
	 */
	@Autowired
	private IQueryManager queryManager;
	/**
	 * 서비스 디스패처
	 */
	@Autowired
	private LogisServiceDispatcher serviceDispatcher;
	
	/**********************************************************************
	 * 								공통 API
	 **********************************************************************/
	/**
	 * 장비 업데이트 하라는 메시지를 장비 타입별로 publish
	 * 
	 * @param deviceType
	 * @return
	 */
	@RequestMapping(value = "/publish/device_update/{device_type}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Publish device update message")
	public BaseResponse publishDeviceUpdate(@PathVariable("device_type") String deviceType) {
		
		this.serviceDispatcher.getDeviceService().sendDeviceUpdateMessage(Domain.currentDomainId(), deviceType);
		return new BaseResponse(true, LogisConstants.OK_STRING);
	}
	
	/**
	 * 디바이스 업데이트 릴리즈 노트를 조회
	 * 
	 * @param deviceType
	 * @return
	 */
	@RequestMapping(value = "/release_notes/{device_type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Release notes of device type")
	public List<Map<String, Object>> releaseNotesOfDevice(@PathVariable("device_type") String deviceType) {
		
		//List<String> releaseNotes = this.serviceDispatcher.getDeviceService().searchUpdateItems(Domain.currentDomainId(), deviceType);
		
		// TODO
		List<Map<String, Object>> releaseNotes = new ArrayList<Map<String, Object>>(5);
		releaseNotes.add(ValueUtil.newMap("seq,content", 1, "디자인 테마 변경"));
		releaseNotes.add(ValueUtil.newMap("seq,content", 2, "메뉴 아이콘 변경"));
		releaseNotes.add(ValueUtil.newMap("seq,content", 3, "모든 엔티티에 대해서 단 건 조회, 리스트 조회, 페이지네이션, 마스터 디테일 구조의 디테일 리스트 조회 공통 유틸리티 추가"));
		releaseNotes.add(ValueUtil.newMap("seq,content", 4, "Fixed : 디바이스 업데이트 시 오류 제거"));
		return releaseNotes;
	}

	/**
	 * 장비 타입별로 전달할 메시지를 publish
	 * 
	 * @param deviceType
	 * @param message
	 * @return
	 */
	@RequestMapping(value = "/publish/message/{device_type}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Publish device message")
	public BaseResponse publishDeviceMessage(@PathVariable("device_type") String deviceType, @RequestBody String message) {
		
		this.serviceDispatcher.getDeviceService().sendMessageToDevice(Domain.currentDomainId(), deviceType, message);
		return new BaseResponse(true, LogisConstants.OK_STRING);
	}
	
	/**
	 * 작업 배치의 작업 진행 요약 정보 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/batch_progress_rate/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Batch Progress Rate")
	public BatchProgressRate batchProgressRate(@PathVariable("equip_type") String equipType, @PathVariable("equip_cd") String equipCd) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getJobStatusService(batch).getBatchProgressSummary(batch);
	}
	
	/**
	 * 고객사 코드 및 상품 코드로 상품 조회
	 * 
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/find/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find sku for cliassification")
	public SKU findSkuForClassify(@PathVariable("com_cd") String comCd, @PathVariable("sku_cd") String skuCd) {
		
		long domainId = Domain.currentDomainId();
		return this.serviceDispatcher.getSkuSearchService().findSku(domainId, comCd, skuCd, true);
	}
	
	/**
	 * 분류 처리를 위한 설비 유형, 설비 코드에 실행 중인 작업 배치 내 주문을 상품 코드로 검색해서 상품 리스트 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/search/{equip_type}/{equip_cd}/{station_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search sku for cliassification")
	public List<SKU> searchSkuCandidates(@PathVariable("equip_type") String equipType
										, @PathVariable("equip_cd") String equipCd
										, @PathVariable("station_cd") String stationCd
										, @PathVariable("sku_cd") String skuCd
										, @RequestParam(name="box_id", required=false) String boxId) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		if(ValueUtil.isEmpty(boxId)) {
			return this.serviceDispatcher.getSkuSearchService().searchListInBatch(batch, stationCd, batch.getComCd(), skuCd, true, true);
		} else {
			return this.serviceDispatcher.getSkuSearchService().searchListInBatch(batch, stationCd, batch.getComCd(), skuCd, boxId, true, true);
		}
	}

	/**
	 * 분류 처리를 위한 설비 유형, 설비 코드 및 상품 코드로 like 검색해서 상품 리스트 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/search_by_like/{equip_type}/{equip_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search sku for cliassification")
	public List<SKU> searchSkuCandidates(@PathVariable("equip_type") String equipType
										, @PathVariable("equip_cd") String equipCd
										, @PathVariable("sku_cd") String skuCd) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		return this.serviceDispatcher.getSkuSearchService().searchListInBatch(equipBatchSet.getBatch(), skuCd, true, true);
	}
	
	/**
	 * 배치 그룹 ID 내에서 상품 코드로 like 검색해서 상품 리스트 조회
	 * 
	 * @param batchId
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/search_by_batch/{batch_id}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search SKU List For Middle Classing")
	public List<SKU> searchSkuListByBatch(@PathVariable("batch_id") String batchId, @PathVariable("sku_cd") String skuCd) {
		
		JobBatch batch = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), batchId);
		return this.serviceDispatcher.getSkuSearchService().searchListInBatchGroup(batch, skuCd, true, true);
	}
	
	/**
	 * 배치 그룹 ID 내에서 상품 코드로 like 검색해서 상품 리스트 조회
	 * 
	 * @param batchGroupId
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/search_by_batch_group/{batch_group_id}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search SKU List For Middle Classing")
	public List<SKU> searchSkuListByBatchGroup(@PathVariable("batch_group_id") String batchGroupId, @PathVariable("sku_cd") String skuCd) {
		
		// 배치 그룹 ID에 속한 진행 중인 배치를 조회 ...
		Query condition = AnyOrmUtil.newConditionForExecution(Domain.currentDomainId());
		condition.addFilter("batchGroupId", batchGroupId);
		condition.addFilter("status", JobBatch.STATUS_RUNNING);
		condition.setPageSize(1);
		condition.setPageIndex(1);
		List<JobBatch> batchList = this.queryManager.selectList(JobBatch.class, condition);
		
		if(ValueUtil.isEmpty(batchList)) {
			// 진행 중인 배치가 없습니다.
			throw ThrowUtil.newValidationErrorWithNoLog(true, SysMessageConstants.DOES_NOT_PROCEED, "terms.label.job_batch"); 
		} else {
			JobBatch batch = batchList.get(0);
			return this.serviceDispatcher.getSkuSearchService().searchListInBatchGroup(batch, skuCd, true, true);
		}
	}
	
	/**
	 * 반품용 - 진행 또는 진행 예정인 배치 그룹 ID 내에서 상품 코드로 like 검색해서 상품 리스트 조회
	 * 
	 * @param batchGroupId
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/search_by_batch_group_for_return/{batch_group_id}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search SKU List For Middle Classing")
	public List<SKU> searchSkuListByBatchGroupForReturn(@PathVariable("batch_group_id") String batchGroupId, @PathVariable("sku_cd") String skuCd) {
		
		return this.serviceDispatcher.getSkuSearchService().searchListInBatchGroupForReturn(Domain.currentDomainId(), batchGroupId, skuCd, true);
	}
	
	/**********************************************************************
	 * 								표시기 점/소등 API
	 **********************************************************************/
	
	/**
	 * 설비 소속 모든 표시기 OFF
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/indicators/off/all/{equip_type}/{equip_cd}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Indicators off all of equipment")
	public BaseResponse indicatorsOffAll(@PathVariable("equip_type") String equipType, @PathVariable("equip_cd") String equipCd) {
		// 1. 설비 정보로 부터 작업 배치 조회 
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 작업 배치 전체 표시기 소등
		this.serviceDispatcher.getIndicationService(batch).indicatorOffAll(batch);
		return new BaseResponse(true);
	}
	
	/**
	 * 장비 존 소속 모든 표시기 OFF
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @return
	 */
	@RequestMapping(value = "/indicators/off/{equip_type}/{equip_cd}/{station_cd}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Indicators off all of zone")
	public BaseResponse indicatorsOff(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("station_cd") String stationCd) {
		
		// 1. 작업장 코드 체크
		stationCd = ValueUtil.isEqualIgnoreCase(stationCd, LogisConstants.ALL_CAP_STRING) ? null : stationCd;
		
		// 2. 설비 정보로 부터 작업 배치 조회 
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 작업 배치 내 작업 스테이션 영역의 표시기 소등
		this.serviceDispatcher.getIndicationService(batch).indicatorListOff(batch.getDomainId(), batch.getStageCd(), equipType, equipCd, stationCd);
		return new BaseResponse(true);
	}

	/**
	 * 설비 별로 이전 작업 상태로 표시기 점등 상태 복원
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/indicators/restore/{equip_type}/{equip_cd}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Restore indicators of running job instances")
	public BaseResponse restoreIndicators(@PathVariable("equip_type") String equipType, @PathVariable("equip_cd") String equipCd) {
		// 1. 설비 정보로 부터 작업 배치 조회 
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 작업 배치 내 표시기 진행 중인 작업에 대한 재점등
		this.serviceDispatcher.getIndicationService(batch).restoreIndicatorsOn(batch);
		return new BaseResponse(true);
	}

	/**
	 * 투입 시퀀스, 장비 존 별 처리할 작업 / 처리한 작업 표시기 점등
	 * 
	 * @param jobInputId
	 * @param stationCd
	 * @param todoOrDone
	 * @return
	 */
	@RequestMapping(value = "/indicators/restore/{job_input_id}/{station_cd}/{mode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Indicators off all of station area")
	public BaseResponse restoreIndicators(
			@PathVariable("job_input_id") String jobInputId,
			@PathVariable("station_cd") String stationCd,
			@PathVariable("mode") String todoOrDone) {
		
		// 1. JobInput 조회
		JobInput input = AnyEntityUtil.findEntityById(true, JobInput.class, jobInputId);
		
		// 2. 설비 정보로 부터 작업 배치 조회
		JobBatch batch = LogisServiceUtil.checkRunningBatch(input.getDomainId(), input.getBatchId());
		
		// 3. 작업 배치 내 표시기 진행 중인 작업에 대한 재점등
		this.serviceDispatcher.getIndicationService(batch).restoreIndicatorsOn(batch, input.getInputSeq(), stationCd, todoOrDone);
		return new BaseResponse(true);
	}
	
	/**********************************************************************
	 * 								중분류 API
	 **********************************************************************/
	/**
	 * 스테이지 내에 진행 중인 작업 배치 리스트의 배치 그룹 ID 리스트를 조회 
	 * 
	 * @param jobDate
	 * @param stageCd
	 * @param jobType
	 * @param equipType
	 * @param bizType
	 * @return
	 */
	@RequestMapping(value = "/search_running_batch_groups/{job_date}/{stage_cd}/{job_type}/{equip_type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search running batch list of stage")
	public List<JobBatch> searchRunningBatchGroups(
			@PathVariable("job_date") String jobDate,
			@PathVariable("stage_cd") String stageCd,
			@PathVariable("job_type") String jobType,
			@PathVariable("equip_type") String equipType,
			@RequestParam(name="biz_type", required = false) String bizType) {
		
		String sql = "select distinct job_date, wms_batch_no, batch_group_id from job_batches where domain_id = :domainId and status = :status and job_date = :jobDate and equip_type = :equipType and stage_cd = :stageCd";
		Map<String, Object> params = ValueUtil.newMap("domainId,jobDate,stageCd,jobType,equipType,status", Domain.currentDomainId(), jobDate, stageCd, jobType, equipType, JobBatch.STATUS_RUNNING);
		
		if(ValueUtil.isNotEmpty(bizType)) {
			sql += " and biz_type = :bizType";
			params.put("bizType", bizType);
		}
		
		return this.queryManager.selectListBySql(sql, params, JobBatch.class, 0, 0);
	}
	
	/**
	 * 스테이지 내에 진행 중인 작업 배치 리스트를 조회 
	 * 
	 * @param stageCd
	 * @param jobType
	 * @param equipType
	 * @param bizType
	 * @return
	 */
	@RequestMapping(value = "/search_running_batches/{stage_cd}/{job_type}/{equip_type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search running batch list of stage")
	public List<JobBatch> searchRunningBatches(
			@PathVariable("stage_cd") String stageCd,
			@PathVariable("job_type") String jobType,
			@PathVariable("equip_type") String equipType,
			@RequestParam(name="biz_type", required = false) String bizType) {
		
		String sql = "select * from job_batches where domain_id = :domainId and status = :status and batch_group_id in (select distinct batch_group_id from job_batches where domain_id = :domainId and stage_cd = :stageCd and equip_type = :equipType and status = :status)";
		Map<String, Object> params = ValueUtil.newMap("domainId,stageCd,jobType,equipType,status", Domain.currentDomainId(), stageCd, jobType, equipType, JobBatch.STATUS_RUNNING);
		
		if(ValueUtil.isNotEmpty(bizType)) {
			sql += " and biz_type = :bizType";
			params.put("bizType", bizType);
		}
		
		return this.queryManager.selectListBySql(sql, params, JobBatch.class, 0, 0);
	}
	
	/**
	 * 스테이지 내에 작업 유형, 설비 유형별 진행 중인 메인 배치를 조회
	 * 
	 * @param stageCd
	 * @param jobType
	 * @param equipType
	 * @return
	 */
	@RequestMapping(value = "/find_running_main_batch/{stage_cd}/{job_type}/{equip_type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find running main batch list")
	public List<JobBatch> findMainRunningBatch(
			@PathVariable("stage_cd") String stageCd,
			@PathVariable("job_type") String jobType,
			@PathVariable("equip_type") String equipType,
			@RequestParam(name="biz_type", required = false) String bizType) {
		
		String sql = "select * from job_batches where domain_id = :domainId and stage_cd = :stageCd and job_type = :jobType and equip_type = :equipType and status = :status and id = batch_group_id";
		Map<String, Object> params = ValueUtil.newMap("domainId,stageCd,jobType,equipType,status", Domain.currentDomainId(), stageCd, jobType, equipType, JobBatch.STATUS_RUNNING);
		
		if(ValueUtil.isNotEmpty(bizType)) {
			sql += " and biz_type = :bizType";
			params.put("bizType", bizType);
		}
		
		return this.queryManager.selectListBySql(sql, params, JobBatch.class, 0, 0);
	}

	/**
	 * 출고 중분류 조회
	 * 
	 * @param jobDate
	 * @param stageCd
	 * @param jobType
	 * @param equipType
	 * @param equipCd
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/categorize/{job_date}/{stage_cd}/{job_type}/{equip_type}/{equip_cd}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Categorize by equipType, equipCd, comCd, skuCd")
	public Category categorize(
			@PathVariable("job_date") String jobDate,
			@PathVariable("stage_cd") String stageCd,
			@PathVariable("job_type") String jobType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd,
			@RequestParam(name="assort_mode", required = false) String assortMode) {
		
		Long domainId = Domain.currentDomainId();
		String sql = "select id, job_type from job_batches where domain_id = :domainId and status = :status and batch_group_id in (select distinct batch_group_id from job_batches where domain_id = :domainId and job_date = :jobDate and stage_cd = :stageCd and job_type = :jobType and equip_type = :equipType and equip_cd = :equipCd)";
		Map<String, Object> params = ValueUtil.newMap("domainId,jobDate,stageCd,jobType,equipType,equipCd,status", domainId, jobDate, stageCd, jobType, equipType, equipCd, JobBatch.STATUS_RUNNING);
		List<JobBatch> batchList = this.queryManager.selectListBySql(sql, params, JobBatch.class, 0, 0);
		
		if(batchList.size() < 1) {
			throw ThrowUtil.newValidationErrorWithNoLog(true, "NOT_FOUND_RUNNING_BATCH");
		}
		
		List<String> batchIdList = new ArrayList<String>();
		for(JobBatch batch : batchList) {
			batchIdList.add(batch.getId());
		}
		
		CategorizeEvent event = new CategorizeEvent(domainId, SysEvent.EVENT_STEP_ALONE, stageCd, batchIdList, jobType, comCd, skuCd);
		event.setJobBatch(batchList.get(0));
		event.setAssortMode(assortMode);
		this.eventPublisher.publishEvent(event);
		return (Category)event.getResult();
	}
	
	/**
	 * 출고 중분류 조회
	 * 
	 * @param batchGroupId
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/categorize/for_out/{batch_group_id}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Categorize by equipType, equipCd, comCd, skuCd")
	public Category categorizeForOut(
			@PathVariable("batch_group_id") String batchGroupId,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd,
			@RequestParam(name="assort_mode", required = false) String assortMode) {
		
		Long domainId = Domain.currentDomainId();
		String sql = "select id, stage_cd, job_type from job_batches where domain_id = :domainId and status = :status and batch_group_id = :batchGroupId";
		Map<String, Object> params = ValueUtil.newMap("domainId,batchGroupId,status", domainId, batchGroupId, JobBatch.STATUS_RUNNING);
		List<JobBatch> batchList = this.queryManager.selectListBySql(sql, params, JobBatch.class, 0, 0);
		
		if(batchList.size() < 1) {
			throw ThrowUtil.newValidationErrorWithNoLog(true, "NOT_FOUND_RUNNING_BATCH");
		}
		
		List<String> batchIdList = new ArrayList<String>();
		for(JobBatch batch : batchList) {
			batchIdList.add(batch.getId());
		}
		
		JobBatch firstBatch = batchList.get(0);
		CategorizeEvent event = new CategorizeEvent(domainId, SysEvent.EVENT_STEP_ALONE, firstBatch.getStageCd(), batchIdList, firstBatch.getJobType(), comCd, skuCd);
		event.setJobBatch(firstBatch);
		event.setAssortMode(assortMode);
		this.eventPublisher.publishEvent(event);
		return (Category)event.getResult();
	}
	
	/**
	 * 반품 중분류 조회
	 * 
	 * @param batchGroupId
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/categorize/for_return/{batch_group_id}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Categorize by equipType, equipCd, comCd, skuCd")
	public List<Map> categorizeForReturn(
			@PathVariable("batch_group_id") String batchGroupId,
			@PathVariable("com_cd") String comCd,
            @PathVariable("sku_cd") String skuCd,
            @RequestParam(name="mode", required = false) String mode) {
		
		// 중분류 조회
		Map<String, Object> params = ValueUtil.newMap("domainId,batchGroupId,comCd,skuCd", Domain.currentDomainId(), batchGroupId, comCd, skuCd);
		
        StringJoiner buffer = new StringJoiner(SysConstants.LINE_SEPARATOR)
        .add(" with v_batch as (")
        .add(" 	select domain_id, id as batch_id")
        .add(" 	     , area_cd, stage_cd")
        .add(" 	     , equip_type, equip_cd")
        .add(" 	     , job_seq")
        .add("  	  from job_batches ")
        .add("  	 where domain_id = :domainId")
        .add(" 	   and status in ('WAIT', 'READY', 'RUN') ")
        .add(" 	   and batch_group_id = :batchGroupId")
        .add(" 	   and equip_cd is not null")
        .add(" ), ")
        .add(" v_equip as (")
        .add(" 	select rack.rack_cd, rack.rack_nm ")
        .add(" 	     , station.station_cd, station.station_nm ")
        .add(" 	     , cell.cell_cd")
        .add(" 	  from racks as rack")
        .add("      inner join stations as station")
        .add(" 	         on rack.domain_id = station.domain_id")
        .add(" 	        and rack.rack_cd = station.equip_cd")
        .add("  	        and station.equip_type = 'Rack'")
        .add(" 	 inner join cells as cell")
        .add(" 	         on station.domain_id = cell.domain_id")
        .add(" 	        and station.equip_type = cell.equip_type")
        .add(" 	        and station.equip_cd = cell.equip_cd")
        .add(" 	        and station.station_cd = cell.station_cd")
        .add(" 	 where (rack.domain_id, rack.rack_cd) in (select domain_id, equip_cd from v_batch)")
        .add(" ),")
        .add(" v_ord as (")
        .add(" 	select pre.batch_id, batch.job_seq")
        .add(" 	     , pre.equip_cd, pre.sub_equip_cd")
        .add(" 	  from order_preprocesses as pre")
        .add(" 	 inner join v_batch as batch")
        .add(" 	         on pre.batch_id = batch.batch_id")
        .add(" 	 where pre.com_cd = :comCd")
        .add(" 	   and pre.cell_assgn_cd = :skuCd")
        .add(" )")
        .add(" select ord.job_seq,")
        .add(ValueUtil.isEqual(mode, "rack") ? "equip.rack_nm as equip_nm" : "equip.station_nm as equip_nm")		
        .add("   from v_ord as ord")
        .add("  inner join v_equip equip")
        .add("          on ord.equip_cd = equip.rack_cd")
        .add(" 		and ord.sub_equip_cd = equip.cell_cd")
        .add(" group by ord.job_seq,")
        .add(ValueUtil.isEqual(mode, "rack") ? " rack_nm" : " station_nm");		
		
		List<Map> results = this.queryManager.selectListBySql(buffer.toString(), params, Map.class, 0, 0);
		
		// 존재하지 않으면 에러
		if(ValueUtil.isEmpty(results)) {
			throw ThrowUtil.newValidationErrorWithNoLog("스캔한 상품은 선택한 대상 배치에 존재하지 않습니다.");
		// 존재하면 리턴
		} else {
			return results;
		}
	}
	
	/**********************************************************************
	 * 								박스 매핑 API
	 **********************************************************************/
	/**
	 * 셀과 박스 ID 매핑 현황 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @return
	 */
	@RequestMapping(value = "/search/cell_mappings/{equip_type}/{equip_cd}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Cell & Box Mapping")
	public List<WorkCell> searchCellMappings(
			@PathVariable("equip_type") String equipType, 
			@PathVariable("equip_cd") String equipCd,
			@RequestParam(name="station_cd", required = false) String stationCd) {
		
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		String sql = "select * from work_cells where domain_id = :domainId and batch_id = :batchId and cell_cd in (select cell_cd from cells where domain_id = :domainId and equip_cd = :equipCd and active_flag = :active #if($stationCd) and station_cd = :stationCd #end) order by cell_cd";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,equipCd,active", domainId, batch.getId(), equipCd, true);
		if(ValueUtil.isNotEmpty(stationCd) && !ValueUtil.isEqualIgnoreCase(stationCd, LogisConstants.ALL_CAP_STRING)) {
			condition.put("stationCd", stationCd);
		}
		return this.queryManager.selectListBySql(sql, condition, WorkCell.class, 0, 0);
	}
	
	/**
	 * 셀과 분류 코드 매핑
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param subEquipCd
	 * @param classCd
	 * @return
	 */
	@RequestMapping(value = "/assign/cell_class/{equip_type}/{equip_cd}/{sub_equip_cd}/{class_cd}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cell & Box Mapping")
	public Object cellClassCdMapping(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("sub_equip_cd") String subEquipCd,
			@PathVariable("class_cd") String classCd) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getClassificationService(batch).classCellMapping(batch, subEquipCd, classCd);
	}
	
	/**
	 * 셀과 박스 ID 매핑 (셀 코드 사용)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param subEquipCd
	 * @param boxId
	 * @param isSkipEquipCheck 셀이 equipCd에 소속되었는지 체크할 지 여부
	 * @param isSkipBoxMapping 박스 매핑을 스킵할 지 여부
	 * @return
	 */
	@RequestMapping(value = "/assign/cell_box/{equip_type}/{equip_cd}/{sub_equip_cd}/{box_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cell & Box Mapping")
	public Object boxMappingByCellCd(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("sub_equip_cd") String subEquipCd,
			@PathVariable("box_id") String boxId,
			@RequestParam(name="skip_equip_check", required = false) boolean isSkipEquipCheck,
			@RequestParam(name="skip_box_mapping", required = false) boolean isSkipBoxMapping) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getClassificationService(batch).boxCellMapping(batch, subEquipCd, boxId);
	}
	
	/**
	 * 셀과 박스 ID 매핑 (표시기 코드 사용)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param indCd
	 * @param boxId
	 * @param isSkipEquipCheck 셀이 equipCd에 소속되었는지 체크할 지 여부
	 * @param isSkipBoxMapping 박스 매핑을 스킵할 지 여부
	 * @return
	 */
	@RequestMapping(value = "/assign/ind_box/{equip_type}/{equip_cd}/{ind_cd}/{box_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cell & Box Mapping")
	public Object boxMappingByIndCd(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("ind_cd") String indCd, 
			@PathVariable("box_id") String boxId,
			@RequestParam(name="skip_equip_check", required = false) boolean isSkipEquipCheck,
			@RequestParam(name="skip_box_mapping", required = false) boolean isSkipBoxMapping) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		String sql = "select cell_cd from cells where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd and ind_cd = :indCd";
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,indCd", batch.getDomainId(), equipType, equipCd, indCd);
		String subEquipCd = this.queryManager.selectBySql(sql, params, String.class);
		return this.serviceDispatcher.getBoxingService(batch).assignBoxToCell(batch, subEquipCd, boxId);
	}
	
	/**
	 * 셀과 박스 ID 매핑 해제
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param subEquipCd
	 * @return
	 */
	@RequestMapping(value = "/cancel/cell_box/{equip_type}/{equip_cd}/{sub_equip_cd}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cancel Cell & Box Mapping")
	public Object cancelBoxMapping(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("sub_equip_cd") String subEquipCd) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getBoxingService(batch).resetBoxToCell(batch, subEquipCd);
	}
	
	/**********************************************************************
	 * 								소분류 처리 API
	 **********************************************************************/
	
	/**
	 * 작업 처리 ID (jobInstanceId)로 소분류 작업 처리
	 * 
	 * @param deviceType 장비 유형 (indicator, pda, tablet, kiosk)
	 * @param equipType 설비 유형 (Rack, Sorter ...)
	 * @param equipCd 설비 코드
	 * @param jobInstanceId 작업 인스턴스 ID
	 * @param pickingQty 피킹 수량
	 * @param pickWithInspection 피킹과 동시에 검수 처리할 지 여부
	 * @param serial 시리얼 번호
	 * @param checkSerialUnique 시리얼 번호가 유니크한 지 체크 여부 (장비 설정 : pick.serial.check.unique)
	 * @return
	 */
	@RequestMapping(value = "/classify/confirm/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Confirm classification")
	public BaseResponse confirmClassification(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId,
			@RequestParam(name="picking_qty", required = false) Integer pickingQty,
			@RequestParam(name="pick_with_inspection", required = false) Boolean pickWithInspection,
			@RequestParam(name="serial", required = false) String serial,
			@RequestParam(name="check_serial_unique", required = false) Boolean checkSerialUnique) {
		
		// 1. 설비 정보로 부터 작업 배치 조회 
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(batch.getDomainId(), jobInstanceId);

		// 3. 시리얼 번호가 있는 경우 이미 등록된 시리얼인지 체크
		if(ValueUtil.isNotEmpty(serial) && checkSerialUnique != null && checkSerialUnique == true) {
			if(this.queryManager.selectSize(SerialInstance.class, ValueUtil.newMap("domainId,comCd,skuCd,serialNo", batch.getDomainId(), job.getComCd(), job.getSkuCd(), serial)) > 0) {
				throw ThrowUtil.newAlreadyProcessedRequest("serial");
			}
		}
		
		// 4. JobInstance 확인
		if(pickingQty != null && pickingQty == 1 && ValueUtil.isNotEmpty(serial)) {
			job.setSerialNo(serial);
		}
		
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 5. 소분류 이벤트 생성
		int resQty = (pickWithInspection != null && pickWithInspection) ? 1 : (pickingQty == null) ? job.getPickQty() : pickingQty;
		ClassifyRunEvent event = new ClassifyRunEvent(batch
				, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase()
				, LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM
				, job
				, job.getPickQty()
				, resQty);
		event.setPickWithInspection(pickWithInspection == null ? false : pickWithInspection);
		
		// 6. 이벤트 발생 
		this.eventPublisher.publishEvent(event);
		
		// 7. 이벤트 처리 결과 리턴 
		if(!event.isExecuted()) {
			throw new ElidomServiceException("분류 작업 처리 이벤트를 받아서 처리한 모듈이 없습니다.");
		} else {
			return new BaseResponse(true,null, event.getResult());
		}
	}

	/**
	 * 작업 ID (jobInstanceId)로 소분류 작업 분할 처리
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @param reqQty
	 * @param resQty
	 * @return
	 */
	@RequestMapping(value = "/classify/split/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Confirm classification")
	public BaseResponse splitClassification(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId, 
			@RequestParam(name = "req_qty", required = true) Integer reqQty,
			@RequestParam(name = "res_qty", required = true) Integer resQty) {

		// 1. Equip 으로 Batch조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회 
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(domainId, jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성 
		ClassifyRunEvent event = new ClassifyRunEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase()
				, LogisCodeConstants.CLASSIFICATION_ACTION_MODIFY, job
				, ValueUtil.isEmpty(reqQty) ? job.getPickQty() : reqQty
				, ValueUtil.isEmpty(resQty) ? 1 : resQty);
		
		// 4. 이벤트 발생
		this.eventPublisher.publishEvent(event);
		
		// 5. 이벤트 처리 결과 리턴
		if(!event.isExecuted()) {
			throw new ElidomServiceException();
		} else {
			return new BaseResponse(true, null, event.getResult());
		}
	}
	
	/**
	 * 작업 ID (jobInstanceId)로 소분류 작업 취소 처리
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @return
	 */
	@RequestMapping(value = "/classify/cancel/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cancel classification")
	public BaseResponse cancelClassification(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId) {

		// 1. Equip 으로 Batch조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(batch.getDomainId(), jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성
		ClassifyRunEvent event = new ClassifyRunEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase(), LogisCodeConstants.CLASSIFICATION_ACTION_CANCEL, job);
		
		// 4. 이벤트 발생
		this.eventPublisher.publishEvent(event);
		
		// 5. 이벤트 처리 결과 리턴
		if(!event.isExecuted()) {
			throw new ElidomServiceException();
		} else {
			return new BaseResponse(true,null, event.getResult());
		}
	}
	
	/**
	 * 소분류 확정 취소
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @return
	 */
	@RequestMapping(value = "/classify/undo/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Undo classification")
	public BaseResponse undoClassification(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId) {
		
		// 1. 설비 정보로 Batch조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(batch.getDomainId(), jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성
		ClassifyRunEvent event = new ClassifyRunEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase(), LogisCodeConstants.CLASSIFICATION_ACTION_UNDO_PICK, job);
		
		// 4. 액션 실행
		this.serviceDispatcher.getClassificationService(batch).classify(event);
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	/**
	 * 풀 박스
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @param reqQty
	 * @param boxId
	 * @return
	 */
	@RequestMapping(value = "/fullbox/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Fullbox")
	public BaseResponse fullboxing(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId,
			@RequestParam(name = "req_qty", required = false) Integer reqQty,
			@RequestParam(name = "box_id", required = false) String boxId) {
		
		// 1. 설비 정보로 Batch조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(domainId, jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성
		ClassifyOutEvent event = new ClassifyOutEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase()
				, LogisCodeConstants.CLASSIFICATION_ACTION_FULL, job
				, ValueUtil.isEmpty(reqQty) ? 0 : reqQty
				, 1);
		
		// 4. 박스 ID가 존재하면
		WorkCell workCell = event.getWorkCell();
		if(ValueUtil.isNotEmpty(boxId) && ValueUtil.isEmpty(workCell.getBoxId())) {
			workCell.setBoxId(boxId);
			event.setBoxId(boxId);
		}
		
		// 5. 액션 실행
		this.serviceDispatcher.getClassificationService(batch).classify(event);
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	/**
	 * 일괄 풀 박스
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/fullbox_all/{equip_type}/{equip_cd}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Batch fullbox")
	public BaseResponse batchFullbox(@PathVariable("equip_type") String equipType,  @PathVariable("equip_cd") String equipCd) {
		
		// 1. 설비 정보로 작업 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 배치 풀 박스 처리
		this.serviceDispatcher.getAssortService(batch).getBoxingService().batchBoxing(batch);
		
		// 3. 응답
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	/**
	 * 풀 박스 취소
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param cellCd
	 * @param boxId
	 * @return
	 */
	@RequestMapping(value = "/fullbox/undo/{device_type}/{equip_type}/{equip_cd}/{cell_cd}/{box_id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Undo fullbox")
	public BaseResponse undoFullboxing(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("cell_cd") String cellCd, 
			@PathVariable("box_id") String boxId) {
		
		// 1. 설비 정보로 작업 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 박스 조회
		BoxPack box = AnyEntityUtil.findEntityById(false, BoxPack.class, boxId);
		
		if(box == null) {
			box = AnyEntityUtil.findEntityBy(batch.getDomainId(), true, true, BoxPack.class, null, "domainId,batchId,boxId", batch.getDomainId(), batch.getId(), boxId);
			this.serviceDispatcher.getAssortService(batch).cancelBoxing(batch.getDomainId(), box);
		}
		
		// 3. 액션 실행
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	@RequestMapping(value = "/finish_order/forcibly/{device_type}/{equip_type}/{equip_cd}/{class_cd}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Finish forcibly all job instances that not finished of order")
	public BaseResponse finishOrderForcibly(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("class_cd") String classCd,
			@RequestParam(name = "station_cd", required = false) String stationCd) {
		
		// 1. 설비 정보로 작업 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 주문에 작업 처리되지 않은 잔여 작업 모두 완료 처리
		IAssortService assortSvc = this.serviceDispatcher.getAssortService(batch);
		List<JobInstance> jobList = this.serviceDispatcher.getJobStatusService(batch).searchPickingJobList(batch, stationCd, classCd);
		for(JobInstance job : jobList) {
			if(LogisConstants.JOB_STATUS_WIP.contains(job.getStatus())) {
				// 소분류 이벤트 생성 
				ClassifyRunEvent event = new ClassifyRunEvent(batch, SysEvent.EVENT_STEP_ALONE
						, deviceType.toLowerCase()
						, LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM, job, job.getPickQty(), job.getPickQty());
				// 이벤트 처리
				assortSvc.confirmAssort(event);
			}
		}
		
		// 3. 액션 실행
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	/**********************************************************************
	 * 								작업 데이터 조회 API
	 **********************************************************************/
	
	/**
	 * 호기 범위 내 혹은 작업 존 범위 내 상태별 투입 리스트를 조회 (페이지네이션)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param status 상태 - 빈 값: 전체 보기, U: 미완료인 것만 보기
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/search/input_pages/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Input list")
	public Page<JobInput> searchInputPages(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@RequestParam(name = "station_cd", required = false) String stationCd,
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getJobStatusService(batch).paginateInputList(batch, equipCd, stationCd, status, page, limit);
	}
	
	/**
	 * 호기 범위 내 혹은 작업 존 범위 내 상태별 미 투입 리스트를 조회 (페이지네이션)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param status 상태 - 빈 값: 전체 보기, U: 미완료인 것만 보기
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/search/not_input_pages/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Not Input list")
	public Page<JobInput> searchNotInputPages(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@RequestParam(name = "station_cd", required = false) String stationCd,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getJobStatusService(batch).paginateNotInputList(batch, equipCd, stationCd, page, limit);
	}
	
	/**
	 * 태블릿 피킹 화면 하단 작업 스테이션 별 투입 리스트 조회 (리스트)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param selectedInputId
	 * @return
	 */
	@RequestMapping(value = "/search/input_list/{equip_type}/{equip_cd}/{station_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Input list")
	public List<JobInput> searchInputList(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("station_cd") String stationCd,
			@RequestParam(name = "selected_input_id", required = false) String selectedInputId) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getJobStatusService(batch).searchInputList(batch, equipCd, stationCd, selectedInputId);
	}
	
	/**
	 * 작업 배치내 작업 중인 투입 정보의 작업 리스트를 조회
	 * 
	 * @param jobInputId
	 * @param equipType
	 * @param equipCd
	 * @param comCd
	 * @param skuCd
	 * @param stationCd
	 * @param indOnYn
	 * @return
	 */
	@RequestMapping(value = "/search/input_jobs/{job_input_id}/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Input Job list")
	public List<JobInstance> searchInputJobs(
			@PathVariable("job_input_id") String jobInputId,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@RequestParam(name = "com_cd", required = false) String comCd,
			@RequestParam(name = "sku_cd", required = false) String skuCd,
			@RequestParam(name = "station_cd", required = false) String stationCd,
			@RequestParam(name = "ind_on_yn", required = false) String indOnYn) {
		
		// 1. 작업 배치 체크 및 조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		List<JobInstance> jobList = null;
		
		// 2. JobInput 조회
		JobInput input = AnyEntityUtil.findEntityBy(domainId, false, JobInput.class, null, "id", jobInputId);
		
		// 3. 서비스 호출
		if(input == null) {
			input = new JobInput();
			input.setComCd(comCd);
			input.setSkuCd(skuCd);
		}
		
		// 4. 작업 리스트
		jobList = this.serviceDispatcher.getJobStatusService(batch).searchInputJobList(batch, input, stationCd);
		boolean indOnFlag = ValueUtil.isEqualIgnoreCase(indOnYn, LogisConstants.Y_CAP_STRING);
		
		// 5. 표시기 점등
		if(indOnFlag) {
			// 해당 스테이션에 존재하는 모든 피킹 중인 작업 리스트를 조회하여 소등
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			indSvc.indicatorListOff(batch, stationCd);
			
			// 작업 리스트 중에 피킹 상태인 작업만 점등
			List<JobInstance> jobsToIndOn = jobList.stream().filter(job -> ValueUtil.isEqualIgnoreCase(job.getStatus(), LogisConstants.JOB_STATUS_PICKING)).collect(Collectors.toList());
			indSvc.indicatorsOn(batch, false, jobsToIndOn);
		}
		
		// 6. 작업 리스트 리턴
		return jobList;
	}
	
	/**
	 * 작업 배치내 작업 중인 투입 정보의 작업 리스트를 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param comCd
	 * @param skuCd
	 * @param stationCd
	 * @param indOnYn
	 * @return
	 */
	@RequestMapping(value = "/search/input_jobs/{equip_type}/{equip_cd}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Input Job list")
	public List<JobInstance> searchInputJobs(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd,
			@RequestParam(name = "station_cd", required = false) String stationCd,
			@RequestParam(name = "ind_on_yn", required = false) String indOnYn) {
		
		// 1. 작업 배치 체크 및 조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		List<JobInstance> jobList = null;
		
		// 3. 서비스 호출
		Map<String, Object> condition = ValueUtil.newMap("comCd,skuCd,stationCd", comCd, skuCd, stationCd);
		jobList = this.serviceDispatcher.getJobStatusService(batch).searchInputJobList(batch, condition);
		boolean indOnFlag = ValueUtil.isEqualIgnoreCase(indOnYn, LogisConstants.Y_CAP_STRING);
		
		// 4. 표시기 점등
		if(indOnFlag) {
			// 해당 스테이션에 존재하는 모든 피킹 중인 작업 리스트를 조회하여 소등
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			indSvc.indicatorListOff(batch, stationCd);
			
			// 작업 리스트 중에 피킹 상태인 작업만 점등
			List<JobInstance> jobsToIndOn = jobList.stream().filter(job -> ValueUtil.isEqualIgnoreCase(job.getStatus(), LogisConstants.JOB_STATUS_PICKING)).collect(Collectors.toList());
			indSvc.indicatorsOn(batch, false, jobsToIndOn);
		}
		
		// 5. 작업 리스트 리턴
		return jobList;
	}
	
	/**
	 * 작업 배치내 셀 별 피킹 현황 조회
	 * 
	 * @param equipType 설비 타입
	 * @param equipCd 설비 코드
	 * @param stationCd 작업 스테이션 코드
	 * @param cellCd 셀 코드
	 * @param workCellOnly 작업이 진행 중인 셀만 표시할 지 여부
	 * @param pickingCellOnly 현재 표시기 점등된 셀만 표시할 지 여부
	 * @return
	 */
	@RequestMapping(value = "/search/picking_status/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Picking status")
	public List<JobInstance> searchPickingStatus(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@RequestParam(name = "station_cd", required = false) String stationCd,
			@RequestParam(name = "cell_cd", required = false) String cellCd,
			@RequestParam(name = "work_cell_only", required = false) Boolean workCellOnly,
			@RequestParam(name = "picking_cell_only", required = false) Boolean pickingCellOnly) {
		
		// 1. 작업 배치 체크 및 조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 서비스 호출
		workCellOnly = workCellOnly != null ? workCellOnly : false;
		pickingCellOnly = pickingCellOnly != null ? pickingCellOnly : false;
		return this.serviceDispatcher.getJobStatusService(batch).searchJobStatusByCell(batch, stationCd, cellCd, workCellOnly, pickingCellOnly);
	}
	
	/**
	 * 작업 배치내 셀 별 피킹 상세 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param classCd
	 * @return
	 */
	@RequestMapping(value = "/picking_status/details/{equip_type}/{equip_cd}/{class_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Picking status")
	public List<JobInstance> searchPickingStatusDetail(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("class_cd") String classCd) {
		
		// 1. 작업 배치 체크 및 조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 서비스 호출
		String sql = "select sku_cd, max(sku_nm) as sku_nm, sum(pick_qty) as pick_qty, sum(picked_qty) as picked_qty from job_instances where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd group by class_cd, sku_cd";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,classCd", domainId, batch.getId(), classCd);
		return this.queryManager.selectListBySql(sql, condition, JobInstance.class, 0, 0);
	}
	
	/**********************************************************************
	 * 								상품 / 박스 투입 API
	 **********************************************************************/
	
	/**
	 * 상품 코드 스캔으로 상품 투입
	 * 
	 * @param equipType 설비 유형
	 * @param equipCd 설비 코드
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드
	 * @param page 현재 페이지
	 * @param limit 페이지 당 레코드 수
	 * @param status
	 * @return
	 */
	@RequestMapping(value = "/input/sku/{equip_type}/{equip_cd}/{com_cd}/{sku_cd}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Input SKU")
	public Object inputSKU(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "status", required = false) String status) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		if(ValueUtil.isEmpty(batch.getEquipType())) {
			batch.setEquipType(equipType);
		}		
		if(ValueUtil.isEmpty(batch.getEquipCd())) {
			batch.setEquipCd(equipCd);
		}
		IClassifyInEvent inputEvent = new ClassifyInEvent(batch, SysEvent.EVENT_STEP_ALONE, false, LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU, skuCd, 1);
		inputEvent.setComCd(comCd);
		return this.serviceDispatcher.getClassificationService(equipBatchSet.getBatch()).input(inputEvent);
	}
	
	/**
	 * 박스 코드 스캔으로 박스 투입
	 * 
	 * @param equipType 설비 유형
	 * @param equipCd 설비 코드
	 * @param boxId 박스 ID
	 * @return
	 */
	@RequestMapping(value = "/input/box/{equip_type}/{equip_cd}/{box_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Input Box")
	public Object inputBox(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("box_id") String boxId) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		IClassifyInEvent inputEvent = new ClassifyInEvent(equipBatchSet.getBatch(),
				SysEvent.EVENT_STEP_ALONE, false, LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_BOX, boxId, 1);
		return this.serviceDispatcher.getClassificationService(equipBatchSet.getBatch()).input(inputEvent);
	}
	
	/**********************************************************************
	 * 								박스 관련 API
	 **********************************************************************/
	
	/**
	 * 설비에서 분류 처리된 박스 리스트 조회 (페이지네이션)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param equipZone
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/paginate/box_list/{equip_type}/{equip_cd}/{equip_zone}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Paginate box list")
	public Page<BoxPack> paginateBoxList(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("equip_zone") String equipZone,
			@RequestParam(name = "cell_cd", required = false) String cellCd,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit) {
		
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		
		// 1. 조회 조건 설정 
		Query condition = AnyOrmUtil.newConditionForExecution(domainId, page, limit);
		condition.addFilter("batchId", equipBatchSet.getBatch().getId());
		
		// 2. cellCd 파라미터가 있으면 
		if(ValueUtil.isNotEmpty(cellCd)) {
			// 2.1 조회 조건에 추가 
			condition.addFilter("subEquipCd", cellCd);
		}
		
		// 3. 조회 후 리턴  
		return this.queryManager.selectPage(BoxPack.class, condition);
	}
	
	/**
	 * 설비에서 분류 처리된 박스 리스트 조회 (페이지네이션)
	 * 
	 * @param batchId
	 * @param equipZone
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/paginate/box_list/{batch_id}/{equip_zone}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Paginate box list")
	public Page<BoxPack> paginateBoxList(
			@PathVariable("batch_id") String batchId,
			@PathVariable("equip_zone") String equipZone,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit) {
		
		Long domainId = Domain.currentDomainId();
		Query condition = AnyOrmUtil.newConditionForExecution(domainId, page, limit);
		condition.addFilter("batchId", batchId);
		return this.queryManager.selectPage(BoxPack.class, condition);
	}
	
	/**
	 * 설비에서 분류 처리된 박스 리스트 조회 (리스트)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param equipZone
	 * @return
	 */
	@RequestMapping(value = "/search/box_list/{equip_type}/{equip_cd}/{equip_zone}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search box list")
	public List<BoxPack> searchBoxList(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("equip_zone") String equipZone) {
		
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", equipBatchSet.getBatch().getId());
		return this.queryManager.selectList(BoxPack.class, condition);
	}
	
	/**
	 * 설비에서 분류 처리된 박스 리스트 조회 (페이지네이션)
	 * 
	 * @param batchId
	 * @param equipZone
	 * @return
	 */
	@RequestMapping(value = "/search/box_list/{batch_id}/{equip_zone}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search box list")
	public List<BoxPack> searchBoxList(
			@PathVariable("batch_id") String batchId,
			@PathVariable("equip_zone") String equipZone) {
		
		Long domainId = Domain.currentDomainId();
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", batchId);
		return this.queryManager.selectList(BoxPack.class, condition);
	}
	
	/**
	 * 박스 처리 ID로 박스 내품 내역 리스트 조회
	 * 
	 * @param boxPackId
	 * @return
	 */
	@RequestMapping(value = "/search/box_items/{box_pack_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search box items")
	public List<BoxItem> searchBoxItems(@PathVariable("box_pack_id") String boxPackId) {
		
		BoxPack boxPack = this.queryManager.select(BoxPack.class, boxPackId);
		if(boxPack != null) {
			boxPack.searchBoxItems();
		}
		
		return boxPack == null ? null : boxPack.getItems();
	}

	/**
	 * 박스 라벨 재발행
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param boxPackId
	 * @param printerId
	 * @return
	 */
	@RequestMapping(value = "/reprint/box_label/{equip_type}/{equip_cd}/{box_pack_id}/{printer_id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Reprint Box Label")
	public BaseResponse reprintBoxLabel(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("box_pack_id") String boxPackId,
			@PathVariable("printer_id") String printerId) {
		
		if(ValueUtil.isNotEmpty(printerId) && ValueUtil.isEqualIgnoreCase(printerId, "NA")) {
			printerId = null;
		}
		
		BoxPack boxPack = this.queryManager.select(BoxPack.class, boxPackId);
		
		if(boxPack != null) {
			JobBatch batch = AnyEntityUtil.findEntityById(true, JobBatch.class, boxPack.getBatchId());
			String labelTemplate = BatchJobConfigUtil.getInvoiceLabelTemplate(batch);
			PrintEvent printEvent = new PrintEvent(boxPack.getDomainId(), boxPack.getJobType(), printerId, labelTemplate, ValueUtil.newMap("batch,box", batch, boxPack));
			printEvent.setPrintType("barcode");
			printEvent.findEmptyPrintInfo();
			this.eventPublisher.publishEvent(printEvent);
			return new BaseResponse(true, ValueUtil.toString(printEvent.getResult()));
		} else {
			return new BaseResponse(false, "Not Found Box By Id [" + boxPackId + "]");
		}
	}
	
	/**
	 * 작업 인스턴스 ID로 부터 박스 조회 후 박스 라벨 재발행
	 * 
	 * @param jobInstanceId
	 * @return
	 */
	@RequestMapping(value = "/reprint/box_label", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Reprint Box Label")
	public BaseResponse reprintBoxLabel(
		@RequestParam(name = "stage_cd", required = true) String stageCd, 
		@RequestParam(name = "equip_type", required = true) String equipType,
		@RequestParam(name = "equip_cd", required = true) String equipCd,
		@RequestParam(name = "sub_equip_cd", required = true) String subEquipCd,
		@RequestParam(name = "job_type", required = true) String jobType,
		@RequestParam(name = "printer_id", required = false) String printerId) {
		
		// 1. 설비에 진행 중인 작업 배치 조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 작업 배치 소속의 subEquipCd에서 가장 마지막으로 주문 완료 처리된 박스 조회
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", batch.getId());
		condition.addFilter("stageCd", batch.getStageCd());
		condition.addFilter("jobType", batch.getJobType());
		condition.addFilter("subEquipCd", subEquipCd);
		condition.addFilter("status", "in", LogisConstants.JOB_STATUS_BER);
		condition.addOrder("boxedAt", false);
		condition.setPageSize(1);
		condition.setPageIndex(1);
		condition.setMaxResultSize(1);
		List<BoxPack> boxList = this.queryManager.selectList(BoxPack.class, condition);
		
		// 3. 박스 정보가 없다면 에러
		if(ValueUtil.isEmpty(boxList)) {
			throw ThrowUtil.newValidationErrorWithNoLog("셀 [" + subEquipCd + "]에서 완료된 주문이 없습니다.");
		}
		
		// 4. 박스 정보 추출
		BoxPack box = boxList.get(0);
		Map<String, Object> params = ValueUtil.newMap("domainId,box,batch", domainId, box, batch);
		
		// 5. 프린트 이벤트 생성
		PrintEvent printEvent = new PrintEvent(box.getDomainId(), box.getJobType(), null, null, params);
		printEvent.setPrintType("barcode");
		printEvent.findEmptyPrintInfo();
		
		// 6. 프린트 이벤트 Publish
		this.eventPublisher.publishEvent(printEvent);
		return new BaseResponse(true, ValueUtil.toString(printEvent.getResult()));
	}

	/**********************************************************************
	 * 								Dynamic API
	 **********************************************************************/

	/**
	 * 디바이스 관련 각 모듈에 특화된 REST GET 서비스
	 * DeviceProcessRestEvent 이벤트를 발생시켜 각 모듈에서 해당 로직 처리
	 */
	@RequestMapping(value = "/dynamic/{job_type}/**", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Device Process Rest GET API")
	public BaseResponse deviceProcessRestGetApi(
			final HttpServletRequest request
			, @PathVariable("job_type") String jobType
			, @RequestParam Map<String,Object> paramMap) {
		
		String finalPath = this.getRequestFinalPath(request);
		DeviceProcessRestEvent event = new DeviceProcessRestEvent(Domain.currentDomainId(), jobType, finalPath, RequestMethod.GET, paramMap);
		return this.restEventPublisher(event);
	}

	/**
	 * 디바이스 관련 각 모듈에 특화된 REST PUT 서비스
	 * DeviceProcessRestEvent 이벤트를 발생시켜 각 모듈에서 해당 로직 처리
	 */
	@RequestMapping(value = "/dynamic/{job_type}/**", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Device Process Rest PUT API")
	public BaseResponse deviceProcessRestPutApi(
			final HttpServletRequest request
			, @PathVariable("job_type") String jobType
			, @RequestParam Map<String,Object> paramMap
			, @RequestBody(required = false) Map<String,Object> requestBody) {
		
		String finalPath = this.getRequestFinalPath(request);
		DeviceProcessRestEvent event = new DeviceProcessRestEvent(Domain.currentDomainId(), jobType, finalPath, RequestMethod.PUT, paramMap);
		event.setRequestPutBody(requestBody);
		return this.restEventPublisher(event);
	}

	/**
	 * 디바이스 관련 각 모듈에 특화된 REST POST 서비스
	 * DeviceProcessRestEvent 이벤트를 발생시켜 각 모듈에서 해당 로직 처리
	 */
	@RequestMapping(value = "/dynamic/{job_type}/**", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Device Process Rest POST API")
	public BaseResponse deviceProcessRestPostApi(
			final HttpServletRequest request
			, @PathVariable("job_type") String jobType
			, @RequestParam Map<String,Object> paramMap
			, @RequestBody(required = false) List<Map<String,Object>> requestBody) {
		
		String finalPath = this.getRequestFinalPath(request);
		DeviceProcessRestEvent event = new DeviceProcessRestEvent(Domain.currentDomainId(), jobType, finalPath, RequestMethod.POST, paramMap);
		event.setRequestPostBody(requestBody);
		return this.restEventPublisher(event);
	}
	
	/**
	 * 바코드 라벨 혹은 PDF 인쇄 처리 시 부가 정보가 필요한 경우 처리
	 * 
	 * @param printInfoEvent
	 */
	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, classes = FindPrintInfoEvent.class)
	public void findPrintInfo(FindPrintInfoEvent printInfoEvent) {
		
		PrintEvent pe = printInfoEvent.getPrintEvent();
		Map<String, Object> templateParams = pe.getTemplateParams();
		String printerId = pe.getPrinterId();
		String printTemplate = pe.getPrintTemplate();
		String printType = pe.getPrintType();
		
		BoxPack box = (BoxPack)templateParams.get("box");
		JobBatch batch = (JobBatch)templateParams.get("batch");
		
		// 인쇄 템플릿이 없다면 조회
		if(ValueUtil.isEmpty(printTemplate)) {
			printTemplate = ValueUtil.isEqualIgnoreCase(printType, "barcode") ? 
					BatchJobConfigUtil.getInvoiceLabelTemplate(batch) : BatchJobConfigUtil.getTradeStatmentTemplate(batch);
			pe.setPrintTemplate(printTemplate);
		}
		
		// 프린터 ID가 없다면 조회
		if(ValueUtil.isEmpty(printerId)) {
			Map<String, Object> params = ValueUtil.newMap("domainId,stageCd,cellCd", pe.getDomainId(), batch.getStageCd(), box.getSubEquipCd());
			
			// 5.1 셀 정보에서 프린터 코드 추출
			if(ValueUtil.isNotEmpty(box.getSubEquipCd())) {
				printerId = this.queryManager.selectBySql("select printer_cd from cells where domain_id = :domainId and cell_cd = :cellCd", params, String.class);
			}
			
			// 5.2 스테이지 별 디폴트 프린터 추출
			if(ValueUtil.isEmpty(printerId)) {
				printerId = this.queryManager.selectBySql("select id from printers where domain_id = :domainId and stage_cd = :stageCd", params, String.class);
			}
			
			pe.setPrinterId(printerId);
		}
	}	

}
