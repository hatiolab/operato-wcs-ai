/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.system.job;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import xyz.elidom.job.entity.JobTrace;
import xyz.elidom.job.model.JobModel;
import xyz.elidom.job.util.SchedulerUtil;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.BeanUtil;

/**
 * Abstract Job
 * 
 * @author shortstop
 */
public abstract class AbstractJob extends QuartzJobBean {
	
	/**
	 * JOB MODEL ID
	 */
	public static final String STR_JOB_MODEL_ID = "jobModelId";
	/**
	 * JOB MODEL CLASS
	 */
	public static final String STR_JOB_MODEL_CLASS = "jobModelClass";
	/**
	 * JOB Model Key
	 */
	public static final String STR_JOB_MODEL = "job";
	/**
	 * JOB TRACE Key
	 */
	public static final String STR_JOB_TRACE = "jobTrace";	
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		JobModel jobModel = this.getJobModel(context);
		JobTrace jobTrace = jobModel.newJobTrace();
		
		if(jobTrace != null) {
			context.put(STR_JOB_TRACE, jobTrace);
		}
		
		// 1. validation check
		boolean goOn = jobModel.beforeExecuteJob(context);
		if(goOn) {
			Domain domain = BeanUtil.get(DomainController.class).findOne(jobModel.getDomainId(), null);
			DomainContext.setCurrentDomain(domain);
			try {
				// 2. job 수행
				BeanUtil.get(this.getClass()).doExecuteJob(context, jobModel);
				// 3. job 수행 후 jobTrace 처리 등 후처리 ...
				jobModel.afterExecuteJob(context);
				
			} catch(Throwable th) {
				// 4. error 처리 
				jobModel.onJobError(context, th);
				
			} finally {
				// 5. Clear context && unnecessary stuff
				context.getJobDetail().getJobDataMap().clear();
				context.getMergedJobDataMap().clear();
				//SchedulerUtil.clear();
				// 6. Reschedule Job
				SchedulerUtil.rescheduleJob(context, jobModel);
				DomainContext.unsetCurrentDomain();
				DomainContext.unsetUserObject();
			}
		}
	}
	
	/**
	 * Abstract method
	 * 
	 * @param context
	 * @param jobModel
	 * @throws JobExecutionException
	 */
	public abstract Object doExecuteJob(JobExecutionContext context, JobModel jobModel) throws JobExecutionException;
	
	/**
	 * JobModel을 리턴 
	 * 
	 * @param context
	 * @return
	 */
	protected JobModel getJobModel(JobExecutionContext context) {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		
		if(jobDataMap.containsKey(STR_JOB_MODEL_ID)) {
			Object jobModelId = jobDataMap.get(STR_JOB_MODEL_ID);
			Class<?> jobModelClass = null;
			
			try {
				jobModelClass = jobDataMap.containsKey(STR_JOB_MODEL_CLASS) ? 
						Class.forName(jobDataMap.get(STR_JOB_MODEL_CLASS).toString()) : xyz.elidom.job.entity.Job.class;
			} catch (ClassNotFoundException e) {
				jobModelClass = xyz.elidom.job.entity.Job.class;
			}
			
			IQueryManager queryMan = BeanUtil.get(IQueryManager.class);
			Object jobObj = queryMan.select(jobModelClass, jobModelId);
			return jobObj == null ? null : (JobModel)jobObj;
			
		} else {
			return null;
		}
	}
	
}