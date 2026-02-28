/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.util;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import xyz.elidom.core.CoreConstants;
import xyz.elidom.core.CoreMessageConstants;
import xyz.elidom.exception.client.ElidomInvalidParamsException;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.job.model.JobModel;
import xyz.elidom.job.system.job.AbstractJob;
import xyz.elidom.job.system.job.CustomServiceJob;
import xyz.elidom.job.system.job.DynamicJob;
import xyz.elidom.job.system.job.QueryJob;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ClassUtil;

/**
 * Scheduler Utilities
 * 
 * @author shortstop
 */
public class SchedulerUtil {

	/**
	 * Scheduler
	 */
	private static Scheduler scheduler;
	
	/**
	 * Scheduler field
	 */
	private static final String TRIGGER_SUFFIX = "_trg";
	
	/**
	 * Scheduler field : 'sched'
	 */
	private static final String SCHEDULER_FIELD = "sched";
	/**
	 * Job Manager Field : 'jobMgr'
	 */
	private static final String JOB_MGR_FIELD = "jobMgr";
	/**
	 * Job Manager Field : 'executingJobs'
	 */
	private static final String EXECUTING_JOBS_FIELD = "executingJobs";	
	
	/**
	 * Term label.handler : 'terms.label.handler'
	 */
	private static final String TERM_LABEL_HANDLER = "terms.label.handler";
	/**
	 * Term label.interval : 'terms.label.interval'
	 */
	private static final String TERM_LABEL_INTERVAL = "terms.label.interval";
	/**
	 * Term menu.Job : 'terms.menu.Job'
	 */
	private static final String TERM_MENU_JOB = "terms.menu.Job";

	/**
	 * get scheduler
	 * 
	 * @return
	 * @throws SchedulerException
	 */
	public static Scheduler getScheduler() throws SchedulerException {
		if (scheduler == null) {
			SchedulerFactoryBean factory = BeanUtil.get(SchedulerFactoryBean.class);
			scheduler = factory.getScheduler();
		}

		return scheduler;
	}

	/**
	 * schedule job
	 * 
	 * @param job
	 * @throws SchedulerException
	 */
	public static boolean scheduleJob(JobModel job) throws SchedulerException {
		Class<? extends Job> jobHandlerClazz = checkJobAction(job, JobModel.ACTION_START);
		String jobName = job.getName();
		String jobGroupName = job.getJobGroupName();
		String triggerName = jobName + TRIGGER_SUFFIX;
		String triggerGroupName = jobGroupName;
		Integer interval = null;

		try {
			interval = Integer.parseInt(job.getIntervalExpr());
		} catch (Exception e) {
		}

		Trigger trigger = null;
		if (ValueUtil.isNotEmpty(interval)) {
			trigger = newSimpleTrigger(triggerName, triggerGroupName, interval, job.getRepeatCount());
		} else {
			trigger = newCronTrigger(triggerName, triggerGroupName, job.getIntervalExpr(), job.getTimezone());
		}

		JobDetail jobDetail = JobBuilder.newJob(jobHandlerClazz).withIdentity(jobName, jobGroupName).build();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		jobDataMap.put(CoreConstants.ENTITY_FIELD_DOMAIN_ID, job.getDomainId());
		jobDataMap.put(AbstractJob.STR_JOB_MODEL_CLASS, job.getClass().getName());
		jobDataMap.put(AbstractJob.STR_JOB_MODEL_ID, job.getId());

		SchedulerUtil.getScheduler().scheduleJob(jobDetail, trigger);
		return true;
	}
	
	/**
	 * Rescheduling - AbstractJob에서 Job이 수행된 이후에 Interval이 적용되도록 하기 위해 Job 수행 이 후  
	 * 
	 * @param context
	 * @param job
	 * @return
	 */
	public static boolean rescheduleJob(JobExecutionContext context, JobModel job) {
		Scheduler scheduler = context.getScheduler();
		try {
			if(scheduler == null || scheduler.isShutdown()) {
				return false;
			}
		} catch (SchedulerException e) {
			return false;
		}
		
		Trigger prevTrigger = context.getTrigger();
		
		if(prevTrigger instanceof SimpleTrigger) {
			SimpleTrigger prevSimpleTrigger = (SimpleTrigger)prevTrigger;
			Date startTime = new Date(System.currentTimeMillis() + prevSimpleTrigger.getRepeatInterval());
			
			try {
				scheduler.rescheduleJob(prevSimpleTrigger.getKey(), prevSimpleTrigger.getTriggerBuilder().startAt(startTime).build());
				return true;
			} catch(SchedulerException se) {
			}			
		}
		
		return false;
	}	

	/**
	 * Cron Trigger 생성
	 * 
	 * @param triggerName
	 * @param triggerGroupName
	 * @param cronExpr
	 * @return
	 */
	private static Trigger newCronTrigger(String triggerName, String triggerGroupName, String cronExpr, String timeZone) {
		if(ValueUtil.isNotEmpty(timeZone)) {
			return newTrigger().withIdentity(triggerName, triggerGroupName).withSchedule(cronSchedule(cronExpr).inTimeZone(TimeZone.getTimeZone(timeZone))).build();
		} else {
			return newTrigger().withIdentity(triggerName, triggerGroupName).withSchedule(cronSchedule(cronExpr)).build();
		}
	}

	/**
	 * Simple Trigger 생성
	 * 
	 * @param triggerName
	 * @param triggerGroupName
	 * @param interval
	 * @param repeatCount
	 * @return
	 */
	private static Trigger newSimpleTrigger(String triggerName, String triggerGroupName, Integer interval, Integer repeatCount) {
		if (ValueUtil.isEmpty(repeatCount) || repeatCount < 1) {
			return newTrigger().withIdentity(triggerName, triggerGroupName).withSchedule(simpleSchedule().withIntervalInSeconds(interval).repeatForever()).build();
		} else {
			return newTrigger().withIdentity(triggerName, triggerGroupName).withSchedule(simpleSchedule().withIntervalInSeconds(interval).withRepeatCount(repeatCount)).build();
		}
	}

	/**
	 * resume job
	 * 
	 * @param job
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean resumeJob(xyz.elidom.job.model.JobModel job) throws SchedulerException {
		return actionJob(job, JobModel.ACTION_RESUME);
	}

	/**
	 * pause job
	 * 
	 * @param job
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean pauseJob(xyz.elidom.job.model.JobModel job) throws SchedulerException {
		return actionJob(job, JobModel.ACTION_PAUSE);
	}

	/**
	 * delete job
	 * 
	 * @param job
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean deleteJob(xyz.elidom.job.model.JobModel job) throws SchedulerException {
		return actionJob(job, JobModel.ACTION_DELETE);
	}
	
	/**
	 * interrupt job
	 * 
	 * @param job
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean interrupt(xyz.elidom.job.model.JobModel job) throws SchedulerException {
		return actionJob(job, JobModel.ACTION_INTERRUPT);
	}

	/**
	 * pause or delete job
	 * 
	 * @param job
	 * @param status
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean actionJob(xyz.elidom.job.model.JobModel job, String status) throws SchedulerException {
		checkJobAction(job, status);
		Scheduler scheduler = SchedulerUtil.getScheduler();
		String jobName = job.getName();
		String jobGroupName = job.getJobGroupName();
		List<String> jobGroupNames = scheduler.getJobGroupNames();
		boolean success = false;

		if (ValueUtil.isNotEmpty(jobGroupNames)) {
			for (String groupName : jobGroupNames) {
				if (ValueUtil.isEqual(groupName, jobGroupName)) {
					for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
						if (jobName.equals(jobKey.getName())) {
							if (ValueUtil.isEqual(status, JobModel.ACTION_PAUSE)) {
								scheduler.pauseJob(jobKey);
								success = true;
								break;

							} else if (ValueUtil.isEqual(status, JobModel.ACTION_RESUME)) {
								scheduler.resumeJob(jobKey);
								success = true;
								break;

							} else if (ValueUtil.isEqual(status, JobModel.ACTION_DELETE)) {
								scheduler.deleteJob(jobKey);
								success = true;
								break;
							} else if (ValueUtil.isEqual(status, JobModel.ACTION_INTERRUPT)) {
								scheduler.interrupt(jobKey);
								success = true;
								break;
							}
						}
					}
				}
			}
		} else {
			if (ValueUtil.isEqual(status, JobModel.ACTION_PAUSE) || ValueUtil.isEqual(status, JobModel.ACTION_DELETE)) {
				job.setStatus(JobModel.STATUS_DELETED);
			}
		}

		return success;
	}

	/**
	 * Check Job Transaction is valid
	 * 
	 * @param job
	 * @param action
	 * @return
	 */
	public static Class<? extends AbstractJob> checkJobAction(JobModel job, String action) {
		AssertUtil.assertNotEmpty(TERM_MENU_JOB, job);

		if ((ValueUtil.isEqual(JobModel.ACTION_START, action) || ValueUtil.isEqual(JobModel.ACTION_RESUME, action)) && ValueUtil.isEqual(job.getStatus(), JobModel.STATUS_RUNNING)) {
			throw new ElidomServiceException(CoreMessageConstants.ALREADY_PROCEED, "{0} is already Running!", MessageUtil.params(TERM_MENU_JOB));
		}

		if (ValueUtil.isEqual(JobModel.ACTION_CHANGE, action) && ValueUtil.isNotEqual(job.getStatus(), JobModel.STATUS_RUNNING)) {
			throw new ElidomServiceException(CoreMessageConstants.IS_NOT_RUNNING, "{0} is not Running!", MessageUtil.params(TERM_MENU_JOB));
		}

		if (ValueUtil.isEqual(JobModel.ACTION_PAUSE, action) && ValueUtil.isNotEqual(job.getStatus(), JobModel.STATUS_RUNNING)) {
			throw new ElidomServiceException(CoreMessageConstants.IS_NOT_RUNNING, "{0} is not Running!", MessageUtil.params(TERM_MENU_JOB));
		}

		if (ValueUtil.isEqual(JobModel.ACTION_PAUSE, action) || ValueUtil.isEqual(JobModel.ACTION_DELETE, action)) {
			return null;
		}

		AssertUtil.assertNotEmpty(TERM_LABEL_INTERVAL, job.getIntervalExpr());
		return SchedulerUtil.getHandlerClass(job);
	}

	/**
	 * jobModel로 부터 handler object를 생성
	 * 
	 * @param job
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Class<? extends AbstractJob> getHandlerClass(JobModel job) {
		String handlerType = job.getHandlerType();
		Class<?> jobClazz = null;

		if (ValueUtil.isEqual(handlerType, JobModel.HANDLER_STATIC)) {
			AssertUtil.assertNotEmpty(TERM_LABEL_HANDLER, job.getHandler());
			jobClazz = ClassUtil.forName(job.getHandler());

		} else if (ValueUtil.isEqual(handlerType, JobModel.HANDLER_DYNAMIC)) {
			jobClazz = ClassUtil.forName(DynamicJob.class.getName());

		} else if (ValueUtil.isEqual(handlerType, JobModel.HANDLER_QUERY)) {
			jobClazz = ClassUtil.forName(QueryJob.class.getName());

		} else if (ValueUtil.isEqual(handlerType, JobModel.HANDLER_DIY_SERVICE)) {
			AssertUtil.assertNotEmpty(TERM_LABEL_HANDLER, job.getHandler());
			jobClazz = ClassUtil.forName(CustomServiceJob.class.getName());
		} 

		if (!AbstractJob.class.isAssignableFrom(jobClazz)) {
			List<String> param = MessageUtil.params(TERM_LABEL_HANDLER, ValueUtil.isEmpty(job.getHandler()) ? jobClazz.getSimpleName() : job.getHandler(), AbstractJob.class.getName());
			throw new ElidomInvalidParamsException(CoreMessageConstants.IS_NOT_INSTANCE_OF, "Invalid {0} Class ({1}) - Class is not instance of {2}.", param);
		}

		return (Class<? extends AbstractJob>) jobClazz;
	}

	/**
	 * Clear unnecessary stuff
	 * 
	 * @param scheduler
	 */
	public static void clear() {
		Scheduler scheduler = null;
		Object quartzScheduler = null;
		Object jobMgr = null;

		try {
			scheduler = SchedulerUtil.getScheduler();
			quartzScheduler = getFieldValue(scheduler, SCHEDULER_FIELD);
			jobMgr = getFieldValue(quartzScheduler, JOB_MGR_FIELD);

			@SuppressWarnings("unchecked")
			Map<String, JobExecutionContext> executingJobs = (Map<String, JobExecutionContext>) getFieldValue(jobMgr, EXECUTING_JOBS_FIELD);
			List<String> removalList = new ArrayList<String>();
			Date currentDate = new Date();

			for (Map.Entry<String, JobExecutionContext> entry : executingJobs.entrySet()) {
				if (entry.getValue().getNextFireTime().before(currentDate)) {
					removalList.add(entry.getKey());
				}
			}

			for (String key : removalList) {
				executingJobs.remove(key);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * parent의 fieldName으로 값을 추출 
	 * 
	 * @param parent
	 * @param fieldName
	 * @return
	 */
	private static Object getFieldValue(Object parent, String fieldName) {
		Field field = ClassUtil.getField(parent.getClass(), fieldName);
		return ClassUtil.getFieldValue(parent, field);
	}
}