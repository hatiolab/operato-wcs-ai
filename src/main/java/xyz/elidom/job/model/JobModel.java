/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.model;

import java.util.Map;

import org.quartz.JobExecutionContext;

import xyz.elidom.job.entity.JobTrace;

/**
 * Job Model 인터페이스 
 * 
 * @author shortstop
 */
public interface JobModel {
	
	/**
	 * JOB Status - 실행 중 
	 */
	public static final String STATUS_RUNNING = "RUNNING";
	/**
	 * JOB Status - 일시 중지 
	 */
	public static final String STATUS_PAUSED = "PAUSED";
	/**
	 * JOB Status - 삭제됨 
	 */
	public static final String STATUS_DELETED = "STOPPED";
	
	/**
	 * JOB Action - 실행 
	 */
	public static final String ACTION_START = "START";
	/**
	 * JOB Action - 재실행 
	 */
	public static final String ACTION_RESUME = "RESUME";	
	/**
	 * JOB Action - 일시 중지  
	 */
	public static final String ACTION_PAUSE = "PAUSE";
	/**
	 * JOB Action - 변경  
	 */
	public static final String ACTION_CHANGE = "CHANGE";
	/**
	 * JOB Action - 삭제 
	 */
	public static final String ACTION_DELETE = "DELETE";
	/**
	 * JOB Interrupt - Job 실행 중 종료
	 */
	public static final String ACTION_INTERRUPT = "INTERRUPT";
	
	/**
	 * Handler Type - static 
	 */
	public static final String HANDLER_STATIC = "static";
	/**
	 * Handler Type - dynamic 
	 */
	public static final String HANDLER_DYNAMIC = "dynamic";
	/**
	 * Handler Type - query
	 */
	public static final String HANDLER_QUERY = "query";		
	/**
	 * Handler Type - diy service 
	 */
	public static final String HANDLER_DIY_SERVICE = "diy_service";	
	/**
	 * Handler Type - alarm 
	 */
	public static final String HANDLER_ALARM = "alarm";
	
	/**
	 * Execution - OK
	 */
	public static final String EXECUTION_OK = "OK";
	/**
	 * Execution - NG
	 */
	public static final String EXECUTION_NG = "NG";	
	
	/**
	 * Job Id
	 * 
	 * @return
	 */
	public String getId();
	
	public void setId(String id);
	
	/**
	 * Domain Id
	 * 
	 * @return
	 */
	public Long getDomainId();
	
	public void setDomainId(Long domainId);
	
	/**
	 * Job Name
	 * 
	 * @return
	 */
	public String getName();
	
	public void setName(String name);

	/**
	 * Job Description
	 * 
	 * @return
	 */
	public String getDescription();
	
	public void setDescription(String description);

	/**
	 * Job Handler Type
	 * 
	 * @return
	 */
	public String getHandlerType();
	
	public void setHandlerType(String handlerType);

	/**
	 * Job Handler
	 * 
	 * @return
	 */
	public String getHandler();
	
	public void setHandler(String handler);

	/**
	 * Simple Interval (seconds) or Cron Expression 
	 * 
	 * @return
	 */
	public String getIntervalExpr();
	
	public void setIntervalExpr(String intervalExpr);
	
	/**
	 * Timezone: def ETC
	 * @return
	 */
	
	public String getTimezone();

	public void setTimezone(String timezone);
	
	
	/**
	 * Job repeat count
	 * 
	 * @return
	 */
	public Integer getRepeatCount();

	public void setRepeatCount(Integer repeatCount);
	
	/**
	 * OK Count
	 * 
	 * @return
	 */
	public Integer getOkCount();
	
	public void setOkCount(Integer okCount);
	
	/**
	 * NG Count
	 * 
	 * @return
	 */
	public Integer getNgCount();
	
	public void setNgCount(Integer ngCount);
	
	/**
	 * Job Status
	 * 
	 * @return
	 */
	public String getStatus();
	
	public void setStatus(String status);

	/**
	 * Job Trace 여부 
	 * 
	 * @return
	 */
	public Boolean getTrace();
	
	public void setTrace(Boolean trace);

	/**
	 * Job 실행 결과 
	 * 
	 * @return
	 */
	public String getResult();
	
	public void setResult(String result);
	
	/**
	 * Job Group Name
	 * 
	 * @return
	 */
	public String getJobGroupName();
	
	/**
	 * Dynamic Logic
	 * 
	 * @return
	 */
	public String getLogic();
	
	public void setLogic(String logic);
	
	public JobTrace newJobTrace();
	
	/**
	 * Schedule job
	 */
	public boolean scheduleJob();
	
	/**
	 * Unschedule job
	 */
	public boolean unscheduleJob();
	
	/**
	 * Interrupt job
	 */
	public boolean interrupt();
	
	/**
	 * Pause job
	 */
	public boolean pauseJob();
	
	/**
	 * Dynamic Logic 수행 
	 * 
	 * @param params
	 * @return
	 */
	public Object executeLogic(Map<String, Object> params);
	
	/**
	 * Dynamic Query 수행 
	 * 
	 * @param params
	 * @return
	 */
	public Object executeQuery(Map<String, Object> params);
	
	/**
	 * job 수행 전 실행 
	 * 
	 * @param context
	 * @return job 실행이 가능한 상태인지 여부를 리턴 
	 */
	public boolean beforeExecuteJob(JobExecutionContext context);
	
	/**
	 * job 수행 후 실행 
	 * 
	 * @param context
	 */
	public void afterExecuteJob(JobExecutionContext context);
	
	/**
	 * job 수행 중 에러 발생시 실행 
	 * 
	 * @param context
	 * @param th
	 */
	public void onJobError(JobExecutionContext context, Throwable th);
}
