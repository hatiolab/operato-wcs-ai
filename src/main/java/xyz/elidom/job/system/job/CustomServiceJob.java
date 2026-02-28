/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.system.job;

import java.util.HashMap;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import xyz.elidom.dev.entity.DiyService;
import xyz.elidom.job.model.JobModel;

/**
 * DiyService 실행을 위한 Job
 * 
 * @author shortstop
 */
@Component
@DisallowConcurrentExecution
public class CustomServiceJob extends AbstractJob {

	/**
	 * DIY_SERVICE PARAMS Key
	 */
	public static final String KEY_DIY_SERVICE_PARAMS = "DIY_SERVICE_PARAMS";

	@Override
	@Transactional
	public Object doExecuteJob(JobExecutionContext context, JobModel jobModel) throws JobExecutionException {
		Map<String, Object> inputParams = new HashMap<String, Object>();
		
		@SuppressWarnings("unchecked")
		Map<String, Object> serviceParams = (Map<String, Object>) context.get(KEY_DIY_SERVICE_PARAMS);
		if(serviceParams == null) {
			serviceParams = new HashMap<String, Object>();
		}
		
		serviceParams.put(AbstractJob.STR_JOB_MODEL, jobModel);
		if(context.get(AbstractJob.STR_JOB_TRACE) != null) {
			serviceParams.put(AbstractJob.STR_JOB_TRACE, context.get(AbstractJob.STR_JOB_TRACE));
		}
		
		inputParams.put("input", serviceParams);
		Object result = DiyService.doDiyService(jobModel.getDomainId(), jobModel.getHandler(), inputParams);
		context.setResult(result);
		return result;
	}
}