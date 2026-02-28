package xyz.anythings.base.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.query.util.IndicatorQueryUtil;
import xyz.anythings.gw.GwConstants;
import xyz.anythings.gw.entity.Gateway;
import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.gw.service.IndicatorDispatcher;
import xyz.anythings.gw.service.api.IIndRequestService;
import xyz.anythings.gw.service.model.IIndOnInfo;
import xyz.anythings.gw.service.model.IndCommonReq;
import xyz.anythings.gw.service.model.IndOnPickReq;
import xyz.anythings.gw.service.util.MwMessageUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 표시기 서비스 유틸리티
 * 
 * @author shortstop
 */
public class RuntimeIndServiceUtil {
	
	/**
	 * 표시기 점, 소등 서비스 리턴
	 * 
	 * @param batch
	 * @return
	 */
	public static IIndRequestService getIndicatorRequestService(JobBatch batch) {
		IndicatorDispatcher dispatcher = BeanUtil.get(IndicatorDispatcher.class);
		IIndRequestService indReqSvc = dispatcher.getIndicatorRequestServiceByBatch(batch.getId());
		
		if(indReqSvc == null) {
			IndConfigSet indConfigSet = batch.getIndConfigSet();
			dispatcher.addIndicatorConfigSet(batch.getId(), indConfigSet);
			indReqSvc = dispatcher.getIndicatorRequestServiceByBatch(batch.getId());
		}
		
		return indReqSvc;
	}
	
	/**
	 * 호기내 로케이션들 중에 거래처 매핑된 
	 * 
	 * @param batch
	 * @param indList
	 */
	public static int indOnNoboxDisplay(JobBatch batch, List<IndCommonReq> indList) {
		// 1. 빈 값 체크 
		if(ValueUtil.isNotEmpty(indList)) {
			IIndRequestService sendSvc = getIndicatorRequestService(batch);
			
			// 2. 점등 요청을 위한 데이터 모델 생성. 
			Map<String, List<IIndOnInfo>> indOnInfoList = new HashMap<String, List<IIndOnInfo>>();

			for (IndCommonReq indOnPick : indList) {
				String gwPath = indOnPick.getGwPath();
				List<IIndOnInfo> indOnList = indOnInfoList.containsKey(gwPath) ? 
						indOnInfoList.get(gwPath) : new ArrayList<IIndOnInfo>();
				IIndOnInfo indOnInfo = sendSvc.newIndicatorInfomration();
				indOnInfo.setId(indOnPick.getIndCd());
				indOnInfo.setBizId(indOnPick.getIndCd());
				indOnList.add(indOnInfo);
				indOnInfoList.put(gwPath, indOnList);
			}
		
			if(ValueUtil.isNotEmpty(indOnInfoList)) {
				// 3. 표시기 점등 요청
				sendSvc.requestIndListOn(batch.getDomainId(), batch.getStageCd(), batch.getJobType(), GwConstants.IND_ACTION_TYPE_NOBOX, indOnInfoList);
				// 4. 점등된 표시기 개수 리턴 
				return indOnInfoList.size();
			}
		}
		
		return 0;
	}
	
	/**
	 * 분류 작업 완료된 작업 리스트의 처리 수량을 표시기에 표시 
	 * 
	 * @param batch
	 * @param jobList
	 */
	public static int restoreIndDisplayJobPicked(JobBatch batch, List<JobInstance> jobList) {
		// 1. 빈 값 체크 
		if(ValueUtil.isNotEmpty(jobList)) {
			IIndRequestService sendSvc = getIndicatorRequestService(batch);
			
			// 2. 점등 요청을 위한 데이터 모델 생성. 
			Map<String, List<IIndOnInfo>> indOnInfoList = new HashMap<String, List<IIndOnInfo>>();
			
			for (JobInstance job : jobList) {
				String gwPath = job.getGwPath();
				List<IIndOnInfo> indOnList = indOnInfoList.containsKey(gwPath) ? 
						indOnInfoList.get(gwPath) : new ArrayList<IIndOnInfo>();
				
				IIndOnInfo indOnInfo = sendSvc.newIndicatorInfomration();
				indOnInfo.setId(job.getIndCd());
				indOnInfo.setBizId(job.getId());
				indOnInfo.setOrgEaQty(job.getPickedQty());
				indOnList.add(indOnInfo);
				indOnInfoList.put(gwPath, indOnList);
			}
			
			if(ValueUtil.isNotEmpty(indOnInfoList)) {
				// 3. 표시기 점등 요청
				sendSvc.requestIndListOn(batch.getDomainId(), batch.getStageCd(), batch.getJobType(), GwConstants.IND_ACTION_TYPE_DISPLAY, indOnInfoList);
				// 4. 점등된 표시기 개수 리턴 
				return indOnInfoList.size();
			}
		}
		
		return 0;
	}
	
	/**
	 * 작업이 완료된 표시기에 END 표시를 복원
	 * 
	 * @param batch
	 * @param gateway
	 * @return
	 */
	public static List<WorkCell> restoreIndDisplayBoxingEnd(JobBatch batch, Gateway gateway) {
		// 1. DAS, RTN에 대해서 로케이션의 status가 END, ENDED 상태인 모든 로케이션을 조회
		// TODO 쿼리로 수정 필요 - 표시기 개수가 1000개 이상인 경우 에러 발생하기 때문에 
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId(), 0, 0, "domain_id", "cell_cd", "ind_cd", "status", "job_instance_id");
		condition.addFilter("indCd", SysConstants.IN, IndicatorQueryUtil.searchIndCdList(gateway, batch.getEquipType(), batch.getEquipCd()));
		condition.addFilter("status", SysConstants.IN, LogisConstants.CELL_JOB_STATUS_END_LIST);
		condition.addOrder("cellCd", true);
		List<WorkCell> workCells = BeanUtil.get(IQueryManager.class).selectList(WorkCell.class, condition);
		
		// 2. 로케이션 별로 상태별로 END (ReadOnly = false), END (ReadOnly = true)를 표시
		return restoreIndDisplayBoxingEnd(batch, workCells);
	}
	
	/**
	 * 호기, 작업 존에 작업이 완료된 표시기에 END 표시를 복원
	 * 
	 * @param batch
	 * @param equipType
	 * @param equipCd
	 * @param equipZone
	 * @return
	 */
	public static List<WorkCell> restoreIndDisplayBoxingEnd(JobBatch batch, String equipZone) {
		// 1. DAS, RTN에 대해서 로케이션의 jobStatus가 END, ENDED 상태인 모든 로케이션을 조회
		// TODO 쿼리로 수정 필요 - 표시기 개수가 1000개 이상인 경우 에러 발생하기 때문에 
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId(), 0, 0, "domain_id", "cell_cd", "ind_cd", "status", "job_instance_id");
		condition.addFilter("indCd", SysConstants.IN, IndicatorQueryUtil.searchIndCdList(batch.getDomainId(), null, batch.getEquipType(), batch.getEquipCd(), null, equipZone));
		condition.addFilter("status", SysConstants.IN, LogisConstants.CELL_JOB_STATUS_END_LIST);
		condition.addOrder("cellCd", true);
		List<WorkCell> workCells = BeanUtil.get(IQueryManager.class).selectList(WorkCell.class, condition);
		
		// 2. 로케이션 별로 상태별로 END (ReadOnly = false), END (ReadOnly = true)를 표시
		return restoreIndDisplayBoxingEnd(batch, workCells);
	}
	
	/**
	 * 로케이션 별로 상태별로 END (ReadOnly = false), END (ReadOnly = true)를 표시
	 * 
	 * @param jobType
	 * @param workCells
	 * @return
	 */
	public static List<WorkCell> restoreIndDisplayBoxingEnd(JobBatch batch, List<WorkCell> workCells) {
		if(ValueUtil.isNotEmpty(workCells)) {
			IIndRequestService indSendService = getIndicatorRequestService(batch);

			for(WorkCell cell : workCells) {
				String jobStatus = cell.getStatus();
				// FIXME gwPath 조회
				String gwPath = null; //cell.getGwPath();
				
				if(ValueUtil.isNotEmpty(jobStatus)) {
					if(ValueUtil.isEqual(LogisConstants.CELL_JOB_STATUS_ENDING, jobStatus)) {
						String bizId = ValueUtil.isEmpty(cell.getJobInstanceId()) ? cell.getIndCd() : cell.getJobInstanceId();
						indSendService.requestIndEndDisplay(cell.getDomainId(), batch.getStageCd(), batch.getJobType(), gwPath, cell.getIndCd(), bizId, false);
						
					} else if(ValueUtil.isEqual(LogisConstants.CELL_JOB_STATUS_ENDED, jobStatus)) {
						String bizId = ValueUtil.isEmpty(cell.getJobInstanceId()) ? cell.getIndCd() : cell.getJobInstanceId();
						indSendService.requestIndEndDisplay(cell.getDomainId(), batch.getStageCd(), batch.getJobType(), gwPath, cell.getIndCd(), bizId, true);
					}
				}
			}
		}
		
		return workCells;
	}
	
	/**
	 * 작업 데이터로 표시기를 점등한다.
	 * 
	 * @param needUpdateJobStatus
	 * @param batch
	 * @param job
	 * @return
	 */
	public static boolean indOnByJob(boolean needUpdateJobStatus, JobBatch batch, JobInstance job) {
		if(ValueUtil.isEmpty(job.getGwPath())) {
			String gwPath = IndicatorQueryUtil.findGatewayPathByIndCd(job.getDomainId(), job.getIndCd());
			job.setGwPath(gwPath);
		}
		
		RuntimeIndServiceUtil.indOnByJobList(needUpdateJobStatus, batch, ValueUtil.toList(job));
		return true;
	}
	
	/**
	 * 작업 데이터로 표시기를 점등한다.
	 * 
	 * @param needUpdateJobStatus
	 * @param batch
	 * @param job
	 * @param showPickingQty 작업의 피킹 수량을 표시기의 분류 수량으로 표시할 지 여부
	 * @return
	 */
	public static boolean indOnByJob(boolean needUpdateJobStatus, JobBatch batch, JobInstance job, boolean showPickingQty) {
		if(ValueUtil.isEmpty(job.getGwPath())) {
			String gwPath = IndicatorQueryUtil.findGatewayPathByIndCd(job.getDomainId(), job.getIndCd());
			job.setGwPath(gwPath);
		}
		
		RuntimeIndServiceUtil.indOnByJobList(needUpdateJobStatus, batch, ValueUtil.toList(job), showPickingQty);
		return true;
	}
	
	/**
	 * 작업 리스트 정보로 표시기 점등
	 * 
	 * @param needUpdateJobStatus Job 데이터의 상태 변경이 필요한 지 여부
	 * @param batch
	 * @param jobList 작업 데이터 리스트
	 * @return 점등된 표시기 개수 리턴
	 */
	public static int indOnByJobList(boolean needUpdateJobStatus, JobBatch batch, List<JobInstance> jobList) {
		// 1. 빈 값 체크 
		if(ValueUtil.isNotEmpty(jobList)) {
			IIndRequestService indSendService = getIndicatorRequestService(batch);
			
			// 2. 점등 요청을 위한 데이터 모델 생성. 
			Map<String, List<IIndOnInfo>> indOnList = buildIndOnList(needUpdateJobStatus, batch, jobList, false);
			
			if(ValueUtil.isNotEmpty(indOnList)) {
				JobInstance firstJob = jobList.get(0);
				// 3. 표시기 점등 요청
				indSendService.requestIndListOn(firstJob.getDomainId(), batch.getStageCd(), batch.getJobType(), GwConstants.IND_ACTION_TYPE_PICK, indOnList);
				// 4. 점등된 표시기 개수 리턴 
				return indOnList.size();
			}
		}
		
		return 0;
	}
	
	/**
	 * 작업 리스트 정보로 표시기 점등 
	 * 
	 * @param needUpdateJobStatus Job 데이터의 상태 변경이 필요한 지 여부
	 * @param batch
	 * @param jobList 작업 데이터 리스트
	 * @param showPickingQty 작업의 피킹 수량을 표시기의 분류 수량으로 표시할 지 여부
	 * @return 점등된 표시기 개수 리턴
	 */
	public static int indOnByJobList(boolean needUpdateJobStatus, JobBatch batch, List<JobInstance> jobList, boolean showPickingQty) {
		// 1. 빈 값 체크 
		if(ValueUtil.isNotEmpty(jobList)) {
			// 2. 점등 요청을 위한 데이터 모델 생성. 
			Map<String, List<IIndOnInfo>> indOnList = buildIndOnList(needUpdateJobStatus, batch, jobList, true);
			
			if(ValueUtil.isNotEmpty(indOnList)) {
				IIndRequestService indSendService = getIndicatorRequestService(batch);
				
				JobInstance firstJob = jobList.get(0);
				// 3. 표시기 점등 요청
				indSendService.requestIndListOn(firstJob.getDomainId(), batch.getStageCd(), batch.getJobType(), GwConstants.IND_ACTION_TYPE_PICK, indOnList);
				// 4. 점등된 표시기 개수 리턴 
				return indOnList.size();
			}
		}
		
		return 0;
	}
	
	/**
	 * 작업 리스트 정보 중 피킹 상태의 정보만 표시기 점등
	 * 
	 * @param needUpdateJobStatus
	 * @param qtyNoCheck
	 * @param batch
	 * @param jobList
	 * @return
	 */
	public static int indOnByPickingJobList(boolean needUpdateJobStatus, boolean qytNoCheck, JobBatch batch, List<JobInstance> jobList) {
		// 1. 빈 값 체크 
		if(ValueUtil.isNotEmpty(jobList)) {
			List<JobInstance> pickingJobs = new ArrayList<JobInstance>(jobList.size());
			
			for(JobInstance job : jobList) {
				// 피킹 예정 수량이 피킹 확정 수량보다 큰 것만 표시기 점등 
				if(qytNoCheck || (job.getPickQty() > job.getPickedQty())) {
					pickingJobs.add(job);
				}
			}
			
			if(ValueUtil.isNotEmpty(pickingJobs)) {
				IIndRequestService indSendService = getIndicatorRequestService(batch);
				
	 			// 2. 점등 요청을 위한 데이터 모델 생성. 
				Map<String, List<IIndOnInfo>> indOnList = buildIndOnList(needUpdateJobStatus, batch, jobList, false);
				
				if(ValueUtil.isNotEmpty(indOnList)) {
					// 3. 표시기 점등 요청
					indSendService.requestIndListOn(batch.getDomainId(), batch.getStageCd(), batch.getJobType(), GwConstants.IND_ACTION_TYPE_PICK, indOnList);
					// 4. 점등된 표시기 개수 리턴 
					return indOnList.size();
				}
			}
		}
		
		return 0;
	}
	
	/**
	 * 작업 리스트 정보 중 피킹 상태의 정보만 표시기 점등
	 * 
	 * @param needUpdateJobStatus
	 * @param batch
	 * @param jobList
	 * @return
	 */
	public static int indDisplayByPickingJobList(boolean needUpdateJobStatus, boolean qytNoCheck, JobBatch batch, List<JobInstance> jobList) {
		// 1. 빈 값 체크 
		if(ValueUtil.isNotEmpty(jobList)) {
			List<JobInstance> pickingJobs = new ArrayList<JobInstance>(jobList.size());
			
			for(JobInstance job : jobList) {
				// 피킹 예정 수량이 피킹 확정 수량보다 큰 것만 표시기 점등 
				if(qytNoCheck || (job.getPickQty() > job.getPickedQty())) {
					pickingJobs.add(job);
				}
			}
			
			if(ValueUtil.isNotEmpty(pickingJobs)) {
				IIndRequestService indSendService = getIndicatorRequestService(batch);
				
	 			// 2. 점등 요청을 위한 데이터 모델 생성. 
				Map<String, List<IIndOnInfo>> indOnList = buildIndOnList(needUpdateJobStatus, batch, jobList, false);
				
				if(ValueUtil.isNotEmpty(indOnList)) {
					// 3. 표시기 점등 요청
					indSendService.requestIndListOn(batch.getDomainId(), batch.getStageCd(), batch.getJobType(), GwConstants.IND_ACTION_TYPE_DISPLAY, indOnList);
					// 4. 점등된 표시기 개수 리턴 
					return indOnList.size();
				}
			}
		}
		
		return 0;
	}
	
	/**
	 * 작업 리스트 정보로 검수를 위한 표시기 점등 
	 * 
	 * @param batch 작업 배치
	 * @param jobList 작업 데이터 리스트
	 * @return 점등된 표시기 개수 리턴
	 */
	public static int indOnByInspectJobList(JobBatch batch, List<JobInstance> jobList) {
		// 1. 빈 값 체크 
		if(ValueUtil.isNotEmpty(jobList)) {
			
			// 2. 검수 색깔은 빨간색으로 고정
			//for(JobInstance job : jobList) {
			//	job.setColorCd(LogisConstants.COLOR_RED);
			//}
			
			// 3. 점등 요청을 위한 데이터 모델 생성. 
			Map<String, List<IIndOnInfo>> indOnList = buildIndOnList(false, batch, jobList, false);
			
			if(ValueUtil.isNotEmpty(indOnList)) {
				IIndRequestService indSendService = getIndicatorRequestService(batch);
				// 4. 표시기 점등 요청
				indSendService.requestIndListOnForInspect(batch.getDomainId(), batch.getStageCd(), batch.getJobType(), indOnList);
				// 5. 점등된 표시기 개수 리턴 
				return indOnList.size();
			}
		}
		
		return 0;
	}
	
	/**
	 * jobList로 부터 표시기 점등 모델을 생성하여 리턴 
	 * 
	 * @param needUpdateJobStatus 표시기 점등 후 Job 데이터의 상태 변경이 필요한 지 여부
	 * @param batch
	 * @param jobList
	 * @param showPickingQty JobInstance의 pickQty가 아니라 pickingQty를 표시기의 분류 수량으로 표시할 지 여부
	 * @return
	 */
	public static Map<String, List<IIndOnInfo>> buildIndOnList(
			boolean needUpdateJobStatus, JobBatch batch, List<JobInstance> jobList, boolean showPickingQty) {
		
		if(ValueUtil.isNotEmpty(jobList)) {
			List<IndOnPickReq> indListToLightOn = new ArrayList<IndOnPickReq>(jobList.size());
			String pickStartedAt = needUpdateJobStatus ? DateUtil.currentTimeStr() : null;
			
			// 점등 요청을 위한 데이터 모델 생성.
			for(JobInstance job : jobList) {
				// 상태가 처리 예정인 작업만 표시기 점등 
				if(needUpdateJobStatus && job.isTodoJob()) {
					// 1. 분류 대상 피킹 시간 업데이트
					job.setPickStartedAt(pickStartedAt);
					// 2. 상태 코드 설정
					job.setStatus(LogisConstants.JOB_STATUS_PICKING);
				}
				
				// 3. 점등 요청 모델 생성 및 복사  
				IndOnPickReq lightOn = ValueUtil.populate(job, new IndOnPickReq(), "comCd", "inputSeq", "indCd", "colorCd", "pickQty", "boxInQty", "gwPath");
				// 4. 비지니스 ID 설정
				lightOn.setJobInstanceId(job.getId());
				// 5. pickingQty를 표시
				if(showPickingQty) {
					lightOn.setPickQty(job.getPickingQty());
				}
				// 6. 표시기 점등을 위한 리스트에 추가
				indListToLightOn.add(lightOn);
			}
			
			if(needUpdateJobStatus) {
				BeanUtil.get(IQueryManager.class).updateBatch(jobList, "pickingQty", "indCd", "pickStartedAt", "status");
			}
			
			// 분류 대상 작업 데이터를 표시기 점등 요청을 위한 프로토콜 모델로 변환한다.
			return indListToLightOn.isEmpty() ? null : MwMessageUtil.groupPickingByGwPath(batch.getId(), indListToLightOn);
			
		} else {
			return null;
		}
	}
	
	/**
	 * jobList로 부터 표시기 점등 모델을 생성하여 리턴 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param jobList
	 * @return
	 */
	public static Map<String, List<IIndOnInfo>> buildTestIndOnList(Long domainId, String stageCd, String jobType, List<JobInstance> jobList) {
		
		if(ValueUtil.isNotEmpty(jobList)) {
			List<IndOnPickReq> indListToLightOn = new ArrayList<IndOnPickReq>(jobList.size());
			
			// 점등 요청을 위한 데이터 모델 생성.
			for(JobInstance job : jobList) {
				// 1. 점등 요청 모델 생성 및 복사  
				IndOnPickReq lightOn = ValueUtil.populate(job, new IndOnPickReq(), "inputSeq", "indCd", "colorCd", "pickQty", "boxInQty", "gwPath");
				// 2. 비지니스 ID 설정
				lightOn.setJobInstanceId(job.getId());
				// 3. 표시기 점등을 위한 리스트에 추가
				indListToLightOn.add(lightOn);
			}
			
			return indListToLightOn.isEmpty() ? null : MwMessageUtil.groupTestByGwPath(domainId, stageCd, indListToLightOn);
			
		} else {
			return null;
		}
	}

}
