package xyz.anythings.gw.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Table(name = "deployments", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_deployments_0", columnList = "target_type,target_id,version,domain_id")
})
public class Deployment extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 139716252076979474L;

	/**
	 * 펌웨어 타입 - 게이트웨이
	 */
	public static final String TARGET_TYPE_GW = "GW";
	/**
	 * 펌웨어 타입 - 표시기 
	 */
	public static final String TARGET_TYPE_MPI = "MPI";
	/**
	 * 펌웨어 배포 상태 - 예약 대기 
	 */
	public static final String STATUS_WAIT = "WAIT";
	/**
	 * 펌웨어 배포 상태 - 예약됨 
	 */
	public static final String STATUS_RESERVED = "RESERVED";
	/**
	 * 펌웨어 배포 상태 - 배포 진행 중 
	 */
	public static final String STATUS_RUN = "RUN";
	/**
	 * 펌웨어 배포 상태 - 배포 완료
	 */
	public static final String STATUS_END = "END";
	/**
	 * 펌웨어 배포 상태 - 배포 중 에러 발생
	 */
	public static final String STATUS_ERROR = "ERROR";
	
	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "target_type", nullable = false, length = 20)
	private String targetType;

	@Column (name = "target_id", nullable = false, length = 40)
	private String targetId;

	@Column (name = "version", nullable = false, length = 15)
	private String version;

	@Column (name = "scheduled_at", nullable = false, length = 22)
	private String scheduledAt;

	@Column (name = "started_at", length = 22)
	private String startedAt;

	@Column (name = "finished_at", length = 22)
	private String finishedAt;

	@Column (name = "remark", length = 1000)
	private String remark;

	@Column (name = "file_name", length = 100)
	private String fileName;

	@Column (name = "file_size", length = 22)
	private Long fileSize;

	@Column (name = "force_flag", length = 1)
	private Boolean forceFlag;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "file_data")
	private byte[] fileData;
  
	@Ignore
	private String attachTempId;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getScheduledAt() {
		return scheduledAt;
	}

	public void setScheduledAt(String scheduledAt) {
		this.scheduledAt = scheduledAt;
	}

	public String getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(String startedAt) {
		this.startedAt = startedAt;
	}

	public String getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(String finishedAt) {
		this.finishedAt = finishedAt;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Boolean getForceFlag() {
		return forceFlag;
	}

	public void setForceFlag(Boolean forceFlag) {
		this.forceFlag = forceFlag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}
	
	public String getAttachTempId() {
		return attachTempId;
	}

	public void setAttachTempId(String attachTempId) {
		this.attachTempId = attachTempId;
	}
	
	public String computeDownloadUrl() {
		String contextPath = SettingUtil.getValue("mps.deployment.download.context.path");
		String divider = contextPath.endsWith(SysConstants.SLASH) ? SysConstants.EMPTY_STRING : SysConstants.SLASH;
		return contextPath + divider + "rest/deployment/download_file/" + this.id;
	}
	
	@Override 
	public void beforeCreate() {
		super.beforeCreate();
		
		// AttachTemp id와 Deployment id와 Job id는 모두 동일하다. 
		IQueryManager queryMgr = BeanUtil.get(IQueryManager.class);
		AttachTemp attachTemp = queryMgr.select(AttachTemp.class, this.attachTempId);
		this.setId(this.attachTempId);
		this.setStatus(Deployment.STATUS_WAIT);
		this.setFileName(attachTemp.getFileName());
		this.setFileSize(attachTemp.getFileSize());
		this.setFileData(attachTemp.getFileData());
	}
	
	@Override
	public void beforeDelete() {
		super.beforeDelete();
		
		// 1. AttachTemp 삭제
		IQueryManager queryMgr = BeanUtil.get(IQueryManager.class);
		queryMgr.delete(AttachTemp.class, this.id);
		
		// 2. Job 조회 후에 Job unschedule 및 삭제 
		/*if(ValueUtil.isEqualIgnoreCase(this.status, Deployment.STATUS_RESERVED)) {
			try {
				Job job = queryMgr.select(Job.class, this.id);
				if(job != null) {
					job.unscheduleJob();
					queryMgr.delete(Job.class, this.id);
				}
			} catch(Exception e) {
				throw new ElidomRuntimeException(e.getMessage(), e);
			}
		}*/
	}
	
	/**
	 * 즉시 배포 - 펌웨어 배포 프로세스 실행
	 * 
	 * @return
	 */
	/*public Boolean deployNow() {
		if(!ValueUtil.isEqualIgnoreCase(this.status, Deployment.STATUS_WAIT) && !ValueUtil.isEqualIgnoreCase(this.status, Deployment.STATUS_RESERVED)) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("NOT_WAIT_OR_RESERVED"));
		}
		
		this.setStatus(Deployment.STATUS_RUN);
		this.setStartedAt(DateUtil.currentTimeStr());
		BeanUtil.get(Type1FirmwareDeployService.class).deployFirmware(this);
		this.setStatus(Deployment.STATUS_END);
		this.setFinishedAt(DateUtil.currentTimeStr());
		BeanUtil.get(IQueryManager.class).update(this, SysConstants.ENTITY_FIELD_STATUS, "startedAt", "finishedAt");
		return true;
	}*/
	
	/**
	 * 배포 예약
	 */
	public Boolean reserve() {
		// 1. 예약 전 체크 
		if(!ValueUtil.isEqualIgnoreCase(this.status, Deployment.STATUS_WAIT)) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("STATE_NOT_WAIT_RESERVATION"));
		}
		
		if(ValueUtil.isEmpty(this.scheduledAt)) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("TIME_IS_EMPTY_CANNOT_RESERVATION"));
		}
		
		// 2. Job 생성 
		/*String deploymentJobName = this.targetType + " (" + this.targetId + ") - " + this.version;
		Job deploymentJob = new Job(this.domainId, deploymentJobName);
		deploymentJob.setId(this.id);
		deploymentJob.setDescription(this.remark);
		deploymentJob.setHandlerType(JobModel.HANDLER_STATIC);
		deploymentJob.setHandler(DeploymentJob.class.getName());
		deploymentJob.setRepeatCount(1);
		String intervalExpr = this.toScheduleExpression(this.scheduledAt); 
		deploymentJob.setIntervalExpr(intervalExpr);
		deploymentJob.setTrace(false);
		IQueryManager queryMgr = BeanUtil.get(IQueryManager.class);
		queryMgr.insert(deploymentJob);
		
		// 3. Job 스케줄링
		JobController jobCtrl = BeanUtil.get(JobController.class);
		Boolean scheduled = jobCtrl.startJob(deploymentJob.getId());
		
		// 4. 배포 상태 업데이트
		if(scheduled) {
			this.status = Deployment.STATUS_RESERVED;
			queryMgr.update(this, OrmConstants.ENTITY_FIELD_STATUS);
		}
		
		// 5. 예약 결과 리턴
		return scheduled;*/
		return false;
	}
	
	/**
	 * 배포 일자 형식을 interval expression으로 변환 
	 * 
	 * @param deploymentTime
	 * @return
	 */
	/*private String toScheduleExpression(String deploymentTime) {
		// 22018년 3월 5일 12시 25분 0초 -> 0 25 12 3 5 2018
		Date date = DateUtil.parse(deploymentTime);
		Date currentDate = new Date();
		
		if(currentDate.getTime() > date.getTime()) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("DIST_RESERVATION_TIME_EARLIER_CURRENT_TIME"));
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return new StringBuffer("0 ").
				append(calendar.get(Calendar.MINUTE)).
				append(SysConstants.SPACE).
				append(calendar.get(Calendar.HOUR_OF_DAY)).
				append(SysConstants.SPACE).
				append(calendar.get(Calendar.DAY_OF_MONTH)).
				append(SysConstants.SPACE).
				append(calendar.get(Calendar.MONTH) + 1).
				append(" ? ").
				append(calendar.get(Calendar.YEAR)).toString();
	}*/

}
