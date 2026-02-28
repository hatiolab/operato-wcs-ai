/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.entity;

import java.net.InetAddress;
import java.net.UnknownHostException;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.job.entity.relation.JobRef;
import xyz.elidom.job.model.JobModel;
import xyz.elidom.job.rest.JobTraceController;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.DomainCreateStampHook;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;

@Table(name = "job_traces", idStrategy = GenerationRule.UUID, indexes = { 
	@Index(name = "ix_job_trace_0", columnList = "domain_id,name"),
	@Index(name = "ix_job_trace_1", columnList = "domain_id,status"),
	@Index(name = "ix_job_trace_2", columnList = "domain_id,job_id")
})
public class JobTrace extends DomainCreateStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -2271910818162472263L;
	
	@PrimaryKey
	@Column(name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;
	
	@Column (name = "job_id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String jobId;

	@Relation(field = "jobId")
	private JobRef job;

	@Column (name = "name", length = OrmConstants.FIELD_SIZE_NAME)
	private String name;

	@Column (name = "job_class", length = OrmConstants.FIELD_SIZE_VALUE_255)
	private String jobClass;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "read_count")
	private Integer readCount;

	@Column (name = "write_count")
	private Integer writeCount;

	@Column (name = "write_fail_count")
	private Integer writeFailCount;

	@Column (name = "started_at", length = OrmConstants.FIELD_SIZE_DATETIME)
	private String startedAt;

	@Column (name = "finished_at", length = OrmConstants.FIELD_SIZE_DATETIME)
	private String finishedAt;

	@Column (name = "msg", length = OrmConstants.FIELD_SIZE_MAX_TEXT)
	private String msg;

	@Column (name = "log", length = OrmConstants.FIELD_SIZE_MAX_TEXT)
	private String log;
		
	public JobTrace() {
	}
	
	public JobTrace(Long domainId, String jobId) {
		this.domainId = domainId;
		this.jobId = jobId;
	}
	
	public JobTrace(JobModel job) {
		this.domainId = job.getDomainId();
		this.jobId = job.getId();
		this.name = job.getName();
		this.jobClass = job.getHandler();
	}
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public JobRef getJob() {
		return job;
	}

	public void setJob(JobRef job) {
		this.job = job;

		if(this.job != null) {
			if (this.job.getId() != null) {
				this.jobId = this.job.getId();
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getReadCount() {
		return readCount;
	}

	public void setReadCount(Integer readCount) {
		this.readCount = readCount;
	}

	public Integer getWriteCount() {
		return writeCount;
	}

	public void setWriteCount(Integer writeCount) {
		this.writeCount = writeCount;
	}

	public Integer getWriteFailCount() {
		return writeFailCount;
	}

	public void setWriteFailCount(Integer writeFailCount) {
		this.writeFailCount = writeFailCount;
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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	/**
	 * Job 실행 후 Trace 생성 
	 * 
	 * @param trace
	 * @param status
	 * @param result
	 */
	public static void jobTrace(JobTrace trace, String status, String result) {
		trace.setFinishedAt(DateUtil.currentTimeStr());
		trace.setStatus(status);
		
		try {
			String hostAddr = InetAddress.getLocalHost().getHostAddress();
			trace.setMsg(ValueUtil.isNotEmpty(result) ? hostAddr + " - " + result : hostAddr);
		} catch (UnknownHostException e) {
		}
		
		JobTraceController jobTraceController = BeanUtil.get(JobTraceController.class);
		jobTraceController.create(trace);		
	}	
}