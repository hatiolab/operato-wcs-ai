/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.model;

import java.util.HashMap;
import java.util.Map;

import org.quartz.JobExecutionContext;

import xyz.elidom.job.entity.JobTrace;
import xyz.elidom.job.rest.JobTraceController;
import xyz.elidom.job.util.SchedulerUtil;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.system.engine.IScriptEngine;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;

/**
 * JobModel 기본 구현
 * 
 * @author shortstop
 */
public abstract class AbstractJobModel implements JobModel {

	@Override
	public boolean scheduleJob() {
		boolean scheduled = true;
		
		try {
			if(ValueUtil.isEqual(this.getStatus(), JobModel.STATUS_PAUSED)) {
				SchedulerUtil.resumeJob(this);
			} else {
				SchedulerUtil.scheduleJob(this);
			}
			
			this.setOkCount(0);
			this.setNgCount(0);
			this.setStatus(JobModel.STATUS_RUNNING);
			
		} catch (Exception e) {
			this.setResult("Failed to schedule job - " + e.getCause() == null ? e.getMessage() : e.getCause().getMessage()); 
			scheduled = false;
		}
		
		BeanUtil.get(IQueryManager.class).update(this);
		return scheduled;
	}

	@Override
	public boolean unscheduleJob() {
		boolean unscheduled = true;
		try {
			unscheduled = SchedulerUtil.deleteJob(this);
			this.setOkCount(0);
			this.setNgCount(0);
			
		} catch (Exception e) {
			this.setResult("Failed to unschedule job - " + e.getCause() == null ? e.getMessage() : e.getCause().getMessage()); 
			unscheduled = false;
		}
				
		if(unscheduled) {
			this.setStatus(JobModel.STATUS_DELETED);
		}
		
		BeanUtil.get(IQueryManager.class).update(this);
		return unscheduled;
	}

	@Override
	public boolean pauseJob() {
		boolean paused = true;
		
		try {
			paused = SchedulerUtil.pauseJob(this);
			this.setOkCount(0);
			this.setNgCount(0);
			
		} catch (Exception e) {
			this.setResult("Failed to pause job - " + e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
			paused = false;
		}
				
		if(paused) {
			this.setStatus(JobModel.STATUS_PAUSED);
		}
		
		BeanUtil.get(IQueryManager.class).update(this);
		return paused;
	}	

	@Override
	public Object executeLogic(Map<String, Object> params) {
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		if(params == null) {
			params = new HashMap<String, Object>();
		}

		params.put("job", this);
		return scriptEngine.runScript(IScriptEngine.SCRIPT_GROOVY, this.getLogic(), params);
	}

	@Override
	public boolean beforeExecuteJob(JobExecutionContext context) {
		if(this.getRepeatCount() >= 1) {
			if(this.getOkCount() + this.getNgCount() >= this.getRepeatCount()) {
				this.setResult("Job is paused because executed count is over repeat count!");
				this.pauseJob();
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void afterExecuteJob(JobExecutionContext context) {
		// update self
		this.setOkCount(this.getOkCount() + 1);
		this.setStatus(JobModel.STATUS_RUNNING);
		BeanUtil.get(IQueryManager.class).update(this);
		
		// create JobTrace
		if(context.get("jobTrace") != null) {
			JobTrace trace = (JobTrace)context.get("jobTrace");
			trace.setFinishedAt(DateUtil.currentTimeStr());
			trace.setStatus(JobModel.EXECUTION_OK);			
			JobTraceController jobTraceController = BeanUtil.get(JobTraceController.class);
			jobTraceController.create(trace);
		}
	}

	@Override
	public void onJobError(JobExecutionContext context, Throwable th) {
		// update self
		this.setNgCount(this.getNgCount() + 1);
		this.setStatus(JobModel.STATUS_RUNNING);
		this.setResult("Failed to execute - " + th.getCause() == null ? th.getMessage() : th.getCause().getMessage());
		BeanUtil.get(IQueryManager.class).update(this);
		
		// create JobTrace
		if(context.get("jobTrace") != null) {
			JobTrace trace = (JobTrace)context.get("jobTrace");
			trace.setFinishedAt(DateUtil.currentTimeStr());
			trace.setStatus(JobModel.EXECUTION_NG);			
			JobTraceController jobTraceController = BeanUtil.get(JobTraceController.class);
			jobTraceController.create(trace);
		}
	}

}
