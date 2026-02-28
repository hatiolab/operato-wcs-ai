/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.entity;


import java.util.HashMap;
import java.util.Map;

import org.quartz.JobExecutionContext;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.job.model.JobModel;
import xyz.elidom.job.util.SchedulerUtil;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;
import xyz.elidom.sys.system.engine.IScriptEngine;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;

@Table(name = "jobs", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,name", indexes = { 
	@Index(name = "ix_job_0", columnList = "domain_id,name", unique = true),
	@Index(name = "ix_job_1", columnList = "domain_id,status")
})
public class Job extends ElidomStampHook implements JobModel {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -6014336081076871316L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column (name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String name;

	@Column (name = "description", length = OrmConstants.FIELD_SIZE_DESCRIPTION)
	private String description;

	@Column (name = "handler_type", nullable = false, length = 15)
	private String handlerType;

	@Column (name = "handler", length = OrmConstants.FIELD_SIZE_LONG_NAME)
	private String handler;

	@Column (name = "interval_expr", nullable = false, length = 80)
	private String intervalExpr;
	
	@Column(name = "timezone", length = 64)
	private String timezone;
	
	@Column (name = "repeat_count")
	private Integer repeatCount;
	
	@Column (name = "ok_count")
	private Integer okCount;

	@Column (name = "ng_count")
	private Integer ngCount;
	
	@Column (name = "status", length = 10)
	private String status;
	
	@Column (name = "trace")
	private Boolean trace;

	@Column (name = "result", length = OrmConstants.FIELD_SIZE_MAX_TEXT)
	private String result;
	
	@Column(name = "logic", type = ColumnType.TEXT)	
	private String logic;
  
	public Job() {
	}
	
	public Job(Long domainId, String name) {
		this.domainId = domainId;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHandlerType() {
		return handlerType;
	}

	public void setHandlerType(String handlerType) {
		this.handlerType = handlerType;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getIntervalExpr() {
		return intervalExpr;
	}

	public void setIntervalExpr(String intervalExpr) {
		this.intervalExpr = intervalExpr;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public Integer getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(Integer repeatCount) {
		this.repeatCount = repeatCount;
	}
	
	public Integer getOkCount() {
		return okCount;
	}

	public void setOkCount(Integer okCount) {
		this.okCount = okCount;
	}

	public Integer getNgCount() {
		return ngCount;
	}

	public void setNgCount(Integer ngCount) {
		this.ngCount = ngCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getTrace() {
		return trace;
	}

	public void setTrace(Boolean trace) {
		this.trace = trace;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getLogic() {
		return logic;
	}

	public void setLogic(String logic) {
		this.logic = logic;
	}

	@Override
	public String getJobGroupName() {
		return "Job-" + this.domainId;
	}
	
	@Override
	public JobTrace newJobTrace() {
		if(this.getTrace()) {
			JobTrace jobTrace = new JobTrace(this);
			jobTrace.setStartedAt(DateUtil.currentTimeStr());
			return jobTrace;
		} else {
			return null;
		}		
	}
	
	@Override
	public boolean scheduleJob() {
		boolean scheduled = true;
		
		try {
			if(ValueUtil.isEqual(this.status, JobModel.STATUS_PAUSED)) {
				SchedulerUtil.resumeJob(this);
			} else {
				SchedulerUtil.scheduleJob(this);
			}
			
			this.status = JobModel.STATUS_RUNNING;
			this.result = "started OK";
			
		} catch (Exception e) {
			this.result = "Failed to schedule job - " + ((e.getCause() == null) ? e.getMessage() : e.getCause().getMessage());
			scheduled = false;
		}
		
		BeanUtil.get(IQueryManager.class).update(this, "okCount", "ngCount", "status", "result");
		return scheduled;
	}

	@Override
	public boolean unscheduleJob() {
		boolean unscheduled = true;
		try {
			unscheduled = SchedulerUtil.deleteJob(this);
			
		} catch (Exception e) {
			this.result = "Failed to unschedule job - " + ((e.getCause() == null) ? e.getMessage() : e.getCause().getMessage());
			unscheduled = false;
		}
				
		if(unscheduled) {
			this.okCount = 0;
			this.ngCount = 0;	
			this.status = JobModel.STATUS_DELETED;
		}
		
		BeanUtil.get(IQueryManager.class).update(this, "okCount", "ngCount", "status", "result");
		return unscheduled;
	}
	
	@Override
	public boolean interrupt() {
		boolean interrupted = true;
		try {
			interrupted = SchedulerUtil.interrupt(this);
			this.okCount = 0;
			this.ngCount = 0;

		} catch (Exception e) {
			this.result = "Failed to interrupt job - "
					+ ((e.getCause() == null) ? e.getMessage() : e.getCause().getMessage());
			interrupted = false;
		}

		if (interrupted) {
			this.status = JobModel.STATUS_DELETED;
		}

		BeanUtil.get(IQueryManager.class).update(this, "okCount", "ngCount", "status", "result");
		return interrupted;
	}
	
	@Override
	public boolean pauseJob() {
		boolean paused = true;
		
		try {
			paused = SchedulerUtil.pauseJob(this);
			
		} catch (Exception e) {
			this.result = "Failed to pause job - " + ((e.getCause() == null) ? e.getMessage() : e.getCause().getMessage());
			paused = false;
		}
				
		if(paused) {
			this.status = JobModel.STATUS_PAUSED;
		}
		
		BeanUtil.get(IQueryManager.class).update(this, "okCount", "ngCount", "status", "result");
		return paused;
	}	

	@Override
	public Object executeLogic(Map<String, Object> params) {
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		if(params == null) {
			params = new HashMap<String, Object>();
		}

		params.put("job", this);
		return scriptEngine.runScript(IScriptEngine.SCRIPT_GROOVY, this.logic, params);
	}
	
	@Override
	public Object executeQuery(Map<String, Object> params) {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		if(params == null) {
			params = new HashMap<String, Object>();
		}

		params.put("domainId", this.getDomainId());
		params.put("job", this);
		return queryManager.selectListBySql(this.logic, params, Map.class, 0, 0);
	}

	@Override
	public boolean beforeExecuteJob(JobExecutionContext context) {
		if(this.repeatCount >= 1 && ((this.okCount + this.ngCount) >= this.repeatCount)) {
			this.result = "Job executed total " + (this.okCount + this.ngCount) + " times!";
			// TODO Pause Job이 수행이 안 됨. 큐에 등록한 후 몇 초 후에 처리하든가 아니면 Job 실행이 완료된 이후에 실행 되든가 ...
			this.pauseJob();
			return false;
		}
		
		return true;
	}

	@Override
	public void afterExecuteJob(JobExecutionContext context) {
		// Update self
		this.okCount++;
		this.status = JobModel.STATUS_RUNNING;
		this.result = "";
		BeanUtil.get(IQueryManager.class).update(this, "okCount", "status", "result");
		
		// Create JobTrace
		if(context.get("jobTrace") != null) {
			JobTrace trace = (JobTrace)context.get("jobTrace");
			JobTrace.jobTrace(trace, JobModel.EXECUTION_OK, null);
		}
	}

	@Override
	public void onJobError(JobExecutionContext context, Throwable th) {
		// Update self
		this.ngCount++;
		this.status = JobModel.STATUS_RUNNING;
		this.result = (th.getCause() == null) ? th.getMessage() : th.getCause().getMessage();
		BeanUtil.get(IQueryManager.class).update(this, "ngCount", "status", "result");
		
		// Create JobTrace
		if(context.get("jobTrace") != null) {
			JobTrace trace = (JobTrace)context.get("jobTrace");
			JobTrace.jobTrace(trace, JobModel.EXECUTION_NG, this.result);
		}
	}
}