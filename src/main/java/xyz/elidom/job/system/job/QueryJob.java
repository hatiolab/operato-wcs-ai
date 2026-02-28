/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.system.job;

import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import xyz.elidom.job.model.JobModel;
import xyz.elidom.sys.util.ValueUtil;

/**
 * Query Job Handler
 * 
 * @author shortstop
 */
@Component
@DisallowConcurrentExecution
public class QueryJob extends AbstractJob {

	@Override
	@Transactional
	public Object doExecuteJob(JobExecutionContext context, JobModel jobModel) throws JobExecutionException {
		Map<String, Object> params = null;
		
		if(context.get(AbstractJob.STR_JOB_TRACE) != null) {
			params = ValueUtil.newMap(AbstractJob.STR_JOB_TRACE, context.get(AbstractJob.STR_JOB_TRACE));
		}
		
		Object result = jobModel.executeQuery(params);
		context.setResult(result);
		return result;
	}
}
